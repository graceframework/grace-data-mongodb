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

import spock.lang.Issue

import grails.gorm.annotation.Entity
import grails.gorm.tests.GormDatastoreSpec

class ListOneToManyOrderingSpec extends GormDatastoreSpec {

    @Issue('GPMONGODB-162')
    void 'Test that one-to-many associations of type list retain ordering'() {
        given: 'A domain model that features a list association'
        def j = new Judge(name: 'Bob')
        j.jury = [
                new Juror(name: 'Fred'),
                new Juror(name: 'Joe'),
                new Juror(name: 'Bill')
        ]
        j.save flush: true
        session.clear()
        when: 'The domain model is queried'
        j = Judge.findByName('Bob')
        then: 'The list ordering is retained'
        j != null
        j.name == 'Bob'
        j.jury.size() == 3
        j.jury[0].name == 'Fred'
        j.jury[1].name == 'Joe'
        j.jury[2].name == 'Bill'
    }

    @Issue('GPMONGODB-162')
    void 'Test that one-to-many associations of type list retain ordering from existing entities'() {
        given: 'A domain model that features a list association'
        def j = new Judge(name: 'Bob')
        def joe = new Juror(name: 'Joe').save()
        def bill = new Juror(name: 'Bill').save()
        def fred = new Juror(name: 'Fred').save(flush: true)
        j.jury = [
                fred,
                joe,
                bill
        ]
        j.save flush: true
        session.clear()
        when: 'The domain model is queried'
        j = Judge.findByName('Bob')
        then: 'The list ordering is retained'
        j != null
        j.name == 'Bob'
        j.jury.size() == 3
        j.jury[0].name == 'Fred'
        j.jury[1].name == 'Joe'
        j.jury[2].name == 'Bill'
    }

    @Override
    List getDomainClasses() {
        [Judge, Juror]
    }

}

@Entity
class Judge {

    Long id
    String name
    List jury = []

    static hasMany = [jury: Juror]

}

@Entity
class Juror {

    Long id
    String name

}
