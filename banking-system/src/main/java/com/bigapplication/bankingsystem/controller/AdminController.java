package com.bigapplication.bankingsystem.controller;

import com.bigapplication.bankingsystem.dto.request.CreateAccountRequest;
import com.bigapplication.bankingsystem.dto.response.AccountResponse;
import com.bigapplication.bankingsystem.dto.response.ApiResponse;
import com.bigapplication.bankingsystem.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/banking/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping("/accounts")
    public ResponseEntity<ApiResponse<AccountResponse>> createAccount(@Valid @RequestBody CreateAccountRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.<AccountResponse>builder()
                .success(true)
                .message("Bank account created successfully")
                .data(adminService.createAccount(request))
                .build());
    }
}
