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

import spock.lang.Issue

import grails.gorm.tests.GormDatastoreSpec
import grails.persistence.Entity

/**
 * Created by graemerocher on 21/04/16.
 */
class EventsWithAbstractInheritanceSpec extends GormDatastoreSpec {

    @Issue('https://github.com/grails/grails-data-mapping/issues/701')
    def 'Test that events work with abstract inheritance'() {
        when: 'An entity is saved'
        ConcreteEventDomain ced = new ConcreteEventDomain(name: 'Bob').save(flush: true)

        then: 'An event listener inherited from the base class is fired'
        ced.eventCount('beforeInsert') == 1

        when: 'An an instance is updated'
        ced.name = 'Fred'
        ced.save(flush: true)

        then: 'The beforeUpdate event listener is fired'
        ced.eventCount('beforeInsert') == 1
        ced.eventCount('beforeUpdate') == 1

        when: 'An an instance is updated again'
        ced.name = 'Joe'
        ced.save(flush: true)

        then: 'The beforeUpdate event listener is fired'
        ced.eventCount('beforeInsert') == 1
        ced.eventCount('beforeUpdate') == 2
    }

    @Override
    List getDomainClasses() {
        [ConcreteEventDomain]
    }

}

@Entity
class ConcreteEventDomain extends AbstractEventDomain {

    String name

}

abstract class AbstractEventDomain {

    private final Map<String, Integer> eventCount = [:].withDefault {
        0
    }

    int eventCount(String name) {
        eventCount[name]
    }

    boolean beforeInsert() {
        eventCount['beforeInsert']++
        return true
    }

    boolean beforeUpdate() {
        eventCount['beforeUpdate']++
        return true
    }

}
