package com.visitscotland.carboncalculator.controller;

import com.visitscotland.carboncalculator.exception.VsException;
import com.visitscotland.carboncalculator.service.BregService;
import com.visitscotland.carboncalculator.service.RecaptchaService;
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
        try {
            if (isValidRecaptcha(request, payload)) {
                return processRequest(payload);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Recaptcha key");
            }
        } catch (VsException e) {
            //TODO logger.error
            return ResponseEntity.status(HttpStatus.valueOf(500)).body("The service encountered an error. Please try again later. ");
        }
    }

    private ResponseEntity<?> processRequest(JsonNode payload) throws VsException {
        String uuid = UUID.randomUUID().toString();
//        bregService.sendRequest(payload, uuid);
        return bregService.sendRequest(payload, uuid);
        //ResponseEntity.ok(payload);


    }

    private boolean isValidRecaptcha(HttpServletRequest request, JsonNode payload){
        String captchaResponse;
        if (payload.has(RecaptchaService.RECAPTCHA_FIELD_NAME)) {
            captchaResponse = payload.get(RecaptchaService.RECAPTCHA_FIELD_NAME).asString();
        } else {
            captchaResponse = "";
        }
        return recaptchaService.isValidRecaptcha(request, captchaResponse);
    }
}
