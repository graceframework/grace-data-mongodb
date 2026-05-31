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
import grails.gorm.tests.GormDatastoreSpec

class OneToOneNoReferenceSpec extends GormDatastoreSpec {

    void 'Test that associations can be saved with no dbrefs'() {
        when: 'A domain class is saved that has references disabled'
        def other = new OtherNoRef().save()
        def noref = new NoRef(other: other)
        noref.save flush: true

        then: 'The association is saved without a dbref'
        println NoRef.collection.find().first()
        NoRef.collection.find().first().other == other.id
    }

    void 'Test that querying an association works'() {
        when: 'A domain class is saved that has references disabled'
        def other = new OtherNoRef().save()
        def noref = new NoRef(other: other)
        noref.save flush: true
        session.clear()

        other = OtherNoRef.get(other.id)
        noref = NoRef.findByOther(other)

        then: 'The association can be queried'
        other != null
        noref != null
    }

    @Override
    List getDomainClasses() {
        [OtherNoRef, NoRef]
    }

}

@Entity
class NoRef {

    ObjectId id

    OtherNoRef other

    static mapping = {
        other reference: false
    }

}

@Entity
class OtherNoRef {

    ObjectId id

}
