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

import org.bson.types.ObjectId

import grails.gorm.annotation.Entity
import grails.gorm.dirty.checking.DirtyCheck
import grails.gorm.tests.GormDatastoreSpec

import org.grails.datastore.mapping.mongo.config.MongoSettings

class MarkDirtyFalseSpec extends GormDatastoreSpec {

    Map getConfiguration() {
        [(MongoSettings.SETTING_MARK_DIRTY): false]
    }

    void 'test behavior with mark dirty false'() {
        when:
        def b = new Bar(foo: 'stuff', strings: ['a', 'b'])
        b.save(flush: true)
        session.clear()
        b = Bar.get(b.id)
        b.save(flush: true)

        then:
        b.version == 0

        when:
        def bTs = new BarWithTimestamp(foo: 'stuff')
        bTs.save(flush: true)
        session.clear()
        bTs = BarWithTimestamp.get(bTs.id)
        bTs.save(flush: true)

        then:
        bTs.version == 0
    }

    @Override
    List getDomainClasses() {
        [Bar, BarWithTimestamp]
    }

}

@Entity
@DirtyCheck
class BarWithTimestamp {

    ObjectId id

    String foo

    Date lastUpdated

}
