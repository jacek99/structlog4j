package com.github.structlog4j.json;

import com.github.structlog4j.IFormatter;
import org.slf4j.Logger;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;
import java.io.StringWriter;

/**
 * Basic JSON formatter. Formats using Glassfish JSON library as it has minimal
 * dependencies
 *
 * @author Jacek Furmankiewicz
 */
public class JsonFormatter implements IFormatter<JsonObjectBuilder> {

    private String fieldMessage = "message";
    private static final String FIELD_MESSAGE_2 = "message2";

    private static final JsonFormatter INSTANCE = new JsonFormatter();
    public static JsonFormatter getInstance() {return INSTANCE;}

    public IFormatter<JsonObjectBuilder> setMessageName(String field) {
        fieldMessage = field;
        return this;
    }

    @Override
    public final JsonObjectBuilder start(Logger log) {
        return Json.createObjectBuilder();
    }

    @Override
    public final IFormatter<JsonObjectBuilder> addMessage(Logger log, JsonObjectBuilder bld, String message) {
        bld.add(fieldMessage,message);
        return this;
    }

    @Override
    public final IFormatter<JsonObjectBuilder> addKeyValue(Logger log, JsonObjectBuilder bld, String key, Object value) {
        // avoid overriding the "fieldMessage" field
        if (key.equals(fieldMessage)) {
            key = FIELD_MESSAGE_2;
            log.warn("Key '{}' renamed to '{}' in order to avoid overriding default JSON message field. Please correct in your code.",fieldMessage,FIELD_MESSAGE_2);
        }

        // different methods per type
        if (value == null) {
            bld.addNull(key);
        } else if (value instanceof Boolean) {
            bld.add(key,(boolean)value);
        } else if (value instanceof Integer || value instanceof Short) {
            bld.add(key,(int)value);
        } else if (value instanceof Long) {
            bld.add(key,(long)value);
        } else if (value instanceof Double || value instanceof Float) {
            bld.add(key,(double)value);
        } else {
            bld.add(key,String.valueOf(value));
        }
        return this;
    }

    @Override
    public final String end(Logger log, JsonObjectBuilder bld) {
        StringWriter stWriter = new StringWriter();
        try (JsonWriter jsonWriter = Json.createWriter(stWriter)) {
            jsonWriter.writeObject(bld.build());
        }
        return stWriter.toString();
    }

}
