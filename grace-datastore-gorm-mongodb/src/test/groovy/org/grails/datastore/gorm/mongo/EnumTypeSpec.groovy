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

import jakarta.persistence.EnumType

import org.bson.types.ObjectId

import grails.gorm.tests.GormDatastoreSpec
import grails.persistence.Entity

/**
 * Created by graemerocher on 06/05/14.
 */
class EnumTypeSpec extends GormDatastoreSpec {

    void 'Test ordinal mapping for enums'() {
        when: 'The enum type is obtained'
        def entity = session.mappingContext.getPersistentEntity(Dist.name)
        def propertyMapping = entity.getPropertyByName('unit').mapping.mappedForm

        then: 'It is ordinal'
        propertyMapping.enumTypeObject == EnumType.ORDINAL

        when: 'An ordinal mapped property is persisted'
        def d = new Dist(amount: 10, unit: Unit.KILOMETERS).save(flush: true)
        session.clear()

        then: 'The value is saved using ordinal value'
        Dist.collection.find().first().unit == 1

        when: 'An enum property mapped as ordinal is retrieved'
        d = Dist.get(d.id)

        then: 'The value is correctly converted'
        d.unit == Unit.KILOMETERS
    }

    @Override
    List getDomainClasses() {
        [Dist]
    }

}

enum Unit {

    MILES, KILOMETERS

}

@Entity
class Dist {

    ObjectId id
    int amount
    Unit unit

    static mapping = {
        unit enumType: 'ordinal'
    }

}
