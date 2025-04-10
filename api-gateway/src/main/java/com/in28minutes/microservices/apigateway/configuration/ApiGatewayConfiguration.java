package com.in28minutes.microservices.apigateway.configuration;

import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.Buildable;
import org.springframework.cloud.gateway.route.builder.PredicateSpec;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Function;

@Configuration
public class ApiGatewayConfiguration {

    // Define a bean that returns a RouteLocator (the routing map for API Gateway)
    @Bean
    public RouteLocator gatewayRouter(RouteLocatorBuilder builder) {

        // Example static route: redirects "/get" to httpbin.org with a custom header and parameter
        Function<PredicateSpec, Buildable<Route>> routeFunction =
                p -> p.path("/get") // Predicate: match path "/get"
                        .filters(f -> f
                                .addRequestHeader("MyHeader", "MyURI") // Add custom header to the request
                                .addRequestParameter("Param", "MyValue")) // Add custom query param
                        .uri("http://httpbin.org:80"); // Target external service

        return builder.routes()
                // Register the static route
                .route(routeFunction)

                // Dynamic route: forwards "/currency-exchange/**" to currency-exchange-service
                .route(p -> p.path("/currency-exchange/**")
                        .uri("lb://currency-exchange-service") // Use load-balanced URI from service registry
                )

                // Dynamic route: forwards "/currency-conversion/**" to currency-conversion-service
                .route(p -> p.path("/currency-conversion/**")
                        .uri("lb://currency-conversion-service")
                )

                // Another route for Feign-based calls
                .route(p -> p.path("/currency-conversion-feign/**")
                        .uri("lb://currency-conversion-service")
                )

                // Route with a rewrite filter
                // If a request comes to "/currency-conversion-new/**", rewrite it to "/currency-conversion-feign/**"
                .route(p -> p.path("/currency-conversion-new/**")
                        .filters(f -> f.rewritePath(
                                "/currency-conversion-new/(?<segment>.*)", // capture everything after prefix
                                "/currency-conversion-feign/${segment}"   // rewrite to feign route
                        ))
                        .uri("lb://currency-conversion-service")
                )

                // Build and return the configured routes
                .build();
    }
}
