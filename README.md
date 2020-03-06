# Core library for Command based services

This library is home for various common classes that can be used across services that use command pattern.

## Provided classes

### Web

* `ee.bitweb.core.api.AbstractController` - Provides handling of the following exceptions:
  * `HttpMessageNotReadableException`
  * `MethodArgumentNotValidException`
  * `BindException`
  * `ConstraintViolationException`
  * `EntityNotFoundException`
  * `ConflictException`
  * `Throwable`
* `ee.bitweb.core.api.model.error` contains response models for above mentioned exceptions
* `ee.bitweb.core.api.model.Response` should be used as the base class for all non-error responses
* `ee.bitweb.core.`

### Validators

* `@Uppercase` - validates that string contains only uppercase letters or numbers

### Exceptions

Package `ee.bitweb.core.exception`

* `BusinessException` - base exception for all exceptions that don't need to be checked (extends Java's `RuntimeException`)
* `CoreException` - base exception for all exceptions that need to be checked
* `ConflictException` - exception to be used for cases when saving a new entity would cause a 
`UniqueConstraintViolationException` for persistence layer
* `EntityNotFoundException` - exception to be used when an entity is not found in database, handled automatically by
`AbstractController`

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

    implementation group: 'ee.bitweb', name: 'core-lib', version: '1.0.0'

### Current development version

All changes to master branch will be pushed to Maven repository as SNAPSHOT versions

    implementation group: 'ee.bitweb', name: 'core-lib', version: '1.0.0-SNAPSHOT', changing: 'true'

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

### 1.0.0-SNAPSHOT

* Initial version

