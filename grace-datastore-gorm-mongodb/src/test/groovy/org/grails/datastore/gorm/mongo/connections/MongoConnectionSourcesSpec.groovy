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
package org.grails.datastore.gorm.mongo.connections

import de.flapdoodle.embed.mongo.commands.ServerAddress
import de.flapdoodle.embed.mongo.distribution.Version
import de.flapdoodle.embed.mongo.transitions.ImmutableMongod
import de.flapdoodle.embed.mongo.transitions.Mongod
import de.flapdoodle.embed.mongo.transitions.RunningMongodProcess
import de.flapdoodle.reverse.TransitionWalker
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

import org.grails.datastore.mapping.core.Session
import org.grails.datastore.mapping.mongo.MongoDatastore
import org.grails.datastore.mapping.mongo.config.MongoSettings
import org.grails.datastore.mapping.mongo.connections.MongoConnectionSources
import org.grails.datastore.mapping.multitenancy.resolvers.SystemPropertyTenantResolver

/**
 * Created by graemerocher on 15/07/2016.
 */
class MongoConnectionSourcesSpec extends Specification {

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
                'grails.gorm.connectionSourcesClass'          : MongoConnectionSources,
                'grails.gorm.multiTenancy.mode'               : 'DATABASE',
                'grails.gorm.multiTenancy.tenantResolverClass': SystemPropertyTenantResolver,
                (MongoSettings.SETTING_URL)                   : "mongodb://$serverAddress/defaultDb".toString(),
                (MongoSettings.SETTING_CONNECTIONS)           : [
                        test1: [
                                url: "mongodb://$serverAddress/test1Db".toString()
                        ],
                        test2: [
                                url: "mongodb://$serverAddress/test2Db".toString()
                        ]
                ]
        ]
        this.datastore = new MongoDatastore(config, CompanyB)
    }

    void cleanupSpec() {
        this.serverAddress = null
        if (this.running != null) {
            this.running.close()
        }
        this.running = null
        this.datastore.destroy()
    }

    void 'Test persist and retrieve entities with multi tenancy'() {
        setup:
        CompanyB.eachTenant {
            CompanyB.DB.drop()
        }

        when: 'A tenant id is present'
        System.setProperty(SystemPropertyTenantResolver.PROPERTY_NAME, 'test1')

        then: 'the correct tenant is used'
        CompanyB.count() == 0
        CompanyB.DB.name == 'test1Db'

        when: 'An object is saved'
        new CompanyB(name: 'Foo').save(flush: true)

        then: 'The results are correct'
        CompanyB.count() == 1

        when: 'The tenant id is switched'
        System.setProperty(SystemPropertyTenantResolver.PROPERTY_NAME, 'test2')

        then: 'the correct tenant is used'
        CompanyB.DB.name == 'test2Db'
        CompanyB.count() == 0
        CompanyB.withTenant('test1') { Serializable tenantId, Session s ->
            assert tenantId
            assert s
            CompanyB.count() == 1
        }

        when: 'each tenant is iterated over'
        Map tenantIds = [:]
        CompanyB.eachTenant { String tenantId ->
            tenantIds.put(tenantId, CompanyB.count())
        }

        then: 'The result is correct'
        tenantIds == [test1: 1, test2: 0]

        when: 'A data source is added and switched to at runtime'
        datastore.connectionSources.addConnectionSource('test3', [url: "mongodb://$serverAddress/test3Db".toString()])
        System.setProperty(SystemPropertyTenantResolver.PROPERTY_NAME, 'test3')

        then: 'The database is usable'
        CompanyB.DB.name == 'test3Db'
        CompanyB.count() == 0
    }

}
