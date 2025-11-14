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

import org.bson.Document
import org.bson.types.Binary
import org.bson.types.ObjectId

import grails.gorm.tests.GormDatastoreSpec
import grails.persistence.Entity

class MongoTypesSpec extends GormDatastoreSpec {

    void 'Test that an entity can save and load native mongo types'() {
        when: 'A domain class with mongodb types is saved and read'
        def mt = new MongoTypes()
        mt.bson = new Document(foo: new Document([embedded: 'bar']))
        mt.binary = new Binary('foo'.bytes)
        def otherId = new ObjectId()
        mt.otherId = otherId
        mt.save flush: true
        session.clear()
        mt = MongoTypes.get(mt.id)
        then: 'Then it is in the correct state'
        mt != null
        mt.bson != null
        mt.bson.foo instanceof Document
        mt.bson.foo.embedded == 'bar'
        mt.binary.data == 'foo'.bytes
        mt.otherId == otherId
    }

    @Override
    List getDomainClasses() {
        [MongoTypes]
    }

}

@Entity
class MongoTypes {

    ObjectId id
    Document bson
    Binary binary
    ObjectId otherId

}
