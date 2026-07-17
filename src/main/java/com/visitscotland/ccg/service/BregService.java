package com.visitscotland.ccg.service;

import com.visitscotland.ccg.exception.VsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

@Service
public class BregService {

    private static final Logger logger = LoggerFactory.getLogger(BregService.class);
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    @Value("${breg.service.url}")
    private String bregServiceUrl;
    
    @Value("${breg.remove.properties}")
    private String[] propertiesToRemove;

    @Value("${breg.enabled}")
    private boolean serviceEnabled;

    public BregService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public ResponseEntity<String> sendRequest(JsonNode payload, String submissionId, boolean traceApiFailure) throws VsException {

        if (!serviceEnabled) {
            //This means to be an absurd error message to quickly identify the issue
            return ResponseEntity.status(HttpStatus.I_AM_A_TEAPOT)
                    .body("This application is in development mode");
        }

        try {
            logger.info("Sending request to BREG service at: {}", bregServiceUrl);

            HttpEntity<String> requestEntity = new HttpEntity<>(
                sanitize(payload, submissionId, traceApiFailure),
                getHeaders()
            );
            
            // Send POST request
            ResponseEntity<String> response = restTemplate.postForEntity(
                bregServiceUrl, 
                requestEntity, 
                String.class
            );
            
            logger.info("BREG service response status: {}", response.getStatusCode());
            return response;
            
        } catch (Exception e) {
            logger.error("Error sending request to BREG service: {}", e.getMessage());
            throw new VsException("Failed to send request to BREG service", e);
        }
    }

    /**
     * Remove all non-necessary properties and includes the submissionId
     * @param payload
     * @param submissionId
     * @return
     */
    private String sanitize(JsonNode payload, String submissionId, boolean traceApiFailure) {
        ObjectNode modifiedPayload = payload.asObject();

        for (String property : propertiesToRemove) {
            modifiedPayload.remove(property);
        }

        modifiedPayload.put("vsUID", submissionId);
        if (traceApiFailure) {
            modifiedPayload.put("traceApiFailure", traceApiFailure);
        }

        return objectMapper.writeValueAsString(modifiedPayload);
    }

    private HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        return headers;
    }
}