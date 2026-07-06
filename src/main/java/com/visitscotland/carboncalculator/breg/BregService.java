package com.visitscotland.carboncalculator.breg;

import com.visitscotland.carboncalculator.exception.VsException;
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

    public ResponseEntity<String> sendRequest(JsonNode payload, String fileUrl) throws VsException {

        if (serviceEnabled) {
            //This means to be an absurd error message to quickly identify the issue
            return ResponseEntity.status(HttpStatus.I_AM_A_TEAPOT)
                    .body("This application is in development mode");
        }

        try {
            // Create a copy of the payload and remove specified properties
            ObjectNode modifiedPayload = payload.deepCopy().asObject();
            
            for (String property : propertiesToRemove) {
                modifiedPayload.remove(property);
            }

            modifiedPayload.put("obs_url", fileUrl);
            
            logger.info("Sending request to BREG service at: {}", bregServiceUrl);
            //TODO only if the application is in debug (non-production) mode
            logger.debug("Modified payload: {}", modifiedPayload);

            // Set up headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            // Create request entity
            HttpEntity<String> requestEntity = new HttpEntity<>(
                objectMapper.writeValueAsString(modifiedPayload), 
                headers
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
}