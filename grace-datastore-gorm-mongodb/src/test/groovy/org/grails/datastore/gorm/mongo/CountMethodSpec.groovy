package org.grails.datastore.gorm.mongo

import de.flapdoodle.embed.mongo.commands.ServerAddress
import de.flapdoodle.embed.mongo.distribution.Version
import de.flapdoodle.embed.mongo.transitions.ImmutableMongod
import de.flapdoodle.embed.mongo.transitions.Mongod
import de.flapdoodle.embed.mongo.transitions.RunningMongodProcess
import de.flapdoodle.reverse.TransitionWalker

import grails.gorm.annotation.Entity
import grails.gorm.tests.Plant
import grails.mongodb.MongoEntity

import org.grails.datastore.mapping.config.Settings
import org.grails.datastore.mapping.mongo.MongoDatastore
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

import org.grails.datastore.mapping.mongo.config.MongoSettings

import static com.mongodb.client.model.Filters.*
/**
 * Created by graemerocher on 29/11/2016.
 */
class CountMethodSpec extends Specification {

    @Shared
    @AutoCleanup
    MongoDatastore datastore

    @Shared
    protected TransitionWalker.ReachedState<RunningMongodProcess> running

    @Shared
    protected ServerAddress serverAddress

    void setupSpec() {
        ImmutableMongod mongodbConfig = Mongod.instance()
        Version.Main version = Version.Main.V7_0

        this.running = mongodbConfig.start(version)
        this.serverAddress = running.current().getServerAddress()

        Map config = [
                (Settings.SETTING_FAIL_ON_ERROR): true,
                (MongoSettings.SETTING_URL)     : "mongodb://$serverAddress".toString()
        ]
        this.datastore = new MongoDatastore(config, [CountTest] as Class[])
    }

    void cleanupSpec() {
        this.serverAddress = null
        if (this.running != null) {
            this.running.close()
        }
        this.running = null
        this.datastore.close()
    }

    void "test count method"() {
        given:"some test data "
        CountTest.DB.drop()
        CountTest.withNewSession {
            new CountTest(name: "foo").save()
            new CountTest(name: "bar").save(flush:true)
        }

        expect:
        CountTest.find(eq("name", "foo"))
        CountTest.count(eq("name", "foo")) == 1
    }
}

@Entity
class CountTest implements MongoEntity<CountTest> {
    String name

}
