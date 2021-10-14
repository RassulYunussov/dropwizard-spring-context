package com.example.springconfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = {"com.example.services"})
public class SpringConfiguration {
    private static final Logger LOG = LoggerFactory.getLogger(SpringConfiguration.class);
    public SpringConfiguration() {
        LOG.info("SpringConfiguration Created!");
    }
}