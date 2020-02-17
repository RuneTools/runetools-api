package org.runetools.api.util;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JsonUtilsTest {
    private ObjectNode root;

    @BeforeEach
    public void setUp() {
        ObjectNode child = JsonNodeFactory.instance.objectNode();
        child.set("value", JsonNodeFactory.instance.numberNode(42));

        root = JsonNodeFactory.instance.objectNode();
        root.set("child", child);
    }

    @Test
    public void testPathNoArgs() {
        var node = JsonUtils.path(root);
        Assertions.assertTrue(node.isPresent());
        Assertions.assertEquals(root, node.get());
    }

    @Test
    public void testPathSuccess() {
        var node = JsonUtils.path(root, "child", "value");
        Assertions.assertTrue(node.isPresent());
        Assertions.assertEquals(42, node.get().intValue());
    }

    @Test
    public void testPathPartialMatch() {
        var node = JsonUtils.path(root, "child", "bad", "path");
        Assertions.assertTrue(node.isEmpty());
    }

    @Test
    public void testPathNonObject() {
        var node = JsonUtils.path(root, "child", "value", "bad");
        Assertions.assertTrue(node.isEmpty());
    }
}
