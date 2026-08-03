package com.visitscotland.ccg.controller;

import com.visitscotland.ccg.client.BregClient;
import com.visitscotland.ccg.client.TraceApiClient;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.ObjectMapper;

import java.util.*;

@RestController
@Profile({"dev", "debug"})
@RequestMapping("/info")
public class InfoController {

    private final ConfigurableEnvironment environment;
    private final BregClient bregClient;
    private final TraceApiClient traceApiClient;
    private final ObjectMapper objectMapper;

    public InfoController(ConfigurableEnvironment environment, BregClient bregClient, TraceApiClient traceApiClient, ObjectMapper objectMapper) {
        this.environment = environment;
        this.bregClient = bregClient;
        this.traceApiClient = traceApiClient;
        this.objectMapper = objectMapper;
    }

    @GetMapping("properties")
    public Map<String, Object> properties() {
        return getJavaProperties();
    }

    public Map<String, Object> getJavaProperties() {

        Map<String, Object> result = new TreeMap<>();

        for (PropertySource<?> source : environment.getPropertySources()) {
            if (source instanceof EnumerablePropertySource<?> enumerable) {
                for (String name : enumerable.getPropertyNames()) {
                    result.putIfAbsent(name, environment.getProperty(name));
                }
            }
        }

        return result;
    }

    @GetMapping("health")
    public List<String[]> getDownstreamHealth() {

        List<String[]> result = new ArrayList<>();

        result.add(new String[]{"Trace API - authentication", time(traceApiClient::getAuthenticationToken)});
        result.add(new String[]{"Trace API - /register", time(() ->
                traceApiClient.register(objectMapper.createObjectNode(), "test")
        )});
        result.add(new String[]{"BREG", time(bregClient::healthCheck)});

        return result;
    }

    private String time(Runnable action) {
        long start = System.currentTimeMillis();
        try {
            action.run();
        } catch (Exception ignored) {
            // An Exception is an acceptable response
        }
        return String.format("%d ms", System.currentTimeMillis() - start);
    }
}
