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
package grails.mongodb.bootstrap

import com.mongodb.client.MongoClient
import de.flapdoodle.embed.mongo.commands.ServerAddress
import de.flapdoodle.embed.mongo.distribution.Version
import de.flapdoodle.embed.mongo.transitions.ImmutableMongod
import de.flapdoodle.embed.mongo.transitions.Mongod
import de.flapdoodle.embed.mongo.transitions.RunningMongodProcess
import de.flapdoodle.reverse.TransitionWalker
import org.bson.Document
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import spock.lang.Ignore
import spock.lang.Issue
import spock.lang.Shared
import spock.lang.Specification

import grails.mongodb.MongoEntity
import grails.mongodb.geo.Point
import grails.persistence.Entity
import grails.validation.ValidationException

import org.grails.datastore.gorm.mongo.Birthday
import org.grails.datastore.gorm.mongo.BirthdayCodec
import org.grails.datastore.mapping.config.Settings
import org.grails.datastore.mapping.core.DatastoreUtils
import org.grails.datastore.mapping.engine.types.AbstractMappingAwareCustomTypeMarshaller
import org.grails.datastore.mapping.model.MappingContext
import org.grails.datastore.mapping.model.PersistentProperty
import org.grails.datastore.mapping.mongo.MongoDatastore
import org.grails.datastore.mapping.mongo.config.MongoMappingContext
import org.grails.datastore.mapping.mongo.config.MongoSettings
import org.grails.datastore.mapping.query.Query

/**
 * @author Graeme Rocher
 */
class MongoDbDataStoreSpringInitializerSpec extends Specification {

    @Shared
    Map config

    @Shared
    protected TransitionWalker.ReachedState<RunningMongodProcess> running

    @Shared
    protected ServerAddress serverAddress

    void setupSpec() {
        ImmutableMongod mongodbConfig = Mongod.instance()
        Version.Main version = Version.Main.V7_0

        this.running = mongodbConfig.start(version)
        this.serverAddress = running.current().getServerAddress()

        this.config = [
                (Settings.SETTING_FAIL_ON_ERROR): true,
                (MongoSettings.SETTING_URL)     : "mongodb://$serverAddress".toString()
        ]
    }

    void cleanupSpec() {
        this.serverAddress = null
        if (this.running != null) {
            this.running.close()
        }
        this.running = null
        this.config.clear()
    }

    void 'Test that MongoDbDatastoreSpringInitializer can setup GORM for MongoDB from scratch'() {
        when: 'the initializer used to setup GORM for MongoDB'
        def initializer = new MongoDbDataStoreSpringInitializer(DatastoreUtils.createPropertyResolver(config), Person)
        def applicationContext = initializer.configure()
        def mongo = applicationContext.getBean(MongoClient)
        mongo.getDatabase(MongoDbDataStoreSpringInitializer.DEFAULT_DATABASE_NAME).drop()

        then: 'GORM for MongoDB is initialized correctly'
        Person.count() == 0
    }

    void 'Test specify mongo database name settings'() {
        when: 'the initializer used to setup GORM for MongoDB'
        config['grails.mongodb.databaseName'] = 'foo'
        def initializer = new MongoDbDataStoreSpringInitializer(DatastoreUtils.createPropertyResolver(config), Person)
        def applicationContext = initializer.configure()
        def mongoDatastore = applicationContext.getBean(MongoDatastore)

        then: 'GORM for MongoDB is initialized correctly'
        mongoDatastore.getDefaultDatabase() == 'foo'

        cleanup:
        mongoDatastore.destroy()
    }

    void 'Test the alias is created when it is the primary datastore'() {
        when: 'the initializer used to setup GORM for MongoDB'
        config['grails.mongodb.databaseName'] = 'foo'
        def initializer = new MongoDbDataStoreSpringInitializer(DatastoreUtils.createPropertyResolver(config), Person)
        def applicationContext = initializer.configure()
        def mongoDatastore = applicationContext.getBean(MongoDatastore)

        then:
        applicationContext.containsBean('grailsDomainClassMappingContext')

        cleanup:
        mongoDatastore.destroy()
    }

    void 'Test the alias is not created when it is the secondary datastore'() {
        when: 'the initializer used to setup GORM for MongoDB'
        config['grails.mongodb.databaseName'] = 'foo'
        def initializer = new MongoDbDataStoreSpringInitializer(DatastoreUtils.createPropertyResolver(config), Person)
        initializer.setSecondaryDatastore(true)
        def applicationContext = initializer.configure()
        def mongoDatastore = applicationContext.getBean(MongoDatastore)

        then:
        !applicationContext.containsBean('grailsDomainClassMappingContext')

        cleanup:
        mongoDatastore.destroy()
    }

