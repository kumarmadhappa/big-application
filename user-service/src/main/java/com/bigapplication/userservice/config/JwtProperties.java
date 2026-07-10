package com.bigapplication.userservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

    private String issuer ;
    private String secret ;
    private long accessTokenExpirationMinutes = 60;
    private long refreshTokenExpirationMinutes = 10080;
}
