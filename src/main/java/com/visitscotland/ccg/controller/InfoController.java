package com.visitscotland.ccg.controller;

import org.springframework.beans.factory.config.PropertyResourceConfigurer;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.TreeMap;

@RestController
@Profile({"dev", "debug"})
@RequestMapping("/info")
public class InfoController {

    private final ConfigurableEnvironment environment;
    private final PropertyResourceConfigurer propertyResourceConfigurer;

    public InfoController(ConfigurableEnvironment environment, PropertyResourceConfigurer propertyResourceConfigurer) {
        this.environment = environment;
        this.propertyResourceConfigurer = propertyResourceConfigurer;
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
}
