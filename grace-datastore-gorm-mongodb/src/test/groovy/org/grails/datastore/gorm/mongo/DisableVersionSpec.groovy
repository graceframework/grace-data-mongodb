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

class DisableVersionSpec extends GormDatastoreSpec {

    void 'Test that disabling the version does not persist the version field'() {
        when: 'An object with a disabled version is persisted'
        def nv = new NoVersion(name: 'Bob').save(flush: true)
        session.clear()
        nv = NoVersion.findByName('Bob')

        then: 'The version field is not persisted'
        nv.name == 'Bob'
        nv.version == null
    }

    @Override
    List getDomainClasses() {
        [NoVersion]
    }

}

@Entity
class NoVersion {

    Long id
    Long version
    String name

    static mapping = {
        version false
    }

}
