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
import org.bson.types.ObjectId

import grails.gorm.tests.GormDatastoreSpec
import grails.persistence.Entity

/**
 * Created by Jim on 8/15/2016.
 */
class EmbeddedCollectionWithIdSpec extends GormDatastoreSpec {

    void 'test embedded collection with IDs set reads and saves correctly'() {
        given:
        ObjectId barId = new ObjectId()
        MainUser.collection.insertOne(
                new Document([_id: new ObjectId(), name: 'Sally', bars: [[_id: barId, type: 'Foo']]]))
        session.clear()
        MainUser mainUser

        when:
        mainUser = MainUser.findByName('Sally')
        mainUser.name = 'Joe'
        EmbeddedBar bar = mainUser.bars.find { it.id.toString() == barId.toString() }
        bar.type = 'Bar'

        then:
        mainUser.save(flush: true, failOnError: true)

        when:
        session.clear()
        mainUser = MainUser.findByName('Joe')

        then:
        mainUser.name == 'Joe'
        mainUser.bars.size() == 1
        mainUser.bars[0].type == 'Bar'
        mainUser.bars[0].id == barId
    }

    @Override
    List getDomainClasses() {
        [MainUser, EmbeddedBar]
    }

}

@Entity
class MainUser {

    ObjectId id
    String name

    static embedded = ['bars']
    static hasMany = [bars: EmbeddedBar]
    static mapping = {
        version false
    }

}

@Entity
class EmbeddedBar {

    ObjectId id
    String type

}
