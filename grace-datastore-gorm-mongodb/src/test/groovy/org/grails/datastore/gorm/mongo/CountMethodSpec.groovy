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
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

import grails.gorm.annotation.Entity
import grails.mongodb.MongoEntity

import org.grails.datastore.mapping.config.Settings
import org.grails.datastore.mapping.mongo.MongoDatastore
import org.grails.datastore.mapping.mongo.config.MongoSettings

import static com.mongodb.client.model.Filters.eq

/**
 * Created by graemerocher on 29/11/2016.
 */
class CountMethodSpec extends Specification {

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
                (Settings.SETTING_FAIL_ON_ERROR): true,
                (MongoSettings.SETTING_URL)     : "mongodb://$serverAddress".toString()
        ]
        this.datastore = new MongoDatastore(config, [CountTest] as Class[])
    }

    void cleanupSpec() {
        this.serverAddress = null
        if (this.running != null) {
            this.running.close()
        }
        this.running = null
        this.datastore.close()
    }

    void 'test count method'() {
        given: 'some test data '
        CountTest.DB.drop()
        CountTest.withNewSession {
            new CountTest(name: 'foo').save()
            new CountTest(name: 'bar').save(flush: true)
        }

        expect:
        CountTest.find(eq('name', 'foo'))
        CountTest.count(eq('name', 'foo')) == 1
    }

}

@Entity
class CountTest implements MongoEntity<CountTest> {

    String name

}
