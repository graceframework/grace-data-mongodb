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

import com.mongodb.WriteConcern
import spock.lang.Issue

import grails.gorm.tests.GormDatastoreSpec
import grails.persistence.Entity

import static grails.mongodb.mapping.MappingBuilder.document

/**
 * Tests usage of WriteConcern
 */
class WriteConcernSpec extends GormDatastoreSpec {

    @Override
    List getDomainClasses() {
        [SafeWrite, UnacknowledgedWrite]
    }

    void 'Test that the correct WriteConcern is used to save entities'() {
        when: 'An object is saved'
        def sw = new SafeWrite(name: 'Bob')
        sw.save(flush: true)

        then: 'The correct write concern is used'
        sw != null
    }

    @Issue('https://github.com/grails/grails-data-mapping/issues/600')
    void 'Test unacknowledged write concern'() {
        when: 'An object is saved'
        def sw = new UnacknowledgedWrite(name: 'Bob')
        sw.save(flush: true)

        then: 'The correct write concern is used'
        sw != null

        when: 'The object is updated'
        session.clear()
        sw.name = 'Fred'
        sw.save(flush: true)
        session.clear()

        then: 'The update worked'
        UnacknowledgedWrite.findByName 'Fred'
    }

}

@Entity
class SafeWrite {

    String id
    String name

    static mapping = document {
        writeConcern WriteConcern.JOURNALED
    }

}

@Entity
class UnacknowledgedWrite {

    String id
    String name

    static mapping = document {
        writeConcern WriteConcern.UNACKNOWLEDGED
    }

}
