package ee.bitweb.core.util.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StringDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.io.IOException;

public class TrimmedStringDeserializer extends StringDeserializer {

    @Override
    public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String value = super.deserialize(p, ctxt);

        return value != null ? value.trim() : null;
    }

    public static void addToObjectMapper(ObjectMapper mapper) {
        SimpleModule module = new SimpleModule();
        module.addDeserializer(String.class, new TrimmedStringDeserializer());
        mapper.registerModule(module);
    }
}
