package com.in28minutes.microservices.currencyexchangeservice.controller;

import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class CircuitBreakerController {

    // Create a logger instance for this class to log incoming requests
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @GetMapping("/sample-api")
    @Retry(name = "sample-api", fallbackMethod = "hardcodedResponse")
// When a GET request is made to /sample-api, the method will be retried upon failure
// 'sample-api' refers to configuration in application.properties (ex : by default it will make three retry we can configure it in application .properties.)
// fallbackMethod = "hardcodedResponse" means this method is called if all retries fail
    public String sampleApi() {
        logger.info("Sample api call received");

        // Makes an HTTP GET request to another service (dummy URL for example)
        ResponseEntity<String> forEntity = new RestTemplate()
                .getForEntity("http://localhost:8080/some-dummy-url", String.class);

        // Return the response body received from the external service
        return forEntity.getBody();
    }

    // Fallback method to be called if retries are exhausted or an exception occurs
// Signature must match the original method + Exception parameter
    public String hardcodedResponse(Exception ex) {
        // This will be returned as a fallback if the external call fails multiple times
        return "fallback-response";
    }

}

