package com.visitscotland.ccg.payload;

import com.visitscotland.ccg.testutil.TestData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.node.ObjectNode;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class SubmissionPayloadTransformerTest {


    @InjectMocks
    private SubmissionPayloadTransformer transformer;

    @Test
    @DisplayName("Should remove configured properties before serialising the payload")
    void shouldRemoveConfiguredProperties() {
        ObjectNode payload = new TestData ()
                .add("keep", "value")
                .add("removeMe", "x")
                .add("removeMeToo", "y")
                .objectNode();

        ObjectNode modifiedPayload =
                transformer.transform(payload, "123", new String[]{"removeMe", "removeMeToo"});

        assertTrue(modifiedPayload.has("keep"));
        assertFalse(modifiedPayload.has("removeMe"));
        assertFalse(modifiedPayload.has("removeMeToo"));
    }

    @Test
    @DisplayName("Should add the submission ID to the outgoing payload")
    void shouldAddSubmissionId() {
        ObjectNode modifiedPayload =
                transformer.transform(TestData.simpleObjectNode(), "submission123", new String[0]);

        assertEquals("submission123", modifiedPayload.path(SubmissionPayloadTransformer.SUBMISSION_ID).asString());
    }

    @Test
    @DisplayName("Should create the base property from a variant field")
    void shouldCreateBasePropertyFromVariant() {
        ObjectNode payload = new TestData ()
                .add("businessSubSector---accommodation", "HOTELS")
                .objectNode();

        ObjectNode modifiedPayload = transformer.transform(payload, "13", new String[0]);

        assertEquals("HOTELS", modifiedPayload.path("businessSubSector").asString());
        assertFalse(modifiedPayload.has("businessSubSector---accommodation"));
    }

    @Test
    @DisplayName("Should not overwrite an existing base property")
    void shouldNotOverwriteExistingProperty() {
        ObjectNode payload = new TestData()
                .add("businessSubSector", "ORIGINAL")
                .add("businessSubSector---accommodation", "HOTELS")
                .objectNode();

        ObjectNode modifiedPayload = transformer.transform(payload, "13", new String[0]);

        assertEquals("ORIGINAL", modifiedPayload.path("businessSubSector").asText());
        assertFalse(modifiedPayload.has("businessSubSector---accommodation"));
    }

    @Test
    @DisplayName("Should ignore empty variant values")
    void shouldIgnoreEmptyVariantValues() {
        ObjectNode payload = new TestData ()
                .add("businessSubSector---events", "")
                .add("businessSubSector---accommodation", "HOTELS")
                .add("businessSubSector---active", "")
                .objectNode();

        ObjectNode modifiedPayload = transformer.transform(payload, "13", new String[0]);

        assertEquals("HOTELS", modifiedPayload.path("businessSubSector").asString());
        assertFalse(modifiedPayload.has("businessSubSector---events"));
        assertFalse(modifiedPayload.has("businessSubSector---accommodation"));
        assertFalse(modifiedPayload.has("businessSubSector---active"));
    }


}