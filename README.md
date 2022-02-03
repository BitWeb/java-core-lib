# BitWeb OÜ Core library

Given library provides basic functionality that is required by any HTTP web service.
Given library contains only generic functionality that is not specific to any field/subject. 

## Provided features 


### General HTTP web request error handling for Spring Boot applications. 

In order to enable standardised error handlers for HTTP web request hooks 
you simply need to include `ee.bitweb.core` package in component scanning.

### Trace ID generation and propagation

Given package contains Trace ID generation and propagation related features consolidated in `TraceIdFilter`
Simply include that filter in your filter chain and you will have a trace id included in MDC. 

## Usage

Add your credentials to `~/.gradle/gradle.properties`:

    nexusUsername=USERNAME
    nexusPassword=PASSWORD

Add BitWeb's private Maven repository to your build.gradle file

    repositories {
        mavenCentral()
        maven {
            url "https://nexus.bitweb.ee/repository/maven-private/"
            credentials {
                username = "${nexusUsername}"
                password = "${nexusPassword}"
            }
        }
    }

Add dependency to your project

### Production release

    // https://bitbucket.bitweb.ee/projects/BITWEB/repos/java-core-lib/browse
    implementation group: 'ee.bitweb', name: 'core', version: '1.0.0'

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
            url "https://nexus.bitweb.ee/repository/maven-private/"
            credentials {
                username = "${nexusUsername}"
                password = "${nexusPassword}"
            }
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

### 1.1.0-SNAPSHOT

* ...

### 1.0.0

* Initial version

