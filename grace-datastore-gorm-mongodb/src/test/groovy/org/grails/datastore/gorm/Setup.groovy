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
package org.grails.datastore.gorm

import com.mongodb.BasicDBObject
import de.flapdoodle.embed.mongo.commands.ServerAddress
import de.flapdoodle.embed.mongo.distribution.Version
import de.flapdoodle.embed.mongo.transitions.ImmutableMongod
import de.flapdoodle.embed.mongo.transitions.Mongod
import de.flapdoodle.embed.mongo.transitions.RunningMongodProcess
import de.flapdoodle.reverse.TransitionWalker
import org.bson.Document
import org.springframework.transaction.support.TransactionSynchronizationManager
import org.springframework.util.StringUtils
import org.springframework.validation.Errors
import org.springframework.validation.Validator

import grails.gorm.tests.GormDatastoreSpec

import org.grails.datastore.bson.query.BsonQuery
import org.grails.datastore.gorm.mongo.Birthday
import org.grails.datastore.mapping.core.Session
import org.grails.datastore.mapping.engine.types.AbstractMappingAwareCustomTypeMarshaller
import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.datastore.mapping.model.PersistentProperty
import org.grails.datastore.mapping.mongo.AbstractMongoSession
import org.grails.datastore.mapping.mongo.MongoDatastore
import org.grails.datastore.mapping.mongo.config.MongoSettings
import org.grails.datastore.mapping.query.Query.Between
import org.grails.datastore.mapping.query.Query.PropertyCriterion

/**
 * @author graemerocher
 */
class Setup {

    static MongoDatastore mongo
    static AbstractMongoSession session
    static TransitionWalker.ReachedState<RunningMongodProcess> running
    static ServerAddress serverAddress

    static destroy() {
        serverAddress = null
        if (running != null) {
            running.close()
        }
        running = null
        session.nativeInterface.dropDatabase(session.defaultDatabase)
        session.disconnect()
        TransactionSynchronizationManager.unbindResource(mongo)
        mongo.close()
    }

    static Session setup(classes) {
        ImmutableMongod mongodbConfig = Mongod.instance()
        Version.Main version = Version.Main.V7_0

        running = mongodbConfig.start(version)
        serverAddress = running.current().getServerAddress()

        def databaseName = System.getProperty(GormDatastoreSpec.CURRENT_TEST_NAME) ?: 'test'

        Map<String, Object> config = [
                (MongoSettings.SETTING_DATABASE_NAME): databaseName,
                (MongoSettings.SETTING_URL)          : "mongodb://$serverAddress".toString()
        ]

        // disable decimal type support on Travis, since MongoDB 3.4 support doesn't exist there yet
        if (System.getenv('TRAVIS')) {
            config.put(MongoSettings.SETTING_DECIMAL_TYPE, false)
        }
        mongo = new MongoDatastore(config, classes as Class[])
        mongo.mappingContext.mappingFactory.registerCustomType(
                new AbstractMappingAwareCustomTypeMarshaller<Birthday, Document, Document>(Birthday) {

                    @Override
                    protected Object writeInternal(PersistentProperty property, String key, Birthday value,
                            Document nativeTarget) {
                        final converted = value.date.time
                        nativeTarget.put(key, converted)
                        return converted
                    }

                    @Override
                    protected void queryInternal(PersistentProperty property, String key, PropertyCriterion criterion,
                            Document nativeQuery) {
                        if (criterion instanceof Between) {
                            def dbo = new BasicDBObject()
                            dbo.put(BsonQuery.MONGO_GTE_OPERATOR, criterion.getFrom().date.time)
                            dbo.put(BsonQuery.MONGO_LTE_OPERATOR, criterion.getTo().date.time)
                            nativeQuery.put(key, dbo)
                        }
                        else {
                            nativeQuery.put(key, criterion.value.date.time)
                        }
                    }

                    @Override
                    protected Birthday readInternal(PersistentProperty property, String key, Document nativeSource) {
                        final num = nativeSource.get(key)
                        if (num instanceof Long) {
                            return new Birthday(new Date(num))
                        }
                        return null
                    }

                })

        PersistentEntity entity = mongo.mappingContext.persistentEntities.find { PersistentEntity e ->
            e.name.contains('TestEntity')
        }

        mongo.mappingContext.addEntityValidator(entity, [
                supports: { Class c -> true },
                validate: { Object o, Errors errors ->
                    if (!StringUtils.hasText(o.name)) {
                        errors.rejectValue('name', 'name.is.blank')
                    }
                }
        ] as Validator)

        session = mongo.connect()

        TransactionSynchronizationManager.bindResource(mongo, session)

        return session
    }

}
