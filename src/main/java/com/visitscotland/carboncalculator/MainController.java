package com.visitscotland.carboncalculator;

import com.visitscotland.carboncalculator.breg.BregService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.JsonNode;

@RestController
@RequestMapping("/")
public class MainController {

    private final BregService bregService;

    public MainController(BregService bregService) {
        this.bregService = bregService;
    }

    @GetMapping("/health")
    public String health () {
        return "Status OK!";
    }

    @PostMapping("/register")
    public ResponseEntity<JsonNode> register(@RequestBody JsonNode payload) {
        return ResponseEntity.ok(payload);
    }

}
