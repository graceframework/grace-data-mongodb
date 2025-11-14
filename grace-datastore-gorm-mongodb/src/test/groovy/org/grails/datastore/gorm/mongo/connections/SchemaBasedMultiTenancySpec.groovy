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

import org.grails.datastore.gorm.mongo.City
import org.grails.datastore.mapping.core.Session
import org.grails.datastore.mapping.mongo.MongoDatastore
import org.grails.datastore.mapping.mongo.config.MongoSettings
import org.grails.datastore.mapping.multitenancy.exceptions.TenantNotFoundException
import org.grails.datastore.mapping.multitenancy.resolvers.SystemPropertyTenantResolver

/**
 * Created by graemerocher on 14/07/2016.
 */
class SchemaBasedMultiTenancySpec extends Specification {

    @AutoCleanup
    @Shared
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
                (MongoSettings.SETTING_URL)                   : "mongodb://$serverAddress/defaultDb".toString(),
                'grails.gorm.multiTenancy.mode'               : 'SCHEMA',
                'grails.gorm.multiTenancy.tenantResolverClass': SystemPropertyTenantResolver
        ]
        this.datastore = new MongoDatastore(config, getDomainClasses() as Class[])
    }

    void setup() {
        System.setProperty(SystemPropertyTenantResolver.PROPERTY_NAME, '')
    }

    void cleanupSpec() {
        this.serverAddress = null
        if (this.running != null) {
            this.running.close()
        }
        this.running = null
        this.datastore.close()
    }

    void 'Test no tenant id'() {
        when:
        CompanyB.DB

        then:
        thrown(TenantNotFoundException)
    }

    void 'Test multi tenancy state'() {
        given:
        System.setProperty(SystemPropertyTenantResolver.PROPERTY_NAME, 'test1')
        expect:
        City.DB.name == 'defaultDb'
        CompanyB.DB.name == 'test1'
    }

    void 'Test persist and retrieve entities with multi tenancy'() {
        setup:
        CompanyB.eachTenant {
            try {
                CompanyB.DB.drop()
            }
            catch (ignore) {
                // continue
            }
        }

        when: 'A tenant id is present'
        System.setProperty(SystemPropertyTenantResolver.PROPERTY_NAME, 'test1')

        then: 'the correct tenant is used'
        CompanyB.count() == 0
        CompanyB.DB.name == 'test1'

        when: 'An object is saved'
        new CompanyB(name: 'Foo').save(flush: true)

        then: 'The results are correct'
        CompanyB.count() == 1

        when: 'The tenant id is switched'
        System.setProperty(SystemPropertyTenantResolver.PROPERTY_NAME, 'test2')

        then: 'the correct tenant is used'
        CompanyB.DB.name == 'test2'
        CompanyB.count() == 0
        new CompanyB(name: 'Bar').save(flush: true)
        CompanyB.withTenant('test1') { Serializable tenantId, Session s ->
            assert tenantId
            assert s
            new CompanyB(name: 'Baz').save(flush: true)
            CompanyB.count() == 2
        }

        when: 'each tenant is iterated over'
        final Map<String, Integer> companyCount = [:]
        CompanyB.eachTenant { String tenantId ->
            companyCount.put(tenantId, CompanyB.count())
        }

        then: 'The result is correct'
        companyCount['admin'] == 0
        companyCount['test1'] == 2
        companyCount['test2'] == 1
    }

    List getDomainClasses() {
        [City, CompanyB]
    }

}
