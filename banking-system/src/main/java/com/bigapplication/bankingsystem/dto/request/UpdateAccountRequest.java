package com.bigapplication.bankingsystem.dto.request;

import com.bigapplication.bankingsystem.entity.AccountSegment;
import com.bigapplication.bankingsystem.entity.AccountType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateAccountRequest {

    @NotBlank
    @Size(max = 150)
    private String displayName;

    @NotNull
    private AccountSegment accountSegment;

    @NotNull
    private AccountType accountType;

    @DecimalMin(value = "0.00")
    private BigDecimal creditLimit;
}
