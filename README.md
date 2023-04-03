# BitWeb OÜ Core library

Given library provides basic functionality that is required by any HTTP web service.
Given library contains only generic functionality that is not specific to any field/subject. 

## Provided features 

On order to enable component scanning, include package in component scanning eg. `scanBasePackages = {"ee.bitweb.core", ...}`

#### Audit logging


###### Since 2.5.0

Introducing Audit logging support. Audit logging allows to create a singular log entry about a request.
In order to enable that feature `ee.bitweb.core.audit.auto-configuration=true` should be added to application.properties file.
Audit logging is flexible. Please refer to AuditLogProperties class to see which configuration properties are available. 
Should you wish to implement own data mapper, simply define a bean that extends `ee.bitweb.core.audit.mappers.AuditLogDataMapper` class.
By default logs are written to logger (which should be sent to Graylog). Should you wish to store logs to separate storage solution, you 
can override behaviour by defining a bean that implements `ee.bitweb.core.audit.writers.AuditLogWriteAdapter` interface.

#### CAUTION 
Given feature can produce alot of data in logs, please be considerate.
Consider separating these logs to separate log storage with low retention.
Enable this feature for environments that really need it. 


### AMQP Support
###### Since 2.4.0
AMQP features autoconfiguration is introduced. Autoconfiguration can be enabled with property `ee.bitweb.core.amqp.auto-configuration=true`. 
This will configure json message converter for both serialization and deserialization.
If trace propagation is enabled, trace id propagation will be also autoconfigured for amqp messages.
By default error handling is configured to drop messages if listener throws an exception.
Recommended way to avoid data loss is to configure Dead Letter Exchange.


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
###### Since 2.5.0

Whitelist Criteria which is used for Auth token injection to requests now implements validation rules to incoming patterns. 
* Invalid pattern endings: ["//.*"]
* Invalid containments in pattern: [".*/", "^https://localhost.*"]
* Valid pattern prefixes: ["^http://", "^https://"]

If any rule is not followed an error is logged. In future releases an exception will be thrown.

Introduced retrofit timeout configuration properties. Timeout properties must be provided in milliseconds. 
0 value means no timeout. Full configuration options:
* `ee.bitweb.core.retrofit.timeout.call` - time limit for a complete HTTP call, default `0`
* `ee.bitweb.core.retrofit.timeout.connect` - time period in which our client should establish a connection with a target host, default `10000`
* `ee.bitweb.core.retrofit.timeout.read` - maximum time of inactivity between two data packets when waiting for the server's response, default `10000`
* `ee.bitweb.core.retrofit.timeout.write` - maximum time of inactivity between two data packets when sending the request to the server, default `10000`

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

### Scheduled jobs
###### Since 2.1.0
Introducing a more convenient way of creating scheduled jobs and reduces boilerplate code. Usage:

    @Component
    public class ProductionOrderUpdateScheduler extends ScheduledJob<ProductionOrderRowImportComponent> {
    
        public ProductionOrderUpdateScheduler(
            final ProductionOrderRowImportComponent runnable,
            final SchedulerTraceIdResolver traceIdResolver
        ) {
            super(runnable, traceIdResolver);
        }
    
        @Scheduled(cron = "${scheduled.production-order-row.cron}")
        public void schedule() {
            run();
        }
    }

`ProductionOrderRowImportComponent` class just needs to implement `ScheduledRunnable` interface and `ScheduledJob` handles
logging, errors and trace id. Requires trace id functionality to be enabled. 

## Usage

### Production release

Add BitWeb's public Maven repository to your build.gradle file

    repositories {
        mavenCentral()
        maven {
            url "https://nexus.bitweb.ee/repository/maven-public/"
        }
    }

Add dependency to your project

    // https://bitbucket.org/bitwebou/java-core-lib/src/master/
    implementation group: 'ee.bitweb', name: 'core', version: '2.6.+'

### Current development version

Add snapshot repository

    repositories {
        mavenCentral()
        maven {
            url "https://nexus.bitweb.ee/repository/maven-snapshot/"
        }
    }

Use desired version

    // https://bitbucket.org/bitwebou/java-core-lib/src/master/
    implementation group: 'ee.bitweb', name: 'core', version: '2.7.0-SNAPSHOT'


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

### 2.6.1
* Fixed MDC clearing once AMQP message has been processed. Before AmqpTraceAfterReceiveMessageProcessor had been used.
  It turned out to not work as intended, thus clearing had been moved to `ee.bitweb.core.trace.invoker.amqp.AmqpTraceAdvisor`

### 2.6.0

* Add handler in `ControllerAdvisor` for `ClientAbortException`. In case of Broken Pipe, no response is sent and the
  exception is logged at desired level. The level is set by `ee.bitweb.core.controller-advice.logging.client-abort-exception`
  and default is `WARN`.
* Fix and bump dependency versions

### 2.5.1

* Added null check inside `ee.bitweb.core.audit.writers.AuditLogLoggerWriterAdapter`

### 2.5.0
* Introduced Audit Log feature.

### 2.4.0
* Require Spring Boot 2.+ instead of 2.6.+
* Bump various other dependencies
* Remove Spring Security from implementation classpath
* Added base functionality to autoconfigure AMQP integration.
* Added ObjectMapper autoconfiguration. To enable, add property `ee.bitweb.core.object-mapper.auto-configuration=true`
  This will setup java.time module support and disable potentially harmful data casting operations which lead to data misinterpretation.

### 2.3.0
* Introduces option to configure logging levels in `ControllerAdvisor`. All exceptions caught now have a configurable 
  logging level. In addition, logging can be turned off. Uncaught exceptions cannot be configured and are always logged
  in error level. See `ee.bitweb.core.api.ControllerAdvisorProperties` for full list of options.

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
* Added `ScheduledJob` class and `ScheduledRunnable` annotation to reduce boilerplate code for scheduled jobs. Handles 
  logging, errors and trace logic.

### 2.0.1 

* SBA request actuator/health threw ClassDefNotFoundException in jackson serializer.
Had to downgrade kotlin version to 2.12.0.
Bugfix thread: https://github.com/FasterXML/jackson-module-kotlin/issues/523

### 2.0.0

* Major rewrite, not at all compatible with 1.*

### 1.0.0

* Initial version

