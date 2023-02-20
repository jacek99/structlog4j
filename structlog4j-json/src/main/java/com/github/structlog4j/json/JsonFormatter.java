package com.github.structlog4j.json;

import com.github.structlog4j.IFormatter;
import java.io.StringWriter;
import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;
import org.slf4j.Logger;

/**
 * Basic JSON formatter. Formats using Glassfish JSON library as it has minimal dependencies
 *
 * @author Jacek Furmankiewicz
 */
public class JsonFormatter implements IFormatter<JsonObjectBuilder> {

  private static final String FIELD_MESSAGE = "message";
  private static final String FIELD_MESSAGE_2 = "message2";

  private static final JsonFormatter INSTANCE = new JsonFormatter();

  public static JsonFormatter getInstance() {
    return INSTANCE;
  }

  @Override
  public final JsonObjectBuilder start(Logger log) {
    return Json.createObjectBuilder();
  }

  @Override
  public final IFormatter<JsonObjectBuilder> addMessage(
      Logger log, JsonObjectBuilder bld, String message) {
    bld.add(FIELD_MESSAGE, message);
    return this;
  }

  @Override
  @SuppressWarnings("PMD.AvoidReassigningParameters")
  public final IFormatter<JsonObjectBuilder> addKeyValue(
      Logger log, JsonObjectBuilder bld, String key, Object value) {
    // avoid overriding the "message" field
    if (key.equals(FIELD_MESSAGE)) {
      key = FIELD_MESSAGE_2;
      log.warn(
          "Key 'message' renamed to 'message2' in order to avoid overriding default JSON message field. Please correct in your code.");
    }

    // different methods per type
    if (value == null) {
      bld.addNull(key);
    } else if (value instanceof Boolean) {
      bld.add(key, (boolean) value);
    } else if (value instanceof Integer || value instanceof Short) {
      bld.add(key, (int) value);
    } else if (value instanceof Long) {
      bld.add(key, (long) value);
    } else if (value instanceof Double || value instanceof Float) {
      bld.add(key, (double) value);
    } else {
      bld.add(key, String.valueOf(value));
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
