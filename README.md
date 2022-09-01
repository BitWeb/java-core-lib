# BitWeb OÜ Core library

Given library provides basic functionality that is required by any HTTP web service.
Given library contains only generic functionality that is not specific to any field/subject. 

## Provided features 

On order to enable component scanning, include package in component scanning eg. `scanBasePackages = {"ee.bitweb.core", ...}`

### General HTTP web request error handling for Spring Boot applications. 
###### Since 2.0.0
In order to enable standardised error handlers for HTTP web request hooks 
you simply need to include property `ee.bitweb.core.controller-advice.enabled=true` in application properties.

### Trace ID generation and propagation

###### Since 2.2.0
Separated additional MDC entries into separate features that TraceIdFilter can add optionally. By default all additional
entries are enabled as they were previously.
Features can be fine tuned with new property `ee.bitweb.core.trace.invoker.http.enabledFeatures` property, for example 
`ee.bitweb.core.trace.invoker.http.enabledFeatures=ADD_URL,ADD_METHOD,ADD_USER_AGENT` to only add URL, Http method and user agent to mdc.
List of features can be observed in class `TraceIdFilter.Feature` class.

###### Since 2.0.0
Given package contains Trace ID generation and propagation related features consolidated in `TraceIdFilter`
Trace ID is easily autoconfigurable by adding `ee.bitweb.core.trace.auto-configuration=true` to application properties.

### Spring Boot Actuator Security
###### Since 2.1.0
Simplifies securing Spring Boot Actuator endpoints to authorise only one user. Health endpoint has special security
configuration to allow unauthorised access by different monitoring tools. Actuator must be configured to only 
show basic data (`when_authorized`) or show no data at all (`never`) with property
`management.endpoint.health.show-details` , otherwise information about the application and it's contents may leak in
the form of used databases and other connections. This may open up indirect attack vectors.

For basic usage, just two config options are needed:

    ee.bitweb.core.actuator.security.enabled=true
    ee.bitweb.core.actuator.security.user.password=my-secret-password-123

Full configuration options:
* `ee.bitweb.core.actuator.security.enabled` - enables or disables security configuration, default `false`
* `ee.bitweb.core.actuator.security.role` - specifies the role that is allowed to access actuator endpoints, default `ACTUATOR`
* `ee.bitweb.core.actuator.security.health-endpoint-roles` - list of roles that are allowed to view health endpoint, default `ACTUATOR,ANONYMOUS`
* `ee.bitweb.core.actuator.security.user.name` - name of the user that is allowed, default `actuator-user`
* `ee.bitweb.core.actuator.security.user.password` - password of the user that is allowed, defaults to random string
* `ee.bitweb.core.actuator.security.user.roles` - list of roles to assign to the user, default `ACTUATOR`

### Retrofit features
###### Since 2.1.0
Introduces a more convenient builder for retrofit api creation. `ee.bitweb.core.retrofit.builder.RetrofitApiBuilder`
builder has no requirements to use, it has logging interceptor set up out of the box. In addition it uses
preconfigured JSON payload converter with predefined ObjectMapper to included `JavaTimeModule`.

Spring context aware `ee.bitweb.core.retrofit.builder.SpringAwareRetrofitBuilder` bean is also created when 
`ee.bitweb.core.retrofit.auto-configuration=true` property is set in configuration. It will automatically detect all
interceptor beans that implement `ee.bitweb.core.retrofit.interceptor.InterceptorBean` interface and add those to all
api-s. 

If both `ee.bitweb.core.trace.auto-configuration=true` and `ee.bitweb.core.retrofit.auto-configuration=true` properties
are set, then TraceId propagation interceptor is automatically loaded to the list of preloaded interceptors for
`ee.bitweb.core.retrofit.builder.SpringAwareRetrofitBuilder`

Basic `ee.bitweb.core.retrofit.interceptor.auth.AuthTokenInjectInterceptor` is also provided that can be used to 
propagate auth tokens between internal services. 
It can be autoconfigured as default interceptor bean by setting property `ee.bitweb.core.retrofit.auth-token-injector.enabled=true`
you will also need to specify the header name via. `ee.bitweb.core.retrofit.auth-token-injector.headerName` property. 
Interceptor will add tokens to request that pass a whitelist. You can specify the contents of that whitelist with
`ee.bitweb.core.retrofit.auth-token-injector.whitelist-urls[]` property which takes regex patterns as values. 
For example ee.bitweb.core.retrofit.auth-token-injector.whitelist-urls[0]=^http?:\\/\\/localhost:\\d{3,5}\\/.*
Lastly, you must provide implementation of `ee.bitweb.core.retrofit.interceptor.auth.TokenProvider` interface. 
In case of an autoconfiguration, it must be declared as bean 


## Usage

Add BitWeb's public Maven repository to your build.gradle file

    repositories {
        mavenCentral()
        maven {
            url "https://nexus.bitweb.ee/repository/maven-public/"
        }
    }

Add dependency to your project

### Production release

    // https://bitbucket.bitweb.ee/projects/BITWEB/repos/java-core-lib/browse
    implementation group: 'ee.bitweb', name: 'core', version: '2.1.0'

### Current development version

Development versions are not provided at this point.

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

After this is done, you'll need to run `gradle publishToMavenLocal` in this project, to generate the jar file and add it
to the local Maven Repository in your machine. Then, in the project that's using this library, you need 
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

## Release notes

### 2.2.0
* Separated additional MDC entries into separate features that TraceIdFilter can add optionally. By default all additional
  entries are enabled as they were previously.
* Added `ee.bitweb.core.trace.thread.MDCTaskDecorator` class to simplify MDC and SecurityContext propagation to threads in thread pools. 

### 2.1.1

* Fix API documentation error: `Error resolving $ref pointer "https://xxx/api-docs/v1#/components/schemas/CriteriaResponse". Token "CriteriaResponse" does not exist.`
* Fix TraceIdFilterConfig: Added sensitiveHeaders to configuration making it configurable as opposed to being locked as filter private attribute

### 2.1.0

* Added Spring Boot Actuator security configuration
* Added Retrofit api builder and auto configuration

### 2.0.1 

* SBA request actuator/health threw classdefNotFoundException in jackson serializer.
Had to downgrade kotlin version to 2.12.0.
Bugfix thread: https://github.com/FasterXML/jackson-module-kotlin/issues/523

### 2.0.0

* Major rewrite, not at all compatible with 1.*

### 1.0.0

* Initial version

