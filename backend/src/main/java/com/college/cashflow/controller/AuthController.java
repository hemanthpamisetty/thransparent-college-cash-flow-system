package com.college.cashflow.controller;

import com.college.cashflow.dto.*;
import com.college.cashflow.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * POST /api/auth/main-admin/login
     * Main Admin login endpoint.
     */
    @PostMapping("/main-admin/login")
    public ResponseEntity<LoginResponse> mainAdminLogin(
            @Valid @RequestBody MainAdminLoginRequest request) {

        LoginResponse response = authService.mainAdminLogin(request);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/auth/dept-admin/login
     * Department Admin login endpoint.
     */
    @PostMapping("/dept-admin/login")
    public ResponseEntity<LoginResponse> deptAdminLogin(
            @Valid @RequestBody DeptAdminLoginRequest request) {

        LoginResponse response = authService.deptAdminLogin(request);
        return ResponseEntity.ok(response);
    }
}
