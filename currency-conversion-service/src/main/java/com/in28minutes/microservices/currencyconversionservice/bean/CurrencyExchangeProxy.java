package com.in28minutes.microservices.currencyconversionservice.bean;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// Declare this interface as a Feign Client
// 'name' can be used for service discovery taken from the application.properties of currency exchange service.
// 'url' is the base URL for the external service.
@FeignClient(name = "currency-exchange-service", url = "localhost:8000")
public interface CurrencyExchangeProxy {

    // Define the REST endpoint this client should call
    // This maps to: GET http://localhost:8000/currency-exchange/from/{from}/to/{to}
    @GetMapping("/currency-exchange/from/{from}/to/{to}")
    public CurrencyConversion retrieveExchangeValue(
            @PathVariable String from,
            @PathVariable String to);
}

