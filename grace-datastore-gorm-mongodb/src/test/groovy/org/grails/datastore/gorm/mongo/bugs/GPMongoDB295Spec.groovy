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
package org.grails.datastore.gorm.mongo.bugs

import spock.lang.Issue

import grails.gorm.tests.GormDatastoreSpec
import grails.persistence.Entity

/**
 * @author Graeme Rocher
 */
class GPMongoDB295Spec extends GormDatastoreSpec {

    @Issue('GPMONGODB-295')
    void "Test that 'com.mongodb.DBRef cannot be cast to java.io.Serializable' exception is not thrown"() {
        given: 'Some test data'
        UserGroup group = new UserGroup(name: 'group', company: 'JFrog').save(flush: true, failOnError: true)
        User user = new User(lastName: 'lastName', name: 'user', group: group).save(flush: true, failOnError: true)
        UserObject obj = new UserObject(objName: 'obj').save(flush: true, failOnError: true)
        group.addToUsers(user)
        user.addToObjects(obj)
        user.save(flush: true, failOnError: true)
        group.save(failOnError: true, flush: true)

        expect: 'The exception is not thrown'
        !user.hasErrors()
        !group.hasErrors()
        getAllSavedDataWithANewSession()
    }

    private getAllSavedDataWithANewSession() {
        UserGroup.withNewSession {
            UserGroup userGroup = UserGroup.findByName('group')
            assert userGroup
            assert userGroup.users.size()
            User user = User.findByName('user')
            user.objects.size()
            assert user.objects.size() == 1
            return user
        }
    }

    @Override
    List getDomainClasses() {
        [InheritUser, ObjParent, UserGroup, User, UserObject]
    }

}

@Entity
class InheritUser {

    String id
    String name

    static constraints = {
        name unique: true, nullable: false
    }

}

@Entity
class ObjParent {

    String id
    String dateCreated
    String lastUpdated

}

@Entity
class UserGroup extends InheritUser {

    String id
    String company
    Set users

    static hasMany = [users: User]
    static mappedBy = [users: 'group']

    static constraints = {
        company nullable: false
    }

}

@Entity
class User extends InheritUser {

    String id
    String lastName
    Set objects
    UserGroup group

    static hasMany = [objects: UserObject]
    static belongsTo = [group: UserGroup]
    static constraints = {
        group nullable: false
        lastName nullable: false
    }

}

@Entity
class UserObject extends ObjParent {

    String id
    String objName
    String dateCreated
    String lastUpdated

    static constraints = {
        objName nullable: false, unique: true
    }

}
