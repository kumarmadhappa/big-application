package com.bigapplication.bankingsystem.controller;

import com.bigapplication.bankingsystem.dto.request.TransactionRequest;
import com.bigapplication.bankingsystem.dto.response.ApiResponse;
import com.bigapplication.bankingsystem.dto.response.TransactionResponse;
import com.bigapplication.bankingsystem.service.TransactionService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/banking/accounts/{accountId}/transactions")
public class TransactionController {

    private static final Logger log = LoggerFactory.getLogger(TransactionController.class);

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/deposit")
    public ResponseEntity<ApiResponse<TransactionResponse>> deposit(@PathVariable Long accountId,
                                                                    @Valid @RequestBody TransactionRequest request,
                                                                    Authentication authentication) {
        log.info("Deposit request received for accountId={} by username={} amount={}",
                accountId, authentication.getName(), request.getAmount());
        return ResponseEntity.ok(ApiResponse.<TransactionResponse>builder()
                .success(true)
                .message("Deposit successful")
                .data(transactionService.deposit(accountId, request.getAmount(), authentication.getName()))
                .build());
    }

    @PostMapping("/withdraw")
    public ResponseEntity<ApiResponse<TransactionResponse>> withdraw(@PathVariable Long accountId,
                                                                     @Valid @RequestBody TransactionRequest request,
                                                                     Authentication authentication) {
        log.info("Withdrawal request received for accountId={} by username={} amount={}",
                accountId, authentication.getName(), request.getAmount());
        return ResponseEntity.ok(ApiResponse.<TransactionResponse>builder()
                .success(true)
                .message("Withdrawal successful")
                .data(transactionService.withdraw(accountId, request.getAmount(), authentication.getName()))
                .build());
    }
}
