package com.in28minutes.microservices.currencyconversionservice.controller;

import com.in28minutes.microservices.currencyconversionservice.bean.CurrencyConversion;
import com.in28minutes.microservices.currencyconversionservice.bean.CurrencyExchangeProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;

@RestController
public class CurrencyConversionController {

    @GetMapping("/currency-conversion/from/{from}/to/{to}/quantity/{quantity}")
    public CurrencyConversion calculateCurrencyConversion(
            @PathVariable String from,
            @PathVariable String to,
            @PathVariable BigDecimal quantity
    ) {

        // Step 1: Create a map to hold the path variables to be replaced in the URL
        HashMap<String, String> uriVariables = new HashMap<>();
        uriVariables.put("from", from);  // e.g., from = "USD"
        uriVariables.put("to", to);      // e.g., to = "INR"

// Step 2: Make a REST API call to the Currency Exchange Microservice
// - Using RestTemplate's getForEntity() method
// - URL contains placeholders for 'from' and 'to', which will be replaced with actual values from uriVariables
        ResponseEntity<CurrencyConversion> responseEntity = new RestTemplate().getForEntity(
                "http://localhost:8000/currency-exchange/from/{from}/to/{to}", // Target endpoint
                CurrencyConversion.class,                                      // Expected response type
                uriVariables                                                   // Path variable values to be substituted
        );

// Step 3: Extract the response body (which is of type CurrencyConversion)
        CurrencyConversion currencyConversion = responseEntity.getBody();

// Step 4: Create and return a new CurrencyConversion object using the fetched data
// - Includes the original 'from', 'to', and 'quantity'
// - Multiplies 'quantity' by 'conversionMultiple' to calculate total converted amount
// - Passes through the environment info to know which instance or service responded
        return new CurrencyConversion(
                currencyConversion.getId(),                         // ID of the conversion record
                from,                                                // Source currency (e.g., USD)
                to,                                                  // Target currency (e.g., INR)
                quantity,                                            // Quantity to convert (e.g., 100)
                currencyConversion.getConversionMultiple(),          // Exchange rate (e.g., 82.5)
                quantity.multiply(currencyConversion.getConversionMultiple()), // Total calculated amount (e.g., 100 * 82.5 = 8250)
                currencyConversion.getEnvironment()                  // Info about the environment (like port or instance)
        );
    }

    // Injecting the Feign Client (interface that talks to currency-exchange service)
    @Autowired
    private CurrencyExchangeProxy proxy;

    @GetMapping("/currency-conversion-feign/from/{from}/to/{to}/quantity/{quantity}")
    public CurrencyConversion calculateCurrencyConversionFeign(
            @PathVariable String from,
            @PathVariable String to,
            @PathVariable BigDecimal quantity
    ) {
        // Step 1: Call the external microservice using Feign client method
        // - This automatically makes a GET call to:
        //   /currency-exchange/from/{from}/to/{to}
        // - The Feign interface handles building the URL and parsing the response.
        CurrencyConversion currencyConversion = proxy.retrieveExchangeValue(from, to);

        // Step 2: Construct a response object for the currency-conversion API
        // - Calculate total amount = quantity * conversionMultiple
        // - Append "feign" to the environment field to identify the method used
        return new CurrencyConversion(
                currencyConversion.getId(),                             // ID of the transaction
                from, to, quantity,                                     // Currency details
                currencyConversion.getConversionMultiple(),             // Exchange rate
                quantity.multiply(currencyConversion.getConversionMultiple()), // Total amount
                currencyConversion.getEnvironment() + " feign"          // Add 'feign' to track origin
        );
    }

}
