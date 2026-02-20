# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Development notes
* Always use Context7 MCP when I need library/API documentation, code generation, setup or configuration steps without me having to explicitly ask.
* Always add modified and new files to Git, but do not commit them.
* Use Lombok whenever possible
* Always remember to use specialized agents to write or fix code and related tests:
    * spring-boot-engineer for coding anything (including unit and integration tests)

## Project Overview

Bitweb Spring Core library (`ee.bitweb:spring-core`) - a reusable library providing generic functionality for Spring Boot HTTP web services. Published to Maven Central.

## Build Commands

```bash
# Build the project
./gradlew build

# Run all tests
./gradlew test

# Run only unit tests (excludes @Tag("integration"))
./gradlew unitTest

# Run only integration tests (includes @Tag("integration"))
./gradlew integrationTest

# Run a single test class
./gradlew test --tests "ee.bitweb.core.api.model.exception.ControllerAdvisorIntegrationTests"

# Run tests with coverage reports
./gradlew testAndReport
```

## Architecture

This is a Spring Boot auto-configuration library. Features are enabled via property flags with the pattern `ee.bitweb.core.<module>.auto-configuration=true`.

### Core Modules

- **trace** - Request tracing with trace ID propagation across HTTP requests, AMQP messages, and threads. Uses MDC for logging context.
- **api** - Global exception handling via `ControllerAdvisor` with standardized error responses.
- **audit** - HTTP request/response audit logging with pluggable mappers and writers.
- **retrofit** - Retrofit HTTP client integration with `SpringAwareRetrofitBuilder` for building API clients with automatic interceptors and configuration.
- **amqp** - RabbitMQ integration with automatic trace ID propagation and message converters.
- **actuator** - Spring Actuator security configuration.
- **cors** - CORS auto-configuration.
- **validator** - Custom Jakarta validators (`@FileType`, `@Uppercase`).

### Key Patterns

- Auto-configuration classes use `@ConditionalOnProperty` with prefix `ee.bitweb.core.<module>`
- Most beans are `@ConditionalOnMissingBean` allowing override by consuming applications
- Properties classes follow pattern `<Module>Properties.java` with `PREFIX` constant
- Integration tests use `@Tag("integration")` annotation

## Testing

- Uses JUnit 5 with Spring Boot Test
- Integration tests require `@Tag("integration")` annotation
- Test application: `ee.bitweb.core.TestSpringApplication`
- Uses Testcontainers for RabbitMQ integration tests
- Uses MockServer for HTTP client tests

## Java Version

Java 17 (Temurin distribution via SDKMAN)

## Spring Boot Version

Spring Boot 4.0.0 with Spring Framework 7.0.0

Note: This version uses Jackson 3.x (tools.jackson package) for exception handling in the ControllerAdvisor.
