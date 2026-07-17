package com.visitscotland.carboncalculator.controller;

import com.visitscotland.carboncalculator.exception.VsException;
import com.visitscotland.carboncalculator.service.BregService;
import com.visitscotland.carboncalculator.service.RecaptchaService;
import com.visitscotland.carboncalculator.service.TraceAPIService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.JsonNode;

import java.util.UUID;

@RestController
@RequestMapping("/")
public class MainController {

    private final BregService bregService;
    private final RecaptchaService recaptchaService;
    private final TraceAPIService traceAPIService;

    public MainController(BregService bregService, RecaptchaService recaptchaService, TraceAPIService traceAPIService) {
        this.bregService = bregService;
        this.recaptchaService = recaptchaService;
        this.traceAPIService = traceAPIService;
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

        traceAPIService.register(payload.deepCopy().asObject(), uuid);
        bregService.sendRequest(payload, uuid);

        return ResponseEntity.ok("Submission completed successfully");
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
