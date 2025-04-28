[![Main branch build status](https://github.com/graceframework/grace-data-mongodb/workflows/Grace%20CI/badge.svg?style=flat)](https://github.com/graceframework/grace-data-mongodb/actions?query=workflow%3A%Grace+CI%22)
[![Apache 2.0 license](https://img.shields.io/badge/License-APACHE%202.0-green.svg?logo=APACHE&style=flat)](https://opensource.org/licenses/Apache-2.0)
[![Latest version on Maven Central](https://img.shields.io/maven-central/v/org.graceframework.plugins/mongodb.svg?label=Maven%20Central&logo=apache-maven&style=flat)](https://search.maven.org/search?q=g:org.graceframework.plugins)
[![Grace on X](https://img.shields.io/twitter/follow/graceframework?style=social)](https://x.com/graceframework)

[![Groovy Version](https://img.shields.io/badge/Groovy-4.0.26-blue?style=flat&color=4298b8)](https://groovy-lang.org/releasenotes/groovy-4.0.html)
[![Grace Version](https://img.shields.io/badge/Grace-2023.3.0-blue?style=flat&color=f49b06)](https://github.com/graceframework/grace-framework/releases/tag/v2023.3.0-M2)
[![Spring Boot Version](https://img.shields.io/badge/Spring_Boot-3.3.10-blue?style=flat&color=6db33f)](https://github.com/spring-projects/spring-boot/releases/tag/v3.3.10)

# Grace Data for MongoDB

This project implements [GORM](https://github.com/graceframework/grace-data) for the [MongoDB Document Database](https://www.mongodb.com).

This project aims to provide an Object-Mapping layer on top of MongoDB to ease common activities such as:

* Marshalling from Mongo to Groovy/Java types and back again
* Support for GORM dynamic finders, criteria and named queries
* Session-managed transactions
* Validating domain instances backed by the Mongo datastore

> [!IMPORTANT]
> Currently, this plugin has been upgraded to MongoDB Driver 5.0.1, but only support Grace Framework 2023.3.x. 


```gradle
dependencies {
    implementation "org.graceframework.plugins:mongodb"
}
```

## Versions

To make it easier for users to use and upgrade, Grace Data MongoDB adopts a version policy consistent with the [Grace Framework](https://github.com/graceframework/grace-framework).

| GORM MongoDb Version   | Grace Version |
|------------------------|---------------|
| 2023.3.x               | 2023.3.x      |

## License

This plugin is available as open source under the terms of the [APACHE LICENSE, VERSION 2.0](http://apache.org/Licenses/LICENSE-2.0)

## Links

- [Grace Framework](https://github.com/graceframework/grace-framework)
- [Grace Data](https://github.com/graceframework/grace-data)
- [Grace Data Hibernate](https://github.com/graceframework/grace-data-hibernate)
- [MongoDB Database](https://www.mongodb.com)
