package com.bigapplication.bankingsystem.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

    @NotBlank
    private String issuer = "banking-system";

    @NotBlank
    private String secret;

    @Min(1)
    private long accessTokenExpirationMinutes = 60;

    @Min(1)
    private long refreshTokenExpirationMinutes = 10080;

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public long getAccessTokenExpirationMinutes() {
        return accessTokenExpirationMinutes;
    }

    public void setAccessTokenExpirationMinutes(long accessTokenExpirationMinutes) {
        this.accessTokenExpirationMinutes = accessTokenExpirationMinutes;
    }

    public long getRefreshTokenExpirationMinutes() {
        return refreshTokenExpirationMinutes;
    }

    public void setRefreshTokenExpirationMinutes(long refreshTokenExpirationMinutes) {
        this.refreshTokenExpirationMinutes = refreshTokenExpirationMinutes;
    }
}
