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

import jakarta.persistence.FlushModeType

import spock.lang.Issue

import grails.gorm.annotation.Entity
import grails.gorm.tests.GormDatastoreSpec

import org.grails.datastore.mapping.core.OptimisticLockingException

/**
 * @author Graeme Rocher
 */
class OptimisticLockingWithExceptionSpec extends GormDatastoreSpec {

    @Issue('GPMONGODB-256')
    void 'Test that when an optimistic locking exception is thrown the flush mode is set to commit'() {
        when: 'An optimistic locking session is thrown'
        Counter c = new Counter(counter: 0).save(flush: true)
        session.clear()

        then: 'The version is 1'
        c.version == 0

        when: 'The object is concurrently updated'
        c = Counter.get(c.id)
        Thread.start {
            Counter.withNewSession {
                Counter c1 = Counter.get(c.id)
                c1.counter++
                c1.save(flush: true)
            }
        }.join()
        c.counter = 2
        c.save(flush: true)

        then: 'An optimistic locking exception was thrown'
        thrown(OptimisticLockingException)
        session.flushMode == FlushModeType.COMMIT
    }

    @Override
    List getDomainClasses() {
        [Counter]
    }

}

@Entity
class Counter {

    Long id
    Long version
    Date lastUpdated
    Date dateCreated
    int counter

}
