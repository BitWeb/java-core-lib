# Jackson 3 Configuration Migration Progress

## Status: Completed

## Summary

The Jackson 3 migration for Spring Boot 4 compatibility is complete. All production code and tests have been migrated to use Jackson 3 (`tools.jackson` package) where appropriate.

## Migration Approach

Due to Retrofit's `converter-jackson` not yet supporting Jackson 3, the project maintains **both Jackson 2 and Jackson 3**:
- **Jackson 3** (`tools.jackson`): Used for Spring Boot 4's internal use, ObjectMapper configuration, ControllerAdvisor exception handling, and Audit module
- **Jackson 2** (`com.fasterxml.jackson`): Required for Retrofit's `JacksonConverterFactory`

## Completed Tasks

- [x] **Task 1**: Create Jackson 3 TrimmedStringDeserializer (extends `StdScalarDeserializer<String>`)
- [x] **Task 2**: Update ObjectMapperAutoConfiguration with JsonMapper bean
- [x] **Task 3**: Migrate AuditLogAutoConfiguration to Jackson 3
- [x] **Task 4**: Migrate RequestForwardingDataMapper to Jackson 3
- [x] **Task 5**: Migrate RequestHeadersMapper to Jackson 3
- [x] **Task 6**: Migrate test files (unit and integration tests)
- [x] **Task 7**: Update build.gradle with versioned Jackson dependencies

## Files Modified

### Production Code
1. `TrimmedStringDeserializer.java` - Extends `StdScalarDeserializer<String>`, uses `p.getString()`
2. `ObjectMapperAutoConfiguration.java` - Provides `JsonMapper` bean with Jackson 3 configuration
3. `AuditLogAutoConfiguration.java` - Uses `JsonMapper` instead of `ObjectMapper`
4. `RequestForwardingDataMapper.java` - Uses `JsonMapper`
5. `RequestHeadersMapper.java` - Uses `JsonMapper`

### Test Code
1. `RequestHeadersMapperUnitTests.java` - Uses `JsonMapper.builder().build()`
2. `RequestForwardingDataMapperUnitTests.java` - Uses `JsonMapper.builder().build()`
3. `AmqpTestHelper.java` - Uses `JsonMapper`
4. `TestPingController.java` - Kept `@JsonFormat` from `com.fasterxml.jackson.annotation` (backward compatible)

### Build Configuration
- `build.gradle` - Added separate version variables for Jackson 2 and Jackson 3

## Key Jackson 2 vs Jackson 3 Differences

| Jackson 2 | Jackson 3 | Notes |
|-----------|-----------|-------|
| `com.fasterxml.jackson` | `tools.jackson` | Package prefix changed |
| `ObjectMapper` | `JsonMapper` | Immutable, uses builder pattern |
| `JsonDeserializer` | `ValueDeserializer` | Class renamed |
| `throws IOException` | No checked exceptions | `JacksonException` extends `RuntimeException` |
| `StringDeserializer` | `StdScalarDeserializer<String>` | Use generic scalar deserializer |
| `p.getText()` | `p.getString()` | Method renamed |
| `JavaTimeModule` | Built-in | No registration needed |
| `DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE` | `DateTimeFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE` | Moved to new enum |
| `@JsonFormat` (annotation) | Same package | `jackson-annotations` unchanged for backward compatibility |

## Sources

- [Jackson 3 Migration Guide](https://github.com/FasterXML/jackson/blob/main/jackson3/MIGRATING_TO_JACKSON_3.md)
- [Jackson 3 Javadoc](https://www.javadoc.io/doc/tools.jackson.core/jackson-databind/3.0.2)
- [Spring Jackson 3 Support](https://spring.io/blog/2025/10/07/introducing-jackson-3-support-in-spring/)
