package com.nttdata.bank.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Single HTTP entry point. Routes are resolved through Eureka.
 * This module uses Spring Cloud Gateway Server MVC so the project stays on Spring MVC and RxJava.
 */
@SpringBootApplication
public class GatewayApplication {

    /**
     * Starts the API gateway.
     *
     * @param args program arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}
