package ee.bitweb.core.trace.creator;

import ee.bitweb.core.trace.TraceIdFormConfig;
import ee.bitweb.core.trace.invoker.InvokerTraceIdFormConfig;
import ee.bitweb.core.util.StringUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class TraceIdCreatorImplTests {

    @Test
    @DisplayName("Default configuration, inbound trace id is present, must append new trace id")
    void onExistingInboundTraceIdShouldGenerateNewCorrectTraceId() {
        TraceIdFormConfig config = new InvokerTraceIdFormConfig();
        TraceIdCreator creator = new TraceIdCreatorImpl(config);

        try (MockedStatic<StringUtil> util = Mockito.mockStatic(StringUtil.class)) {
            util.when(() -> StringUtil.random(InvokerTraceIdFormConfig.MAX_LENGTH)).thenReturn("d4s5da48d7");

            Assertions.assertEquals("inbound-id_d4s5da48d7", creator.generate("inbound-id"));
        }
    }

    @Test
    @DisplayName("Default configuration, inbound trace id is null, must create new trace id")
    void onNullExistingTraceIdShouldGenerateNew() {
        TraceIdFormConfig config = new InvokerTraceIdFormConfig();
        TraceIdCreator creator = new TraceIdCreatorImpl(config);

        try (MockedStatic<StringUtil> util = Mockito.mockStatic(StringUtil.class)) {
            util.when(() -> StringUtil.random(InvokerTraceIdFormConfig.MAX_LENGTH)).thenReturn("d4s5da48d7");

            Assertions.assertEquals("d4s5da48d7", creator.generate(null));
        }
    }

    @Test
    @DisplayName("Default configuration, inbound trace id is blank, must create new trace id")
    void onBlankExistingTraceIdShouldGenerateNew() {
        TraceIdFormConfig config = new InvokerTraceIdFormConfig();
        TraceIdCreator creator = new TraceIdCreatorImpl(config);

        try (MockedStatic<StringUtil> util = Mockito.mockStatic(StringUtil.class)) {
            util.when(() -> StringUtil.random(InvokerTraceIdFormConfig.MAX_LENGTH)).thenReturn("d4s5da48d7");

            Assertions.assertEquals("d4s5da48d7", creator.generate(""));
        }
    }

    @Test
    @DisplayName("Custom length, inbound trace id is null, must return new trace id with correct length")
    void onNullExistingTraceIdShouldGenerateNewWithCorrectLength() {
        InvokerTraceIdFormConfig config = new InvokerTraceIdFormConfig();
        config.setLength(18);
        TraceIdCreator creator = new TraceIdCreatorImpl(config);

        try (MockedStatic<StringUtil> util = Mockito.mockStatic(StringUtil.class)) {
            util.when(() -> StringUtil.random(18)).thenReturn("d4s5da48d7");

            Assertions.assertEquals("d4s5da48d7", creator.generate(null));
        }
    }

    @Test
    @DisplayName("Custom length, inbound trace id is present, must append new trace id with correct length")
    void onExistingTraceIdShouldGenerateNewWithCorrectLength() {
        InvokerTraceIdFormConfig config = new InvokerTraceIdFormConfig();
        config.setLength(18);
        TraceIdCreator creator = new TraceIdCreatorImpl(config);

        try (MockedStatic<StringUtil> util = Mockito.mockStatic(StringUtil.class)) {
            util.when(() -> StringUtil.random(18)).thenReturn("d4s5da48d7");

            Assertions.assertEquals("inbound-id_d4s5da48d7", creator.generate("inbound-id"));
        }
    }

    @Test
    @DisplayName("Custom delimiter, header is present, must append new trace id with correct delimiter")
    void onExistingTraceIdShouldGenerateNewWithCorrectDelimiter() {
        InvokerTraceIdFormConfig config = new InvokerTraceIdFormConfig();
        config.setDelimiter('/');
        TraceIdCreator creator = new TraceIdCreatorImpl(config);

        try (MockedStatic<StringUtil> util = Mockito.mockStatic(StringUtil.class)) {
            util.when(() -> StringUtil.random(InvokerTraceIdFormConfig.MAX_LENGTH)).thenReturn("d4s5da48d7");

            Assertions.assertEquals("inbound-id/d4s5da48d7", creator.generate("inbound-id"));
        }
    }

    @Test
    @DisplayName("Custom prefix, inbound traceid is not present, must return new trace id with correct prefix and length")
    void onNullTraceIdShouldGenerateNewWithCorrectPrefix() {
        InvokerTraceIdFormConfig config = new InvokerTraceIdFormConfig();
        config.setPrefix("MY-PREFIX");
        TraceIdCreator creator = new TraceIdCreatorImpl(config);

        try (MockedStatic<StringUtil> util = Mockito.mockStatic(StringUtil.class)) {
            util.when(
                    () -> StringUtil.random(InvokerTraceIdFormConfig.MAX_LENGTH - config.getPrefix().length())
            ).thenReturn("d4s5da48d7");

            Assertions.assertEquals("MY-PREFIXd4s5da48d7", creator.generate(null));
        }
    }

    @Test
    @DisplayName("Custom prefix, inbound is present, must return new trace id with correct prefix and length")
    void onExistingTraceIdShouldGenerateNewWithCorrectPrefix() {
        InvokerTraceIdFormConfig config = new InvokerTraceIdFormConfig();
        config.setPrefix("MY-PREFIX");
        TraceIdCreator creator = new TraceIdCreatorImpl(config);

        try (MockedStatic<StringUtil> util = Mockito.mockStatic(StringUtil.class)) {
            util.when(
                    () -> StringUtil.random(InvokerTraceIdFormConfig.MAX_LENGTH - config.getPrefix().length())
            ).thenReturn("d4s5da48d7");

            Assertions.assertEquals("inbound-id_MY-PREFIXd4s5da48d7", creator.generate("inbound-id"));
        }
    }
}
