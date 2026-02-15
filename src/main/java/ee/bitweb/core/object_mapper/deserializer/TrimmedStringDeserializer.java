package ee.bitweb.core.object_mapper.deserializer;

import ee.bitweb.core.util.StringUtil;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdScalarDeserializer;
import tools.jackson.databind.module.SimpleModule;

/**
 * Jackson 3.x deserializer that trims whitespace from all string fields.
 *
 * @see Jackson2TrimmedStringDeserializer for Jackson 2.x version (Retrofit compatibility)
 */
public class TrimmedStringDeserializer extends StdScalarDeserializer<String> {

    public TrimmedStringDeserializer() {
        super(String.class);
    }

    @Override
    public String deserialize(JsonParser p, DeserializationContext ctxt) {
        return StringUtil.trim(p.getString());
    }

    public static SimpleModule createModule() {
        SimpleModule module = new SimpleModule();
        module.addDeserializer(String.class, new TrimmedStringDeserializer());

        return module;
    }
}
