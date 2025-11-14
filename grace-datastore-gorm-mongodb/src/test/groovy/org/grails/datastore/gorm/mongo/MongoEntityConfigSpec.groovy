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

import com.mongodb.WriteConcern
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoDatabase
import spock.lang.IgnoreIf

import grails.gorm.tests.GormDatastoreSpec
import grails.mongodb.MongoEntity
import grails.persistence.Entity

import org.grails.datastore.mapping.document.config.DocumentPersistentEntity
import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.datastore.mapping.mongo.AbstractMongoSession
import org.grails.datastore.mapping.mongo.config.MongoAttribute
import org.grails.datastore.mapping.mongo.config.MongoCollection

class MongoEntityConfigSpec extends GormDatastoreSpec {

    @IgnoreIf({ System.getenv('TRAVIS_BRANCH') != null })
    def 'Test custom collection config'() {
        given:
        session.mappingContext.addPersistentEntity MyMongoEntity

        def client = (MongoClient) session.nativeInterface
        MongoDatabase db = client.getDatabase(session.defaultDatabase)

        db.drop()
        // db.resetIndexCache() // this method is missing from more recent driver versions

        when:
        PersistentEntity entity = session.mappingContext.getPersistentEntity(MyMongoEntity.name)

        then:
        entity instanceof DocumentPersistentEntity

        when:
        MongoCollection coll = entity.mapping.mappedForm
        MongoAttribute attr = entity.getPropertyByName('name').getMapping().getMappedForm()
        MongoAttribute location = entity.getPropertyByName('location').getMapping().getMappedForm()
        then:
        coll != null
        coll.collection == 'mycollection'
        coll.database == 'test2'
        coll.writeConcern == WriteConcern.JOURNALED
        attr != null
        attr.index == true
        attr.targetName == 'myattribute'
        attr.indexAttributes == [unique: true]
        location != null
        location.index == true
        location.indexAttributes == [type: '2d']
        coll.indices.size() == 1
        coll.indices[0].definition == [summary: 'text']

        when:
        AbstractMongoSession ms = session
        then:
        ms.getCollectionName(entity) == 'mycollection'
    }

}

@Entity
class MyMongoEntity implements MongoEntity<MyMongoEntity> {

    String id

    String name
    String location
    String summary

    static mapping = {
        collection 'mycollection'
        database 'test2'
        shard 'name'
        writeConcern WriteConcern.JOURNALED
        index summary: 'text'

        name index: true, attr: 'myattribute', indexAttributes: [unique: true]

        location geoIndex: true
    }

}
