package com.visitscotland.ccg.controller;

import com.visitscotland.ccg.exception.TraceApiException;
import com.visitscotland.ccg.exception.VsException;
import com.visitscotland.ccg.service.BregService;
import com.visitscotland.ccg.service.RecaptchaService;
import com.visitscotland.ccg.service.TraceAPIService;
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

    private final BregService bregService;
    private final RecaptchaService recaptchaService;
    private final TraceAPIService traceAPIService;
    private final GlobalExceptionHandler globalExceptionHandler;


    public MainController(BregService bregService, RecaptchaService recaptchaService, TraceAPIService traceAPIService,
                          GlobalExceptionHandler globalExceptionHandler) {
        this.bregService = bregService;
        this.recaptchaService = recaptchaService;
        this.traceAPIService = traceAPIService;
        this.globalExceptionHandler = globalExceptionHandler;
    }

    @GetMapping("/health")
    public String health () {
        return "Status OK!";
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
        boolean traceapiFailure = false;
        boolean bregFailure = false;

        try {
            traceAPIService.register(payload.deepCopy().asObject(), uuid);
        } catch (TraceApiException e) {
            traceapiFailure = true;
        }

        try {
            bregService.sendRequest(payload, uuid, traceapiFailure);
        } catch (VsException e) {
            bregFailure = true;
        }

        return processResponse(uuid, traceapiFailure, bregFailure);

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
        if (payload.has(RecaptchaService.RECAPTCHA_FIELD_NAME)) {
            captchaResponse = payload.get(RecaptchaService.RECAPTCHA_FIELD_NAME).asString();
        } else {
            captchaResponse = "";
        }
        return recaptchaService.isValidRecaptcha(request, captchaResponse);
    }

}
