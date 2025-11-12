package org.grails.datastore.gorm.mongo.connections

import de.flapdoodle.embed.mongo.commands.ServerAddress
import de.flapdoodle.embed.mongo.distribution.Version
import de.flapdoodle.embed.mongo.transitions.ImmutableMongod
import de.flapdoodle.embed.mongo.transitions.Mongod
import de.flapdoodle.embed.mongo.transitions.RunningMongodProcess
import de.flapdoodle.reverse.TransitionWalker

import grails.gorm.MultiTenant
import grails.gorm.annotation.Entity
import grails.mongodb.MongoEntity
import org.bson.types.ObjectId
import org.grails.datastore.gorm.mongo.City
import org.grails.datastore.mapping.core.Session
import org.grails.datastore.mapping.mongo.MongoDatastore
import org.grails.datastore.mapping.mongo.config.MongoSettings
import org.grails.datastore.mapping.multitenancy.AllTenantsResolver
import org.grails.datastore.mapping.multitenancy.exceptions.TenantNotFoundException
import org.grails.datastore.mapping.multitenancy.resolvers.SystemPropertyTenantResolver
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification
import static com.mongodb.client.model.Filters.*;
/**
 * Created by graemerocher on 13/07/2016.
 */
class MultiTenancySpec extends Specification {

    @AutoCleanup
    @Shared
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
                "grails.gorm.multiTenancy.mode"               :"DISCRIMINATOR",
                "grails.gorm.multiTenancy.tenantResolverClass": MyResolver,
                (MongoSettings.SETTING_URL)                   : "mongodb://$serverAddress/defaultDb".toString(),
        ]
        this.datastore = new MongoDatastore(config, getDomainClasses() as Class[])
    }

    void setup() {
        System.setProperty(SystemPropertyTenantResolver.PROPERTY_NAME, "")
    }

    void cleanupSpec() {
        this.serverAddress = null
        if (this.running != null) {
            this.running.close()
        }
        this.running = null
        this.datastore.close()
    }

    void "Test persist and retrieve entities with multi tenancy"() {
        setup:
        CompanyC.DB.drop()

        when:"A tenant id is present"
        System.setProperty(SystemPropertyTenantResolver.PROPERTY_NAME, "test1")

        then:"the correct tenant is used"
        CompanyC.count() == 0
        CompanyC.DB.name == 'defaultDb'

        when:"An object is saved"
        new CompanyC(name: "Foo").save(flush:true)

        then:"The results are correct"
        CompanyC.count() == 1

        when:"An object is updated"
        CompanyC c = CompanyC.findByName("Foo")
        c.name = "Bar"
        c.save(flush:true)

        then:
        !CompanyC.findByName("Foo")
        CompanyC.findByName("Bar")?.version == 1

        when:"The tenant id is switched"
        System.setProperty(SystemPropertyTenantResolver.PROPERTY_NAME, "test2")

        then:"the correct tenant is used"
        CompanyC.DB.name == 'defaultDb'
        CompanyC.count() == 0
        !CompanyC.find(eq("name", "Foo")).first()
        !CompanyC.find(eq("name", "Bar")).first()
        CompanyC.withTenant("test1") { Serializable tenantId, Session s ->
            assert tenantId
            assert s
            CompanyC.count() == 1
        }

        when:"each tenant is iterated over"
        Map tenantIds = [:]
        CompanyC.eachTenant { String tenantId ->
            tenantIds.put(tenantId, CompanyC.count())
        }

        then:"The result is correct"
        tenantIds == [test1:1, test2:0]
    }

    List getDomainClasses() {
        [City, CompanyC]
    }

    static class MyResolver extends SystemPropertyTenantResolver implements AllTenantsResolver {
        @Override
        Iterable<Serializable> resolveTenantIds() {
            ['test1','test2']
        }
    }

}
@Entity
class CompanyC implements MongoEntity<CompanyC>, MultiTenant {
    ObjectId id
    String name
    String parent

    static mapping = {
        tenantId name:'parent'
    }

}