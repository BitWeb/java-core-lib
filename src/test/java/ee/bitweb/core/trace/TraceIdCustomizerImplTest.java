package ee.bitweb.core.trace;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

@Tag("unit")
class TraceIdCustomizerImplTest {

    @Test
    @DisplayName("Validate default values")
    void testDefaultValues() {
        TraceIdCustomizerImpl customizer = TraceIdCustomizerImpl.standard();

        assertEquals("X-Trace-ID", customizer.getHeaderName());
        assertNull(customizer.getPrefix());
        assertEquals('_', customizer.getDelimiter());
        assertEquals(20, customizer.getLength());
        assertEquals(0, customizer.getAdditionalHeaders().size());
    }

    @Test
    @DisplayName("Header name can be changed")
    void testHeaderNameCanBeChanged() {
        TraceIdCustomizerImpl customizer = TraceIdCustomizerImpl.builder().headerName("some-other-header").build();

        assertEquals("some-other-header", customizer.getHeaderName());
    }

    @Test
    @DisplayName("Header name can not be set to null")
    void testHeaderNameCannotBeSetToNull() {
        TraceIdCustomizerImpl.Builder builder = TraceIdCustomizerImpl.builder();

        assertThrows(IllegalArgumentException.class, () -> builder.headerName(null));
    }

    @Test
    @DisplayName("Header name can not contain spaces")
    void testHeaderNameCannotContainSpaces() {
        TraceIdCustomizerImpl.Builder builder = TraceIdCustomizerImpl.builder();

        assertThrows(IllegalArgumentException.class, () -> builder.headerName("A A"));
    }

    @Test
    @DisplayName("Prefix can be set a value")
    void testPrefixCanBeChanged() {
        TraceIdCustomizerImpl customizer = TraceIdCustomizerImpl.builder().idPrefix("AA").build();

        assertEquals("AA", customizer.getPrefix());
    }

    @Test
    @DisplayName("Prefix can be changed to null")
    void testPrefixCanBeSetBackToNull() {
        TraceIdCustomizerImpl customizer = TraceIdCustomizerImpl.builder().idPrefix("AS").idPrefix(null).build();

        assertNull(customizer.getPrefix());
    }

    @Test
    @DisplayName("Empty string prefix is changed to null")
    void testEmptyPrefixIsTreatedAsNull() {
        TraceIdCustomizerImpl customizer = TraceIdCustomizerImpl.builder().idPrefix("").build();

        assertNull(customizer.getPrefix());
    }

    @Test
    @DisplayName("Prefix with length of 5 char cannot be used")
    void testPrefixCannotBeLongerThan4char() {
        TraceIdCustomizerImpl.Builder builder = TraceIdCustomizerImpl.builder();

        assertThrows(IllegalArgumentException.class, () -> builder.idPrefix("12345"));
    }

    @Test
    @DisplayName("Prefix can't contain spaces")
    void testPrefixCannotContainSpaces() {
        TraceIdCustomizerImpl.Builder builder = TraceIdCustomizerImpl.builder();

        assertThrows(IllegalArgumentException.class, () -> builder.idPrefix("A A"));
    }

    @Test
    @DisplayName("Delimiter can be changed")
    void testCustomDelimiterCanBeSet() {
        TraceIdCustomizerImpl customizer = TraceIdCustomizerImpl.builder().delimiter('/').build();

        assertEquals('/', customizer.getDelimiter());
    }

    @Test
    @DisplayName("Length can be changed")
    void testLengthCanBeChanged() {
        TraceIdCustomizerImpl customizer = TraceIdCustomizerImpl.builder().length(10).build();

        assertEquals(10, customizer.getLength());
    }

    @Test
    @DisplayName("Length can't be under 10")
    void testLengthCannotBeUnder10() {
        TraceIdCustomizerImpl.Builder builder = TraceIdCustomizerImpl.builder();

        assertThrows(IllegalArgumentException.class, () -> builder.length(9));
    }

    @Test
    @DisplayName("Length can't be over 20")
    void testLengthCannotBeOver20() {
        TraceIdCustomizerImpl.Builder builder = TraceIdCustomizerImpl.builder();

        assertThrows(IllegalArgumentException.class, () -> builder.length(21));
    }

