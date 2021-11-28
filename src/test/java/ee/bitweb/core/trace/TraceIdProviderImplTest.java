package ee.bitweb.core.trace;

import ee.bitweb.core.util.StringUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.http.HttpServletRequest;

import static org.junit.jupiter.api.Assertions.*;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class TraceIdProviderImplTest {

    @Mock
    private HttpServletRequest request;

    @Test
    @DisplayName("Default configuration, header is not present in request, must return new trace id")
    void testCorrectTraceIdIsGeneratedWhenNonePresent() {
        TraceIdProviderImpl provider = new TraceIdProviderImpl(TraceIdCustomizerImpl.standard());

        Mockito.when(request.getHeader("X-Trace-ID")).thenReturn(null);

        try (MockedStatic<StringUtil> util = Mockito.mockStatic(StringUtil.class)) {
            util.when(() -> StringUtil.random(20)).thenReturn("d4s5da48d7e8dwfweq45");

            assertEquals("d4s5da48d7e8dwfweq45", provider.generate(request));
        }
    }

    @Test
    @DisplayName("Default configuration, header is present in request, must append new trace id")
    void testCorrectTraceIdIsGeneratedWhenOneIsPresent() {
        TraceIdProviderImpl provider = new TraceIdProviderImpl(TraceIdCustomizerImpl.standard());

        Mockito.when(request.getHeader("X-Trace-ID")).thenReturn("d4s5da48d7e8dwfweq45");

        try (MockedStatic<StringUtil> util = Mockito.mockStatic(StringUtil.class)) {
            util.when(() -> StringUtil.random(20)).thenReturn("8dwfweq45d4s5da48d7e");

            assertEquals("d4s5da48d7e8dwfweq45_8dwfweq45d4s5da48d7e", provider.generate(request));
        }
    }

    @Test
    @DisplayName("Custom length, header not present, must return new trace id with correct length")
    void testCorrectLengthIdIsGenerated() {
        TraceIdProviderImpl provider = new TraceIdProviderImpl(TraceIdCustomizerImpl.builder().length(10).build());

        Mockito.when(request.getHeader("X-Trace-ID")).thenReturn(null);

        try (MockedStatic<StringUtil> util = Mockito.mockStatic(StringUtil.class)) {
            util.when(() -> StringUtil.random(10)).thenReturn("d4s5da48d7");

            assertEquals("d4s5da48d7", provider.generate(request));
        }
    }

    @Test
    @DisplayName("Custom length, header is present, must append new trace id with correct length")
    void testCorrectLengthIdIsGeneratedIrrelevantOfExisting() {
        TraceIdProviderImpl provider = new TraceIdProviderImpl(TraceIdCustomizerImpl.builder().length(10).build());

        Mockito.when(request.getHeader("X-Trace-ID")).thenReturn("d4s5da48d7e8dwfweq45");

        try (MockedStatic<StringUtil> util = Mockito.mockStatic(StringUtil.class)) {
            util.when(() -> StringUtil.random(10)).thenReturn("d4s5da48d7");

            assertEquals("d4s5da48d7e8dwfweq45_d4s5da48d7", provider.generate(request));
        }
    }

    @Test
    @DisplayName("Custom delimiter, header is present, must append new trace id with correct delimiter")
    void testCustomDelimiterIsHonoured() {
        TraceIdProviderImpl provider = new TraceIdProviderImpl(TraceIdCustomizerImpl.builder().delimiter('/').build());

        Mockito.when(request.getHeader("X-Trace-ID")).thenReturn("d4s5da48d7-e8dwfweq45");

        try (MockedStatic<StringUtil> util = Mockito.mockStatic(StringUtil.class)) {
            util.when(() -> StringUtil.random(20)).thenReturn("d4s5da48d7d4s5da48d7");

            assertEquals("d4s5da48d7-e8dwfweq45/d4s5da48d7d4s5da48d7", provider.generate(request));
        }
    }

    @Test
    @DisplayName("Custom prefix, header is not present, must return new trace id with correct prefix and length")
    void testCustomPrefixIsUsed() {
        TraceIdCustomizerImpl customizer = TraceIdCustomizerImpl.builder().idPrefix("ABC").length(10).build();
        TraceIdProviderImpl provider = new TraceIdProviderImpl(customizer);

        Mockito.when(request.getHeader("X-Trace-ID")).thenReturn(null);

        try (MockedStatic<StringUtil> util = Mockito.mockStatic(StringUtil.class)) {
            util.when(() -> StringUtil.random(7)).thenReturn("d4s5da4");

            assertEquals("ABCd4s5da4", provider.generate(request));
        }
    }

    @Test
    @DisplayName("Custom prefix, header is present, must append new trace id with correct prefix and length")
    void testCustomPrefixIsUsed2() {
        TraceIdCustomizerImpl customizer = TraceIdCustomizerImpl.builder().idPrefix("A8").length(10).build();
        TraceIdProviderImpl provider = new TraceIdProviderImpl(customizer);

        Mockito.when(request.getHeader("X-Trace-ID")).thenReturn("d4s5da48d7");

        try (MockedStatic<StringUtil> util = Mockito.mockStatic(StringUtil.class)) {
            util.when(() -> StringUtil.random(8)).thenReturn("d5da4fea");

            assertEquals("d4s5da48d7_A8d5da4fea", provider.generate(request));
        }
    }
}
