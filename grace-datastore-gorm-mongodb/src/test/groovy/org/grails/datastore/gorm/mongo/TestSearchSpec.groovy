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

import org.bson.types.ObjectId
import spock.lang.IgnoreIf

import grails.gorm.tests.GormDatastoreSpec
import grails.mongodb.MongoEntity
import grails.persistence.Entity

/**
 * Created by graemerocher on 14/04/14.
 */
// TODO: Remove IgnoreIf when travis supports MongoDB 2.6
@IgnoreIf({ System.getenv('TRAVIS_BRANCH') != null })
class TestSearchSpec extends GormDatastoreSpec {

    void 'Test simple text search'() {
        given: 'Some sample data'
        new Product(title: 'Italian Coffee').save()
        new Product(title: 'Arabian Coffee').save()
        new Product(title: 'Coffee Maker').save()
        new Product(title: 'Coffee Grinder').save()
        new Product(title: 'Coffee Cake').save()
        new Product(title: 'Apple Cake').save()
        new Product(title: 'Chocolate Cake').save()
        new Product(title: 'Cheese Bake').save()
        new Product(title: 'Bake a Cake').save()
        new Product(title: 'Potato Bake').save(flush: true)

        expect: 'The results are correct'
        Product.search('coffee').size() == 5
        Product.search('bake coffee cake').size() == 10
        Product.search('bake coffee -cake').size() == 6
        Product.search('"Coffee Cake"').size() == 1
        Product.searchTop('cake').size() == 4
        Product.searchTop('cake', 3).size() == 3
        Product.countHits('coffee') == 5
    }

    @Override
    List getDomainClasses() {
        [Product]
    }

}

@Entity
class Product implements MongoEntity<Product> {

    ObjectId id
    String title

    static mapping = {
        index title: 'text'
    }

    @Override
    String toString() {
        title
    }

}
