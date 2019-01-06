package slackmailer.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class SlackApiResponseTest {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    void should_deserialize_ok_response() throws IOException {
        String ok_response = "{\n" +
                "    \"ok\": true,\n" +
                "    \"stuff\": \"This is good\"\n" +
                "}";

        SlackApiResponse response = MAPPER.readValue(ok_response, SlackApiResponse.class);

        assertTrue(response.isOk());
        assertEquals("This is good", response.getStuff());
        assertNull(response.getError());
        assertNull(response.getWarning());
        assertNull(response.getPerson());
    }

    @Test
    void should_deserialize_error_response() throws IOException {
        String error_response = "{\n" +
                "    \"ok\": false,\n" +
                "    \"error\": \"something_bad\"\n" +
                "}";

        SlackApiResponse response = MAPPER.readValue(error_response, SlackApiResponse.class);

        assertFalse(response.isOk());
        assertEquals("something_bad", response.getError());
        assertNull(response.getStuff());
        assertNull(response.getWarning());
        assertNull(response.getPerson());
    }

    @Test
    void should_deserialize_warning_response() throws IOException {
        String warning_response = "{\n" +
                "    \"ok\": true,\n" +
                "    \"warning\": \"something_problematic\",\n" +
                "    \"stuff\": \"Your requested information\"\n" +
                "}";

        SlackApiResponse response = MAPPER.readValue(warning_response, SlackApiResponse.class);

        assertTrue(response.isOk());
        assertEquals("something_problematic", response.getWarning());
        assertEquals("Your requested information", response.getStuff());
        assertNull(response.getError());
        assertNull(response.getPerson());
    }
}
