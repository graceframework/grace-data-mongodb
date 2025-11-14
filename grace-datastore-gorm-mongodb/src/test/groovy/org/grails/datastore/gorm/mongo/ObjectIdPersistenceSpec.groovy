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

import org.bson.types.ObjectId

import grails.gorm.tests.GormDatastoreSpec
import grails.persistence.Entity

class ObjectIdPersistenceSpec extends GormDatastoreSpec {

    def 'Test that we can persist an object that has a BSON ObjectId'() {
        when:
        def t = new MongoObjectIdEntity(name: 'Bob').save(flush: true)
        session.clear()
        t = MongoObjectIdEntity.get(t.id)

        then:
        t != null
        t.id != null
    }

    @Override
    List getDomainClasses() {
        [MongoObjectIdEntity]
    }

}

@Entity
class MongoObjectIdEntity {

    ObjectId id

    String name

}
