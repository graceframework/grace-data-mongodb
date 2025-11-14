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

import com.mongodb.client.MongoCollection
import spock.lang.IgnoreIf

import grails.gorm.tests.GormDatastoreSpec
import grails.mongodb.MongoEntity

@IgnoreIf({ System.getenv('TRAVIS_BRANCH') != null })
class MongoGormEnhancerSpec extends GormDatastoreSpec {

    def 'Test is MongoEntity'() {
        expect:
        MongoEntity.isAssignableFrom(MyMongoEntity)
    }

    def 'Test getCollectionName static method'() {
        when:
        def collectionName = MyMongoEntity.collectionName

        then:
        collectionName == 'mycollection'
    }

    def 'Test getCollection static method'() {
        when:
        MongoCollection collection = MyMongoEntity.collection

        then:
        collection.namespace.collectionName == 'mycollection'
    }

    @Override
    List getDomainClasses() {
        [MyMongoEntity]
    }

}
