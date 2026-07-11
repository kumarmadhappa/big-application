package com.bigapplication.bankingsystem;

import com.bigapplication.bankingsystem.config.AdminProperties;
import com.bigapplication.bankingsystem.config.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan(basePackageClasses = {
        JwtProperties.class,
        AdminProperties.class
})
public class BankingSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(BankingSystemApplication.class, args);
    }
}
