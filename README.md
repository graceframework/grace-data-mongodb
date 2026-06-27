[![Main branch build status](https://github.com/graceframework/grace-data-mongodb/workflows/Grace%20CI/badge.svg?style=flat)](https://github.com/graceframework/grace-data-mongodb/actions?query=workflow%3A%Grace+CI%22)
[![Apache 2.0 license](https://img.shields.io/badge/License-APACHE%202.0-green.svg?logo=APACHE&style=flat)](https://opensource.org/licenses/Apache-2.0)
[![Latest version on Maven Central](https://img.shields.io/maven-central/v/org.graceframework/grace-datastore-gorm-mongodb.svg?label=Maven%20Central&logo=apache-maven&style=flat)](https://search.maven.org/search?q=g:org.graceframework)
[![Grace Document](https://img.shields.io/badge/Grace_Document-latest-blue?style=flat&logo=asciidoctor&logoColor=E40046&labelColor=ffffff&color=f49b06)](https://graceframework.org/grace-data-mongodb/latest/)
[![Grace on X](https://img.shields.io/twitter/follow/graceframework?style=social)](https://x.com/graceframework)

[![Groovy Version](https://img.shields.io/badge/Groovy-4.0.32-blue?style=flat&color=4298b8)](https://groovy-lang.org/releasenotes/groovy-4.0.html)
[![Grace Version](https://img.shields.io/badge/Grace-2024.2.0-blue?style=flat&color=f49b06)](https://github.com/graceframework/grace-framework/releases/tag/v2024.2.0-M1)

# Grace Data MongoDB

This project implements [Grace Data](https://github.com/graceframework/grace-data) for the [MongoDB Document Database](https://www.mongodb.com).

This project aims to provide an Object-Mapping layer on top of MongoDB to ease common activities such as:

* Marshalling from Mongo to Groovy/Java types and back again
* Support for GORM dynamic finders, criteria and named queries
* Session-managed transactions
* Validating domain instances backed by the Mongo datastore

## Usage

```gradle
dependencies {
    implementation "org.graceframework:grace-boot-mongodb"
}
```

## Versions

To make it easier for users to use and upgrade, Grace Data MongoDB adopts a version policy consistent with the [Grace Framework](https://github.com/graceframework/grace-framework).

| GORM Version | Grace Version | MongoDB Version |
|--------------|---------------|-----------------|
| 2024.2.x     | 2024.2.x      | 5.5.2           |
| 2024.1.x     | 2024.1.x      | 5.5.2           |
| 2024.0.x     | 2024.0.x      | 5.2.1           |
| 2023.3.x     | 2023.3.x      | 5.0.1           |

## Ducumentation

* [2024.2.x](https://graceframework.org/grace-data-mongodb/2024.2.x/)

## License

This plugin is available as open source under the terms of the [APACHE LICENSE, VERSION 2.0](http://apache.org/Licenses/LICENSE-2.0)

## Links

- [Grace Framework](https://github.com/graceframework/grace-framework)
- [Grace Data](https://github.com/graceframework/grace-data)
- [Grace Data Hibernate](https://github.com/graceframework/grace-data-hibernate)
- [MongoDB Database](https://www.mongodb.com)