    @Test
    @DisplayName("Additional header can be added with one argument")
    void testAdditionalHeaderCanBeAdded() {
        TraceIdCustomizerImpl customizer = TraceIdCustomizerImpl.builder().additionalHeader("testing-more").build();

        assertEquals(1, customizer.getAdditionalHeaders().size());

        AdditionalHeader additionalHeader = customizer.getAdditionalHeaders().get(0);
        assertEquals("testing-more", additionalHeader.getHeader());
        assertEquals("testing-more", additionalHeader.getMdc());
    }

    @Test
    @DisplayName("Additional header can be added with different names")
    void testAdditionalHeaderCanBeAddedWithDifferentNames() {
        TraceIdCustomizerImpl customizer = TraceIdCustomizerImpl
                .builder()
                .additionalHeader("mdc_key", "header_key")
                .build();

        assertEquals(1, customizer.getAdditionalHeaders().size());

        AdditionalHeader additionalHeader = customizer.getAdditionalHeaders().get(0);
        assertEquals("header_key", additionalHeader.getHeader());
        assertEquals("mdc_key", additionalHeader.getMdc());
    }

    @Test
    @DisplayName("Multiple additional headers can be added")
    void testMultipleAdditionalHeadersCanBeAdded() {
        TraceIdCustomizerImpl customizer = TraceIdCustomizerImpl
                .builder()
                .additionalHeader("testing-more")
                .additionalHeader("testing-more-2")
                .build();

        assertEquals(2, customizer.getAdditionalHeaders().size());

        AdditionalHeader additionalHeader1 = customizer.getAdditionalHeaders().get(0);
        assertEquals("testing-more", additionalHeader1.getHeader());
        assertEquals("testing-more", additionalHeader1.getMdc());

        AdditionalHeader additionalHeader2 = customizer.getAdditionalHeaders().get(1);
        assertEquals("testing-more-2", additionalHeader2.getHeader());
        assertEquals("testing-more-2", additionalHeader2.getMdc());
    }

    @Test
    @DisplayName("Additional headers can be added with list")
    void testAdditionalHeadersCanBeAddedWithList() {
        ArrayList<AdditionalHeader> headers = new ArrayList<>();
        headers.add(new TraceIdCustomizerImpl.AdditionalHeaderImpl("test-mdc", "test-header"));
        headers.add(new TraceIdCustomizerImpl.AdditionalHeaderImpl("test-mdc-2", "test-header-2"));

        TraceIdCustomizerImpl customizer = TraceIdCustomizerImpl.builder().additionalHeaders(headers).build();

        assertEquals(2, customizer.getAdditionalHeaders().size());

        AdditionalHeader additionalHeader1 = customizer.getAdditionalHeaders().get(0);
        assertEquals("test-header", additionalHeader1.getHeader());
        assertEquals("test-mdc", additionalHeader1.getMdc());

        AdditionalHeader additionalHeader2 = customizer.getAdditionalHeaders().get(1);
        assertEquals("test-header-2", additionalHeader2.getHeader());
        assertEquals("test-mdc-2", additionalHeader2.getMdc());
    }

    @Test
    @DisplayName("Additional header can't be added after customizer has been built")
    void testAdditionalHeadersCannotBeAddedLater() {
        TraceIdCustomizerImpl customizer = TraceIdCustomizerImpl.standard();
        var headers = customizer.getAdditionalHeaders();
        var header = new TraceIdCustomizerImpl.AdditionalHeaderImpl("test-header", "test-header");

        assertThrows(UnsupportedOperationException.class, () -> headers.add(header));
    }

    @Test
    @DisplayName("Additional header can't be removed after customizer has been built")
    void testAdditionalHeadersCannotBeRemoved() {
        TraceIdCustomizerImpl customizer = TraceIdCustomizerImpl.builder().additionalHeader("test-header").build();
        var headers = customizer.getAdditionalHeaders();

        assertThrows(UnsupportedOperationException.class, () -> headers.remove(0));
    }
}
