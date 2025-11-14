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

import grails.gorm.tests.GormDatastoreSpec
import grails.gorm.tests.Person

class NegateInListSpec extends GormDatastoreSpec {

    void 'Test negate in list query'() {
        given: 'two people'
        def p1 = new Person(firstName: 'Homer', lastName: 'Simpson').save()
        new Person(firstName: 'Bart', lastName: 'Simpson').save(flush: true)

        when: "We query for people who don't have the given id"
        def results = Person.withCriteria {
            not {
                inList('id', [p1.id])
            }
        }

        then: 'The results are correct'
        results.size() == 1
        results[0].firstName == 'Bart'
    }

}
