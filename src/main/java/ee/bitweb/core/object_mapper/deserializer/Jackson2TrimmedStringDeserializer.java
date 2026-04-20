package ee.bitweb.core.object_mapper.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StringDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import ee.bitweb.core.util.StringUtil;

import java.io.IOException;

/**
 * Jackson 2.x deserializer that trims whitespace from all string fields.
 * Required for Retrofit's converter-jackson which uses Jackson 2.
 *
 * @see TrimmedStringDeserializer for Jackson 3.x version
 */
public class Jackson2TrimmedStringDeserializer extends StringDeserializer {

    @Override
    public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        return StringUtil.trim(super.deserialize(p, ctxt));
    }

    public static SimpleModule createModule() {
        SimpleModule module = new SimpleModule();
        module.addDeserializer(String.class, new Jackson2TrimmedStringDeserializer());

        return module;
    }

    public static void addToObjectMapper(ObjectMapper mapper) {
        mapper.registerModule(createModule());
    }
}
