package org.runetools.api.test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import java.io.IOException;
import java.io.UncheckedIOException;

public class TestUtil {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static ArrayNode jsonArray(JsonNode... nodes) {
        var arrayNode = JsonNodeFactory.instance.arrayNode();
        for (var node : nodes) arrayNode.add(node);
        return arrayNode;
    }

    public static JsonNode jsonResource(String path) {
        try {
            return OBJECT_MAPPER.readTree(TestUtil.class.getResourceAsStream(path));
        } catch (IOException exception) {
            throw new UncheckedIOException("Error reading resource JSON " + path, exception);
        }
    }
}
