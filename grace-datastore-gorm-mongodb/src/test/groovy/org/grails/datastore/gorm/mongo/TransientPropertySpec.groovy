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

import grails.gorm.annotation.Entity
import grails.gorm.tests.GormDatastoreSpec

class TransientPropertySpec extends GormDatastoreSpec {

    void 'Test that transient properties are not saved to mongodb'() {
        when: 'A doman with a transient property is saved'
        def c = new Cow(name: 'Daisy', other: 'foo').save(flush: true)
        def service = c.rodeoService
        session.clear()
        c = Cow.findByName('Daisy')

        then: 'The transient instance is not persisted'
        c != null
        c.name == 'Daisy'
        c.other == null
        c.rodeoService != null
        c.rodeoService != service
    }

    @Override
    List getDomainClasses() {
        [Cow]
    }

}

class RodeoService {

}

@Entity
class Cow {

    Long id
    String name
    transient String other
    transient rodeoService = new RodeoService()

}
