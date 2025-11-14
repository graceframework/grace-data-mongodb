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

import grails.gorm.annotation.Entity
import grails.gorm.tests.GormDatastoreSpec
import grails.mongodb.MongoEntity
import grails.mongodb.geo.Point

import static grails.mongodb.mapping.MappingBuilder.document

/**
 * Created by graemerocher on 02/02/2017.
 */
class DocumentMappingSpec extends GormDatastoreSpec {

    void 'test custom document mapping'() {
        when: 'A document is saved with a custom mapping'
        new CustomMapping(name: 'test', loc: Point.valueOf(10, 15)).save(flush: true)
        Document doc = CustomMapping.collection.find().first()

        then:
        CustomMapping.collection.namespace.collectionName == 'mycoll'
        CustomMapping.collection.namespace.databaseName == 'mydb'
        doc.get('my_name') == 'test'
        doc.get('loc').inspect() == '[\'type\':\'Point\', \'coordinates\':[10.0, 15.0]]'
    }

    @Override
    List getDomainClasses() {
        [CustomMapping]
    }

}

@Entity
class CustomMapping implements MongoEntity<CustomMapping> {

    String name
    Point loc

    static mapping = document {
        collection 'mycoll'
        database 'mydb'
        name property {
            reference false
            attr 'my_name'
        }
        loc property {
            geoIndex '2dsphere'
        }
    }

}
