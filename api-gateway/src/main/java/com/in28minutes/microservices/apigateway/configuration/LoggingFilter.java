package com.in28minutes.microservices.apigateway.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class LoggingFilter implements GlobalFilter {

    // Logger instance using SLF4J
    private Logger logger = LoggerFactory.getLogger(LoggingFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // Capture the request path
        String requestPath = exchange.getRequest().getPath().toString();

        // Record start time
        long startTime = System.currentTimeMillis();

        // Log the incoming request path
        logger.info("Request received -> Path: {}", requestPath);

        // Continue the filter chain and add a post-processing step using then(...)
        return chain.filter(exchange).then(
                Mono.fromRunnable(() -> {
                    // Calculate time taken after response is processed
                    long duration = System.currentTimeMillis() - startTime;

                    // Log the time taken to process the request
                    logger.info("Request path [{}] processed in {} ms", requestPath, duration);
                })
        );
    }
}
