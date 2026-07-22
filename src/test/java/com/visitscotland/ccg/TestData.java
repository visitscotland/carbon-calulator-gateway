package com.visitscotland.ccg;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.util.function.Consumer;

/**
 * Note: This tool has been AI generated to simplify testing
 *
 * It would be able to create JSON like objects like this:
 *
 * <pre>
 * ObjectNode payload = new TestData()
 *         .add("name", "Jose")
 *         .add("removeMe", "x")
 *         .add("company", company -> company
 *                 .add("name", "VisitScotland"))
 *         .addArray("roles", "ADMIN", "USER")
 *         .objectNode();
 * </pre>
 *
 * Note that it hasn't been built for production use.
 */
public class TestData {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final ObjectNode node;

    public TestData() {
        this.node = OBJECT_MAPPER.createObjectNode();
    }

    public TestData add(String field, String value) {
        node.put(field, value);
        return this;
    }

    public TestData add(String field, int value) {
        node.put(field, value);
        return this;
    }

    public TestData add(String field, long value) {
        node.put(field, value);
        return this;
    }

    public TestData add(String field, boolean value) {
        node.put(field, value);
        return this;
    }

    public TestData add(String field, double value) {
        node.put(field, value);
        return this;
    }

    public TestData add(String field, JsonNode value) {
        node.set(field, value);
        return this;
    }

    public TestData add(String field, Consumer<TestData> child) {

        TestData nested = new TestData();
        child.accept(nested);

        node.set(field, nested.objectNode());

        return this;
    }

    public TestData addArray(String field, String... values) {

        ArrayNode array = node.putArray(field);

        for (String value : values) {
            array.add(value);
        }

        return this;
    }

    public ObjectNode objectNode() {
        return node.deepCopy();
    }

    public static ObjectNode simpleObjectNode() {
        return OBJECT_MAPPER.createObjectNode();
    }

}
