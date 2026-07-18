package com.bigapplication.bankingsystem.controller;

import com.bigapplication.bankingsystem.dto.request.CreateAccountRequest;
import com.bigapplication.bankingsystem.dto.request.TransactionRequest;
import com.bigapplication.bankingsystem.dto.request.UpdateAccountRequest;
import com.bigapplication.bankingsystem.dto.response.AccountResponse;
import com.bigapplication.bankingsystem.dto.response.ApiResponse;
import com.bigapplication.bankingsystem.dto.response.TransactionResponse;
import com.bigapplication.bankingsystem.service.AdminService;
import com.bigapplication.bankingsystem.service.TransactionService;
import jakarta.validation.Valid;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/banking/admin")
public class AdminController {

    private static final Logger log = LoggerFactory.getLogger(AdminController.class);

    private final AdminService adminService;
    private final TransactionService transactionService;

    public AdminController(AdminService adminService, TransactionService transactionService) {
        this.adminService = adminService;
        this.transactionService = transactionService;
    }

    @PostMapping("/accounts")
    public ResponseEntity<ApiResponse<AccountResponse>> createAccount(@Valid @RequestBody CreateAccountRequest request) {
        log.info("Banking admin account creation request received for username={} holder={}",
                request.getUsername(), request.getDisplayName());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.<AccountResponse>builder()
                .success(true)
                .message("Bank account created successfully")
                .data(adminService.createAccount(request))
                .build());
    }

    @GetMapping("/accounts")
    public ResponseEntity<ApiResponse<List<AccountResponse>>> getAllAccounts() {
        log.info("Banking admin list accounts request received");
        return ResponseEntity.ok(ApiResponse.<List<AccountResponse>>builder()
                .success(true)
                .message("Accounts fetched successfully")
                .data(adminService.getAllAccounts())
                .build());
    }

    @PutMapping("/accounts/{accountId}")
    public ResponseEntity<ApiResponse<AccountResponse>> updateAccount(@PathVariable Long accountId,
                                                                      @Valid @RequestBody UpdateAccountRequest request) {
        log.info("Banking admin update account request received for accountId={}", accountId);
        return ResponseEntity.ok(ApiResponse.<AccountResponse>builder()
                .success(true)
                .message("Account updated successfully")
                .data(adminService.updateAccount(accountId, request))
                .build());
    }

    @DeleteMapping("/accounts/{accountId}")
    public ResponseEntity<ApiResponse<Void>> deleteAccount(@PathVariable Long accountId) {
        log.info("Banking admin delete account request received for accountId={}", accountId);
        adminService.deleteAccount(accountId);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Account deleted successfully")
                .data(null)
                .build());
    }

    @PostMapping("/accounts/{accountId}/transactions/deposit")
    public ResponseEntity<ApiResponse<TransactionResponse>> deposit(@PathVariable Long accountId,
                                                                    @Valid @RequestBody TransactionRequest request,
                                                                    Authentication authentication) {
        log.info("Banking admin deposit request received for accountId={} by username={} amount={}",
                accountId, authentication.getName(), request.getAmount());
        return ResponseEntity.ok(ApiResponse.<TransactionResponse>builder()
                .success(true)
                .message("Deposit successful")
                .data(transactionService.deposit(accountId, request.getAmount(), authentication.getName(), true))
                .build());
    }

    @PostMapping("/accounts/{accountId}/transactions/withdraw")
    public ResponseEntity<ApiResponse<TransactionResponse>> withdraw(@PathVariable Long accountId,
                                                                     @Valid @RequestBody TransactionRequest request,
                                                                     Authentication authentication) {
        log.info("Banking admin withdrawal request received for accountId={} by username={} amount={}",
                accountId, authentication.getName(), request.getAmount());
        return ResponseEntity.ok(ApiResponse.<TransactionResponse>builder()
                .success(true)
                .message("Withdrawal successful")
                .data(transactionService.withdraw(accountId, request.getAmount(), authentication.getName(), true))
                .build());
    }
}
