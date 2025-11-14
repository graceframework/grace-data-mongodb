/*
 * Copyright 2016-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.grails.datastore.gorm.mongo

import de.flapdoodle.embed.mongo.commands.ServerAddress
import de.flapdoodle.embed.mongo.distribution.Version
import de.flapdoodle.embed.mongo.transitions.ImmutableMongod
import de.flapdoodle.embed.mongo.transitions.Mongod
import de.flapdoodle.embed.mongo.transitions.RunningMongodProcess
import de.flapdoodle.reverse.TransitionWalker
import org.bson.types.ObjectId
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

import grails.gorm.annotation.Entity
import grails.mongodb.MongoEntity

import org.grails.datastore.mapping.mongo.MongoDatastore
import org.grails.datastore.mapping.mongo.config.MongoSettings

/**
 * Created by graemerocher on 30/06/16.
 */
class MultipleConnectionsSpec extends Specification {

    @Shared
    @AutoCleanup
    MongoDatastore datastore

    @Shared
    protected TransitionWalker.ReachedState<RunningMongodProcess> running

    @Shared
    protected ServerAddress serverAddress

    void setupSpec() {
        ImmutableMongod mongodbConfig = Mongod.instance()
        Version.Main version = Version.Main.V7_0

        this.running = mongodbConfig.start(version)
        this.serverAddress = running.current().getServerAddress()

        Map config = [
                (MongoSettings.SETTING_CONNECTIONS): [
                        test1: [
                                url: "mongodb://$serverAddress/test1Db".toString()
                        ],
                        test2: [
                                url: "mongodb://$serverAddress/test2Db".toString()
                        ]
                ],
                (MongoSettings.SETTING_URL)        : "mongodb://$serverAddress".toString()
        ]
        this.datastore = new MongoDatastore(config, getDomainClasses() as Class[])
    }

    void cleanupSpec() {
        this.serverAddress = null
        if (this.running != null) {
            this.running.close()
        }
        this.running = null
        this.datastore.close()
    }

    void 'Test multiple datasources state'() {
        expect:
        CompanyA.DB.name == 'test1Db'
        CompanyA.test2.DB.name == 'test2Db'
    }

    void 'Test query multiple data sources'() {
        setup:
        CompanyA.DB.drop()
        CompanyA.test2.DB.drop()

        when: 'An entity is saved'
        new CompanyA(name: 'One').save(flush: true)

        then: 'The results are correct'
        CompanyA.count() == 1
        CompanyA.withConnection('test2') { count() } == 0

        when: 'An entity is saved to another connection'
        new CompanyA(name: 'Two').save(flush: true)
        CompanyA.withConnection('test2') {
            save(new CompanyA(name: 'Three'), [flush: true])
        }

        then: 'The results are correct'
        CompanyA.count() == 2
        CompanyA.first()
        CompanyA.withConnection('test2') { count() == 1 }
    }

    List getDomainClasses() {
        [CompanyA]
    }

}

/**
 * Created by graemerocher on 30/06/16.
 */
@Entity
class CompanyA implements MongoEntity<CompanyA> {

    ObjectId id
    String name

    static mapping = {
        connections 'test1', 'test2'
    }

}
