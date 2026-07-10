package com.bigapplication.apigateway;

import com.bigapplication.apigateway.config.JwtProperties;
import com.bigapplication.apigateway.config.RateLimitProperties;
import com.bigapplication.apigateway.config.RouteProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan(basePackageClasses = {
        JwtProperties.class,
        RateLimitProperties.class,
        RouteProperties.class
})
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
