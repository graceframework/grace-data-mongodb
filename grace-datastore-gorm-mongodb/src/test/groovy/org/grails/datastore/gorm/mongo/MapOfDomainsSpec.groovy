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

import groovy.transform.EqualsAndHashCode
import org.bson.types.ObjectId

import grails.gorm.annotation.Entity
import grails.gorm.tests.GormDatastoreSpec

/**
 * Created by graemerocher on 22/04/14.
 */
class MapOfDomainsSpec extends GormDatastoreSpec {

    void 'Test that a map of embedded objects can be persisted'() {
        when: 'A domain class with a map of embedded objects is persisted'
        def phones = new Smartphones()

        def data = [apple: new Smartphone(name: 'iPhone'), samsung: new Smartphone(name: 'Galaxy')]
        phones.phonesByManufacturer = data
        phones.save(flush: true)
        session.clear()
        phones = Smartphones.get(phones.id)

        then: 'The results are correct'
        phones.phonesByManufacturer == data
    }

    @Override
    List getDomainClasses() {
        [Smartphones]
    }

}

@Entity
class Smartphones {

    ObjectId id
    Map<String, Smartphone> phonesByManufacturer

    static embedded = ['phonesByManufacturer']

}

@EqualsAndHashCode
class Smartphone {

    String name

}
