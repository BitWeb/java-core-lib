# BitWeb OÜ Core library

Given library provides basic functionality that is required by any HTTP web service.
Given library contains only generic functionality that is not specific to any field/subject. 

## Provided features 

On order to enable component scanning, include package in component scanning eg. `scanBasePackages = {"ee.bitweb.core", ...}`

### General HTTP web request error handling for Spring Boot applications. 

In order to enable standardised error handlers for HTTP web request hooks 
you simply need to include property `ee.bitweb.core.controller-advice.enabled=true` in application properties.

### Trace ID generation and propagation

Given package contains Trace ID generation and propagation related features consolidated in `TraceIdFilter`
Trace ID is easily autoconfigurable by adding `ee.bitweb.core.trace.auto-configuration=true` to application properties.

## Usage

Add BitWeb's private Maven repository to your build.gradle file

    repositories {
        mavenCentral()
        maven {
            url "https://nexus.bitweb.ee/repository/maven-public/"
        }
    }

Add dependency to your project

### Production release

    // https://bitbucket.bitweb.ee/projects/BITWEB/repos/java-core-lib/browse
    implementation group: 'ee.bitweb', name: 'core', version: '2.0.0'

### Current development version

All changes to master branch will be pushed to Maven repository as SNAPSHOT versions

    // https://bitbucket.bitweb.ee/projects/BITWEB/repos/java-core-lib/browse
    implementation group: 'ee.bitweb', name: 'core', version: '1.1.0-SNAPSHOT', changing: 'true'

## Development

Development of this library should be done using local Maven repository. Doing so is very easy, all you need to do is
add `mavenLocal()` as the first repository in repositories list, like so:

    repositories {
        mavenLocal()
        mavenCentral()
        maven {
            url "https://nexus.bitweb.ee/repository/maven-public/"
        }
    }

After this is done, you'll need to run `gradle install` in this project, to generate the jar file and add it to the
local Maven Repository in your machine. Then, in the project that's using this library, you need 
[reimport](https://www.jetbrains.com/help/idea/work-with-gradle-projects.html#gradle_refresh_project) the Gradle project.
In some cases reimport might not be needed.

All changes made to the public API or behaviour of methods, must be documented to the corresponding version paragraph in
release notes section of this document. Documentation must be done before making a pull request.

## Versioning

[Semver](http://semver.org) standard is used for versioning.
 
**major.minor.patch**

* **Major** - Backwards incompatible public API change
* **Minor** - New features added to public API, all changes are backwards compatible within current **Major** version
* **Patch** - Bug fixes. No features added to public API
* **SNAPSHOT** - Indicates version in progress. DO NOT use snapshot versions for production or staging

Versions will be released at least before any project that uses this library, moves to staging environment. 

## Docker

Dockerfile is only for running tests in Bamboo, this is a library **NOT** an application, so there is no main class to 
run.

## Release notes

### 2.0.1 

* SBA request actuator/health threw classdefNotFoundException in jackson serializer.
Had to downgrade kotlin version to 2.12.0.
Bugfix thread: https://github.com/FasterXML/jackson-module-kotlin/issues/523

### 1.1.0-SNAPSHOT

* ...

### 1.0.0

* Initial version

