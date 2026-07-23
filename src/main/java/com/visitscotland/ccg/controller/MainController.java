package com.visitscotland.ccg.controller;

import com.visitscotland.ccg.exception.TraceApiException;
import com.visitscotland.ccg.exception.VsException;
import com.visitscotland.ccg.client.BregClient;
import com.visitscotland.ccg.client.RecaptchaClient;
import com.visitscotland.ccg.client.TraceApiClient;
import com.visitscotland.ccg.notification.EmailNotificationService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.JsonNode;

import java.util.UUID;

@RestController
@RequestMapping("/")
public class MainController {

    private final static Logger logger = LoggerFactory.getLogger(MainController.class);

    private final BregClient bregService;
    private final RecaptchaClient recaptchaService;
    private final TraceApiClient traceAPIService;
    private final EmailNotificationService emailNotificationService;


    public MainController(BregClient bregService, RecaptchaClient recaptchaService, TraceApiClient traceAPIService, EmailNotificationService emailNotificationService) {
        this.bregService = bregService;
        this.recaptchaService = recaptchaService;
        this.traceAPIService = traceAPIService;
        this.emailNotificationService = emailNotificationService;
    }

    @GetMapping("/health")
    public String health () {
        return "Status OK!!";
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody JsonNode payload, HttpServletRequest request) {
        if (isValidRecaptcha(request, payload)) {
            return processRequest(payload);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Recaptcha key");
        }
    }

    private ResponseEntity<String> processRequest(JsonNode payload) throws VsException {
        String uuid = UUID.randomUUID().toString();
        logger.info("Processing submission with UUID: {}", uuid);
        TraceApiException traceApiException = null;
        VsException vsException = null;

        try {
            traceAPIService.register(payload.asObject(), uuid);
        } catch (TraceApiException e) {
            logger.error(e.getApiMessage());
            traceApiException = e;
        }

        try {
            bregService.sendRequest(payload, uuid, (traceApiException != null));
        } catch (VsException e) {
            vsException = e;
        }

        if (traceApiException != null || vsException != null) {
            notify(traceApiException, vsException, uuid);
        }

        return processResponse(uuid, traceApiException!=null, vsException != null);
    }

    private void notify(TraceApiException traceApiException, VsException vsException, String uuid) {
        emailNotificationService.notify(traceApiException, vsException, uuid);
    }

    private ResponseEntity<String> processResponse(String submissionId, boolean traceApiFailure, boolean bregFailure) {
        if (traceApiFailure && bregFailure) {
            logger.error("Both TraceAPI and BREG service calls failed for submission UUID: {}", submissionId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Submission failed. Please try again later or contact VisitScotland Team with the following " +
                            "reference number: " + submissionId);
        } else if (traceApiFailure || bregFailure) {
            logger.error("One of the services could not process the submission, SubmissionId: {}, Trace API:{}, BREG: {}",
                    submissionId, traceApiFailure?"FAIL": "SUCCESS", bregFailure?"FAIL": "SUCCESS");
            return ResponseEntity.ok(String.format("Submission completed with errors. " +
                    "Please contact VisitScotland Team with the following reference number: {}", submissionId));
        } else {
            return ResponseEntity.ok(String.format("Submission %s completed successfully", submissionId));
        }
    }

    private boolean isValidRecaptcha(HttpServletRequest request, JsonNode payload) {
        String captchaResponse;
        if (payload.has(RecaptchaClient.RECAPTCHA_FIELD_NAME)) {
            captchaResponse = payload.get(RecaptchaClient.RECAPTCHA_FIELD_NAME).asString();
        } else {
            captchaResponse = "";
        }
        return recaptchaService.isValidRecaptcha(request, captchaResponse);
    }

}
