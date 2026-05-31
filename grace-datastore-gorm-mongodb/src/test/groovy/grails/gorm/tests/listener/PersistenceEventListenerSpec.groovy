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
package grails.gorm.tests.listener

import org.springframework.context.ApplicationEvent

import grails.gorm.DetachedCriteria
import grails.gorm.annotation.Entity
import grails.gorm.tests.GormDatastoreSpec

import org.grails.datastore.mapping.core.Datastore
import org.grails.datastore.mapping.engine.event.AbstractPersistenceEvent
import org.grails.datastore.mapping.engine.event.AbstractPersistenceEventListener
import org.grails.datastore.mapping.engine.event.EventType
import org.grails.datastore.mapping.engine.event.PostDeleteEvent
import org.grails.datastore.mapping.engine.event.PreDeleteEvent
import org.grails.datastore.mapping.engine.event.ValidationEvent

/**
 * @author Tom Widmer
 */
class PersistenceEventListenerSpec extends GormDatastoreSpec {

    SpecPersistenceListener listener

    @Override
    List getDomainClasses() {
        [Simples]
    }

    @Override
    def setup() {
        listener = new SpecPersistenceListener(session.datastore)
        session.datastore.applicationEventPublisher.addApplicationListener(listener)
    }

    void 'Test delete events'() {
        given:
        def p = new Simples()
        p.name = 'Fred'
        p.save(flush: true)
        session.clear()

        when:
        p = Simples.get(p.id)

        then:
        listener.PreDeleteCount == 0
        listener.PostDeleteCount == 0

        when:
        p.delete(flush: true)

        then:
        listener.PreDeleteCount == 1
        listener.PostDeleteCount == 1
        listener.events.size() > 0
        listener.events[-1].entityObject == p
        listener.events[-1].eventType == EventType.PostDelete
        listener.events[-1] instanceof PostDeleteEvent
        listener.events[-2].eventType == EventType.PreDelete
        listener.events[-2] instanceof PreDeleteEvent
    }

    void 'Test multi-delete events'() {
        given:
        def freds = (1..3).collect {
            new Simples(name: "Fred$it").save(flush: true)
        }
        session.clear()

        when:
        freds = Simples.findAllByIdInList(freds*.id)

        then:
        freds.size() == 3
        listener.PreDeleteCount == 0
        listener.PostDeleteCount == 0

        when:
        new DetachedCriteria(Simples).build {
            'in'('id', freds*.id)
        }.deleteAll()
        session.flush()

        then:
        Simples.count() == 0
        Simples.list().size() == 0

        // conditional assertions because in the case of batch DML statements neither Hibernate nor JPA triggers
        // delete events for individual entities
        if (!session.getClass().simpleName in ['JpaSession', 'HibernateSession']) {
            listener.PreDeleteCount == 3
            listener.PostDeleteCount == 3
        }
    }

    void 'Test update events'() {
        given:
        def p = new Simples()

        p.name = 'Fred'
        p.save(flush: true)
        session.clear()

        when:
        p = Simples.get(p.id)

        then:
        p.name == 'Fred'
        listener.PreUpdateCount == 0
        listener.PostUpdateCount == 0

        when:
        p.name = 'Bob'
        p.save(flush: true)
        session.clear()
        p = Simples.get(p.id)

        then:
        p.name == 'Bob'
        listener.PreUpdateCount == 1
        listener.PostUpdateCount == 1
    }

    void 'Test insert events'() {
        given:
        def p = new Simples()

        p.name = 'Fred'
        p.save(flush: true)
        session.clear()

        when:
        p = Simples.get(p.id)

        then:
        p.name == 'Fred'
        listener.PreUpdateCount == 0
        listener.PreInsertCount == 1
        listener.PostUpdateCount == 0
        listener.PostInsertCount == 1

        when:
        p.name = 'Bob'
        p.save(flush: true)
        session.clear()
        p = Simples.get(p.id)

        then:
        p.name == 'Bob'
        listener.PreUpdateCount == 1
        listener.PreInsertCount == 1
        listener.PostUpdateCount == 1
        listener.PostInsertCount == 1
    }

    void 'Test load events'() {
        given:
        def p = new Simples()

        p.name = 'Fred'
        p.save(flush: true)
        session.clear()

        when:
        p = Simples.get(p.id)

        then:
        p.name == 'Fred'
        if (!'JpaSession'.equals(session.getClass().simpleName)) {
            // JPA doesn't seem to support a pre-load event
            listener.PreLoadCount == 1
        }
        listener.PostLoadCount == 1
    }

    void 'Test multi-load events'() {
        given:
        def freds = (1..3).collect {
            new Simples(name: "Fred$it").save(flush: true)
        }
        session.clear()

        when:
        freds = Simples.findAllByIdInList(freds*.id)
        for (f in freds) {
        } // just to trigger load

        then:
        freds.size() == 3
        if (!'JpaSession'.equals(session.getClass().simpleName)) {
            // JPA doesn't seem to support a pre-load event
            listener.PreLoadCount == 3
        }
        listener.PostLoadCount == 3
    }

    void 'Test validation events'() {
        given:
        def p = new Simples()

        p.name = 'Fred'

        when:
        p.validate()

        then:
        listener.ValidationCount == 1
        listener.events.size() == 1
        listener.events[0].entityObject == p
        listener.events[0] instanceof ValidationEvent
        listener.events[0].validatedFields == null

        when:
        p.name = null
        p.validate(['name'])

        then:
        listener.ValidationCount == 2
        listener.events.size() == 2
        listener.events[1].entityObject == p
        listener.events[1] instanceof ValidationEvent
        listener.events[1].validatedFields == ['name']
    }

}

class SpecPersistenceListener extends AbstractPersistenceEventListener {

    SpecPersistenceListener(Datastore datastore) {
        super(datastore)
    }

    List<AbstractPersistenceEvent> events = []

    int PreDeleteCount
    int PreInsertCount
    int PreUpdateCount
    int PostUpdateCount
    int PostDeleteCount
    int PostInsertCount
    int PreLoadCount
    int PostLoadCount
    int SaveOrUpdateCount
    int ValidationCount

    @Override
    protected void onPersistenceEvent(AbstractPersistenceEvent event) {
        String typeName = event.eventType.name()
        this."${typeName}Count"++
        events << event
    }

    @Override
    boolean supportsEventType(Class<? extends ApplicationEvent> eventType) {
        true
    }

}

@Entity
class Simples implements Serializable {

    Long id
    String name

}
