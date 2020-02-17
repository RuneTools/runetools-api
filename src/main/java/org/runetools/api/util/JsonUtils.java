package org.runetools.api.util;

import com.fasterxml.jackson.databind.JsonNode;

import javax.validation.constraints.NotNull;
import java.util.Optional;

public class JsonUtils {
    public static Optional<JsonNode> path(@NotNull JsonNode startNode, String... keys) {
        var nodeOptional = Optional.of(startNode);
        for (var key : keys) {
            nodeOptional = nodeOptional.map(node -> node.get(key));
        }
        return nodeOptional;
    }
}
