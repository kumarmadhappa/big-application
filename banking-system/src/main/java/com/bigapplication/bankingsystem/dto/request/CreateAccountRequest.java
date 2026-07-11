package com.bigapplication.bankingsystem.dto.request;

import com.bigapplication.bankingsystem.entity.AccountSegment;
import com.bigapplication.bankingsystem.entity.AccountType;
import com.bigapplication.bankingsystem.entity.HolderType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateAccountRequest {

    @NotNull
    private HolderType holderType;

    @NotBlank
    @Size(max = 150)
    private String displayName;

    @NotBlank
    @Size(min = 4, max = 50)
    private String username;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    @Size(min = 8, max = 120)
    private String password;

    @NotNull
    private AccountSegment accountSegment;

    @NotNull
    private AccountType accountType;

    @NotNull
    @DecimalMin(value = "0.00")
    private BigDecimal initialBalance;

    @DecimalMin(value = "0.00")
    private BigDecimal creditLimit;
}
