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
package org.grails.datastore.mapping.mongo.config

import java.lang.reflect.Modifier

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.MongoCredential
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.core.env.PropertyResolver
import org.springframework.util.ReflectionUtils

/**
 * Helper class for building {@link MongoClientSettings} from a {@link PropertyResolver}
 *
 * @author Graeme Rocher
 * @since 6.0
 */
@CompileStatic
@Slf4j
class MongoClientOptionsBuilder {

    final PropertyResolver propertyResolver
    final String databaseName

    private String prefix = MongoSettings.SETTING_OPTIONS

    private ConnectionString connectionString
    private String host
    private String username
    private String password
    private String uAndP
    private MongoCredential mongoCredential

    MongoClientOptionsBuilder(PropertyResolver propertyResolver) {
        this(propertyResolver, propertyResolver.getProperty(MongoSettings.SETTING_DATABASE_NAME, 'test'))
    }

    MongoClientOptionsBuilder(PropertyResolver propertyResolver, String databaseName) {
        this.propertyResolver = propertyResolver
        this.databaseName = databaseName

        this.host = propertyResolver.getProperty(MongoSettings.SETTING_HOST, '')
        this.username = propertyResolver.getProperty(MongoSettings.SETTING_USERNAME, '')
        this.password = propertyResolver.getProperty(MongoSettings.SETTING_PASSWORD, '')
        this.uAndP = this.username && this.password ? "$username:$password@" : ''
        if (this.host) {
            def port = propertyResolver.getProperty(MongoSettings.SETTING_PORT, '')
            port = port ? ":$port" : ''
            this.connectionString = new ConnectionString("mongodb://${uAndP}${host}${port}/$databaseName")
        }
        else {
            this.connectionString = new ConnectionString(
                    propertyResolver.getProperty(MongoSettings.SETTING_CONNECTION_STRING, propertyResolver.getProperty(
                            MongoSettings.SETTING_URL, "mongodb://localhost/$databaseName")))
        }
        this.mongoCredential = this.uAndP ? MongoCredential.createCredential(this.username, databaseName,
                this.password.toCharArray()) : null
    }

    MongoClientSettings.Builder build() {
        MongoClientSettings.Builder builder = MongoClientSettings.builder()
        buildInternal(builder, this.prefix, true)
        return builder
    }

    private Object buildInternal(Object builder, String startingPrefix, boolean root = false) {
        def builderClass = builder.getClass()
        def methods = builderClass.declaredMethods

        def applyConnectionStringMethod = ReflectionUtils.findMethod(builderClass, 'applyConnectionString',
                ConnectionString)
        if (applyConnectionStringMethod != null) {
            applyConnectionStringMethod.invoke(builder, this.connectionString)
        }

        if (this.mongoCredential != null) {
            def credentialListMethod = ReflectionUtils.findMethod(builderClass, 'credentialList', List)
            if (credentialListMethod != null) {
                credentialListMethod.invoke(builder, Arrays.asList(this.mongoCredential))
            }
        }

        for (method in methods) {
            def methodName = method.name
            if (!Modifier.isPublic(method.modifiers) || methodName.equals('applyConnectionString') ||
                    methodName.equals('credentialList')) {
                continue
            }

            def parameterTypes = method.parameterTypes
            if (parameterTypes.length == 1) {
                Class argType = parameterTypes[0]

                def builderMethod = ReflectionUtils.findMethod(argType, 'builder')
                String propertyPath = "${startingPrefix}.${methodName}"
                if (builderMethod != null && Modifier.isStatic(builderMethod.modifiers)) {
                    if (this.propertyResolver.containsProperty(propertyPath)) {
                        def newBuilder = builderMethod.invoke(argType)
                        if (newBuilder.respondsTo('applyConnectionString')) {
                            applyConnectionString(newBuilder, this.connectionString)
                        }
                        method.invoke(builder, buildInternal(newBuilder, propertyPath))
                    }
                }
                else {
                    if (argType.isEnum()) {
                        def value = propertyResolver.getProperty(propertyPath, '')
                        if (value) {
                            try {
                                method.invoke(builder, Enum.valueOf((Class) argType, value))
                            }
                            catch (Throwable e) {
                                log.warn("Error occurred reading setting [$propertyPath]: ${e.message}", e)
                            }
                        }
                        continue
                    }
                    def valueOfMethod = ReflectionUtils.findMethod(argType, 'valueOf')
                    if (valueOfMethod != null && Modifier.isStatic(valueOfMethod.modifiers)) {
                        try {
                            def value = propertyResolver.getProperty(propertyPath, '')
                            if (value) {
                                def converted = valueOfMethod.invoke(argType, value)
                                method.invoke(builder, converted)
                            }
                        }
                        catch (e) {
                            log.warn("Error occurred reading setting [$propertyPath]: ${e.message}", e)
                        }
                    }
                    else if (!List.isAssignableFrom(argType)) {
                        try {
                            def value = propertyResolver.getProperty(propertyPath, (Class) argType, null)
                            if (value != null) {
                                method.invoke(builder, value)
                            }
                        }
                        catch (Throwable e) {
                            log.warn("Error occurred reading setting [$propertyPath]: ${e.message}", e)
                        }
                    }
                }
            }
        }

        if (!root) {
            return doBuild(builder)
        }
        else {
            return builder
        }
    }

    @CompileDynamic
    private void applyConnectionString(newBuilder, ConnectionString connectionString) {
        newBuilder.applyConnectionString(connectionString)
    }

    @CompileDynamic
    private Object doBuild(builder) {
        builder.build()
    }

}
