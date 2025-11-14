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
import grails.persistence.Entity

/**
 * Created by graemerocher on 05/01/16.
 */
class InnerEnumSpec extends GormDatastoreSpec {

    void 'Test that inner enums are persisted'() {
        given:
        new InnerPerson(name: 'Fred', state: InnerPerson.PersonState.GOOD).save(flush: true)
        session.clear()

        expect:
        InnerPerson.first().state == InnerPerson.PersonState.GOOD
    }

    @Override
    List getDomainClasses() {
        [InnerPerson]
    }

}

@Entity
class InnerPerson {

    String name
    PersonState state

    static constraints = {
    }

    enum PersonState {

        GOOD, BAD

    }

}
