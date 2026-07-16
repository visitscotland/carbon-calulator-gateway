package com.visitscotland.carboncalculator.service;

import com.visitscotland.carboncalculator.exception.TraceApiException;
import com.visitscotland.carboncalculator.exception.VsException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

@Service
public class TraceAPIService {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(TraceAPIService.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${trace-api.key}")
    String apiKey;

    @Value("${trace-api.remove.properties}")
    private String[] propertiesToRemove;

    @Value("${trace-api.base-url}")
    String baseUrl;

    public TraceAPIService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public ResponseEntity<ObjectNode> register(ObjectNode payload, String submissionId) throws VsException {
        String token = getAuthenticationToken();
        String sanitizedPayload = sanitize(payload, submissionId);
        return submit(baseUrl + "/visitscotland/register", sanitizedPayload, token);
    }

    /**
     * Get the authentication token from the Trace API using the provided API key and validates
     * that the service is alive.
     *
     * @return Token for the session
     *
     * @throws VsException when the authentication to their service fail.
     */
    public String getAuthenticationToken() throws VsException {
        try {
            ResponseEntity<JsonNode> authentication = authenticate();
            if (authentication.hasBody() && authentication.getBody().has("payload")) {
                ObjectNode payload = authentication.getBody().get("payload").asObject();
                if (payload.has("token")) {
                    return payload.get("token").asString();
                }
            }
        } catch (HttpClientErrorException e) {
            logger.error("Failed to authenticate with Trace API. Status code: {}, message: {}",
                    e.getStatusCode(), e.getMessage());
        }

        throw new VsException("Failed to authenticate with Trace API");
    }

    /**
     * Makes the request to the authentication service
     */
    private ResponseEntity<JsonNode> authenticate(){
        return restTemplate.postForEntity(baseUrl + "/auth/token",
                new HttpEntity<>("", getHeaders(apiKey)), JsonNode.class);
    }

    /**
     * Remove all non-necessary properties and includes the submissionId
     * @param payload
     * @param submissionId
     * @return
     */
    private String sanitize(ObjectNode payload, String submissionId) {
        ObjectNode modifiedPayload = payload.asObject();

        for (String property : propertiesToRemove) {
            modifiedPayload.remove(property);
        }

        modifiedPayload.put("vsUID", submissionId);

        return objectMapper.writeValueAsString(modifiedPayload);
    }

    private ResponseEntity<ObjectNode> submit(String url, String payload, String token) throws HttpClientErrorException {
        HttpEntity<String> entity = new HttpEntity<>(payload, getHeaders(token));

        try {
            return restTemplate.exchange(url, HttpMethod.PUT, entity, ObjectNode.class);
        } catch (HttpClientErrorException e) {
//            ObjectNode errorMessage = e.getResponseBodyAs(ObjectNode.class);
//            if (errorMessage != null && errorMessage.has("status")
//                    && errorMessage.get("status").asInt() == HttpStatus.UNAUTHORIZED.value()) {
//                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorMessage);
//            }
            throw new TraceApiException("Error submitting to Trace API.", e);
        }

    }

    private HttpHeaders getHeaders(String authorization){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", authorization);

        return headers;
    }


}
