package ee.bitweb.core.object_mapper;

import ee.bitweb.core.TestSpringApplication;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@Tag("integration")
@ActiveProfiles("MockedInvokerTraceIdCreator")
@SpringBootTest(classes = TestSpringApplication.class)
class ObjectMapperAutoConfigurationNotSetIntegrationTests {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    @DisplayName("Should not load ObjectMapperAutoConfiguration bean when property is not set")
    void shouldNotLoadAutoConfigurationBeanWhenPropertyNotSet() {
        assertFalse(applicationContext.containsBean("objectMapperAutoConfiguration"));
    }

    @Test
    @DisplayName("Should not load coreLibJsonMapperCustomizer bean when property is not set")
    void shouldNotLoadCoreLibJsonMapperCustomizerBeanWhenPropertyNotSet() {
        assertFalse(applicationContext.containsBean("coreLibJsonMapperCustomizer"));
    }
}
