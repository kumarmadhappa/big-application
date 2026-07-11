package com.bigapplication.bankingsystem.controller;

import com.bigapplication.bankingsystem.dto.response.AccountResponse;
import com.bigapplication.bankingsystem.dto.response.ApiResponse;
import com.bigapplication.bankingsystem.service.AccountService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/banking/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/mine")
    public ResponseEntity<ApiResponse<List<AccountResponse>>> myAccounts(Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.<List<AccountResponse>>builder()
                .success(true)
                .message("Accounts fetched successfully")
                .data(accountService.getAccountsForCurrentUser(authentication.getName()))
                .build());
    }
}
