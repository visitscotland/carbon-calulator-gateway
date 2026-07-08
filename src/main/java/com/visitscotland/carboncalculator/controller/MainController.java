package com.visitscotland.carboncalculator.controller;

import com.visitscotland.carboncalculator.service.BregService;
import com.visitscotland.carboncalculator.service.RecaptchaService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.JsonNode;

@RestController
@RequestMapping("/")
public class MainController {

    private final BregService bregService;
    private final RecaptchaService recaptchaService;

    public MainController(BregService bregService, RecaptchaService recaptchaService) {
        this.bregService = bregService;
        this.recaptchaService = recaptchaService;
    }

    @GetMapping("/health")
    public String health () {
        return "Status OK!";
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody JsonNode payload, HttpServletRequest request) {
        if (isValidRecaptcha(request, payload)) {
            return processRequest(payload);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Recaptcha key");
        }
    }

    private ResponseEntity<?> processRequest(JsonNode payload) {
        return ResponseEntity.ok(payload);
    }

    private boolean isValidRecaptcha(HttpServletRequest request, JsonNode payload){
        String captchaResponse;
        if (payload.has(RecaptchaService.RECAPTCHA_FIELD_NAME)) {
            captchaResponse = payload.get(RecaptchaService.RECAPTCHA_FIELD_NAME).asString();
        } else {
            captchaResponse = "";
        }
        return recaptchaService.captchaCheck(request.getRemoteAddr(), captchaResponse);
    }
}