    @Issue('GPMONGODB-339')
    @Ignore
    // The MongoDB API for this test has been altered / removed with no apparent replacement for getting the number
    // of pooled connections in use
    void 'Test withTransaction returns connections when used without session handling'() {
        given: 'the initializer used to setup GORM for MongoDB'
        def initializer = new MongoDbDataStoreSpringInitializer(DatastoreUtils.createPropertyResolver(config), Person)
        def applicationContext = initializer.configure()
        def mongo = applicationContext.getBean(Mongo)

        when: 'The a normal GORM method is used'
        Person.count()
        then: 'No connections are in use afterwards'
        db.getStats().get('connections') == 0
        mongo.connector.@_masterPortPool.statistics.inUse == 0

        when: 'The withTransaction method is used'
        Person.withTransaction {
            new Person(name: 'Bob').save()
        }

        then: 'No connections in use'
        mongo.connector.@_masterPortPool.statistics.inUse == 0
    }

    void 'Test that constraints and Geo types work'() {
        given: 'the initializer used to setup GORM for MongoDB'
        def initializer = new MongoDbDataStoreSpringInitializer(DatastoreUtils.createPropertyResolver(config), Person)
        initializer.configure()
        Person.DB.drop()

        when: 'we try to persist an invalid object'
        def p = new Person().save(flush: true)

        then: 'Throw ValidationException'
        thrown(ValidationException)

        when: 'We persist a Geo type'
        Person.withNewSession {
            new Person(name: 'Bob', home: Point.valueOf(10, 10)).save(flush: true)
            p = Person.first()
        }

        then: 'The geo type was persisted'
        p != null
        p.home != null
    }

    @Ignore
    void 'Test custom codecs from Spring'() {
        given: 'the initializer used to setup GORM for MongoDB'
        def initializer = new MongoDbDataStoreSpringInitializer(DatastoreUtils.createPropertyResolver(config), Person)
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext()
        applicationContext.beanFactory.registerSingleton('birthdayCodec', new BirthdayCodec())

        initializer.configureForBeanDefinitionRegistry(applicationContext)
        applicationContext.refresh()
        Person.DB.drop()

        when: 'we persist an object with a custom type '
        def birthday = new Birthday(new Date())
        def p = new Person(name: 'Bob', home: Point.valueOf(10, 10), birthday: birthday).save(flush: true)

        then: 'The object was persisted successfully'
        Person.findByBirthday(birthday).birthday == birthday
        !Person.findByBirthday(new Birthday(new Date() - 7))
    }

    @Ignore
    void 'Test custom type marshallers from Spring'() {
        given: 'the initializer used to setup GORM for MongoDB'
        def initializer = new MongoDbDataStoreSpringInitializer(DatastoreUtils.createPropertyResolver(config), Person)
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext()
        applicationContext.beanFactory.registerSingleton('birthdayMarshaller', new BirthdayCustomTypeMarshaller())

        initializer.configureForBeanDefinitionRegistry(applicationContext)
        applicationContext.refresh()
        Person.DB.drop()

        when: 'we persist an object with a custom type '
        def birthday = new Birthday(new Date())
        new Person(name: 'Bob', home: Point.valueOf(10, 10), birthday: birthday).save(flush: true)

        then: 'The object was persisted successfully'
        Person.first().birthday == birthday
        Person.findByBirthday(birthday).birthday == birthday
        !Person.findByBirthday(new Birthday(new Date() - 7))
    }

}

@Entity
class Person implements MongoEntity<Person> {

    Long id
    Long version
    String name
    Point home
    Birthday birthday

    static constraints = {
        name blank: false
        birthday nullable: true
    }

}

class BirthdayCustomTypeMarshaller extends AbstractMappingAwareCustomTypeMarshaller<Birthday, Document, Document> {

    BirthdayCustomTypeMarshaller() {
        super(Birthday)
    }

    @Override
    boolean supports(MappingContext context) {
        return context instanceof MongoMappingContext
    }

    @Override
    protected Object writeInternal(PersistentProperty property, String key, Birthday value, Document nativeTarget) {
        nativeTarget.put(key, value.date)
        return nativeTarget
    }

    @Override
    protected Birthday readInternal(PersistentProperty property, String key, Document nativeSource) {
        return new Birthday(nativeSource.getDate(key))
    }

    @Override
    protected void queryInternal(PersistentProperty property, String key, Query.PropertyCriterion value,
            Document nativeQuery) {
        nativeQuery.put(key, value.getValue().date)
    }

}
