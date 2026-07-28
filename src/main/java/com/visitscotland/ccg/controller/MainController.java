package com.visitscotland.ccg.controller;

import com.visitscotland.ccg.exception.TraceApiException;
import com.visitscotland.ccg.exception.VsException;
import com.visitscotland.ccg.client.BregClient;
import com.visitscotland.ccg.client.RecaptchaClient;
import com.visitscotland.ccg.client.TraceApiClient;
import com.visitscotland.ccg.model.RegisterResponse;
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

    private static final Logger logger = LoggerFactory.getLogger(MainController.class);

    public static final String INVALID_RECAPTCHA = "INVALID_RECAPTCHA";
    public static final String BOTH_SUBMISSIONS_FAILED = "BOTH_SUBMISSIONS_FAILED";
    public static final String TRACE_API_FAILED = "TRACE_API_FAILED";
    public static final String BREG_FAILED = "BREG_FAILED";
    public static final String SUCCESS = "SUCCESS";
    public static final String EXISTING_USER = "EXISTING_USER";

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
        return "Status OK!";
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@RequestBody JsonNode payload, HttpServletRequest request) {
        if (isValidRecaptcha(request, payload)) {
            return processRequest(payload);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new RegisterResponse(null, INVALID_RECAPTCHA));
        }
    }

    private ResponseEntity<RegisterResponse> processRequest(JsonNode payload) throws VsException {
        String uuid = UUID.randomUUID().toString();
        logger.info("Processing submission with UUID: {}", uuid);
        TraceApiException traceApiException = null;
        VsException vsException = null;

        try {
            traceAPIService.register(payload.asObject(), uuid);
        } catch (TraceApiException e) {
            logger.error(e.getApiMessage());
            if (e.getApiMessage().contains("User already exists with this email")) {
                logger.info("User already exists with the email");
                return new ResponseEntity<>(new RegisterResponse(uuid, EXISTING_USER), HttpStatus.CONFLICT);
            } else {
                traceApiException = e;
            }
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

    private ResponseEntity<RegisterResponse> processResponse(String submissionId, boolean traceApiFailure, boolean bregFailure) {
        if (traceApiFailure && bregFailure) {
            logger.error("Both TraceAPI and BREG service calls failed for submission UUID: {}", submissionId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new RegisterResponse(submissionId, BOTH_SUBMISSIONS_FAILED));
        } else if (traceApiFailure || bregFailure) {
            logger.error("One of the services could not process the submission, SubmissionId: {}, Trace API:{}, BREG: {}",
                    submissionId, traceApiFailure?"FAIL": "SUCCESS", bregFailure?"FAIL": "SUCCESS");

            return ResponseEntity.status(HttpStatus.ACCEPTED).body(new RegisterResponse(submissionId, (traceApiFailure ? TRACE_API_FAILED : BREG_FAILED)));
        } else {
            return ResponseEntity.ok(new RegisterResponse(submissionId, SUCCESS));
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
