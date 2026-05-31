/*
 * Copyright 2016-2026 the original author or authors.
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
import org.bson.types.ObjectId
import spock.lang.Issue

import grails.gorm.annotation.Entity
import grails.gorm.tests.GormDatastoreSpec

/**
 * @author Graeme Rocher
 */
class MongoDynamicPropertyOnEmbeddedSpec extends GormDatastoreSpec {

    @Issue('GPMONGODB-290')
    void 'Test that accessing dynamic attributes on embedded objects use the embedded collection'() {
        when: 'An embedded collection is created'
        Container.collection.insertOne(new Document(name: 'big box of items',
                contents: (0..9).collect { [name: "Item $it"] }))
        def collectionNames = Container.DB.listCollectionNames().sort()

        then: 'The embedded collection is valid'
        Container.count() == 1
        Container.first().contents.size() == 10
        Container.first().contents.first().name ==~ /Item \d/
        collectionNames.any { it =~ /^container\b/ }
        !collectionNames.any { it =~ /^item\b/ }
        session.clear()

        when: 'An embedded dynamic property is accessed'
        Container.first().contents.first().nonexistentProperty == null
        then: 'A collection is not created for the embedded property'
        Container.DB.listCollectionNames().sort() == collectionNames
        !collectionNames.any { it =~ /^item\b/ }
    }

    @Override
    List getDomainClasses() {
        [Container]
    }

}

@Entity
class Container {

    ObjectId id
    String name

    Set<Item> contents = new HashSet<Item>()

    static mapWith = 'mongo'
    static embedded = ['contents']
    static mapping = {
        version false
        cache false
    }

    static constraints = {
        contents nullable: true
    }

}

@Entity
class Item {

    ObjectId id
    String name

    static mapWith = 'mongo'

    static mapping = {
        version false
    }

    static constraints = {
    }

    static belongsTo = Container

}
