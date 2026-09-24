package com.college.cashflow.controller;

import com.college.cashflow.dto.DeptAdminCreateRequest;
import com.college.cashflow.dto.DeptAdminResponse;
import com.college.cashflow.dto.DeptAdminUpdateRequest;
import com.college.cashflow.dto.StatusUpdateRequest;
import com.college.cashflow.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * GET /api/users/dept-admins
     * List all Department Admins. ONLY MAIN_ADMIN.
     */
    @GetMapping("/dept-admins")
    @PreAuthorize("hasRole('MAIN_ADMIN')")
    public ResponseEntity<List<DeptAdminResponse>> getAllDeptAdmins() {
        return ResponseEntity.ok(userService.getAllDeptAdmins());
    }

    /**
     * GET /api/users/dept-admins/{id}
     * Get single Department Admin. ONLY MAIN_ADMIN.
     */
    @GetMapping("/dept-admins/{id}")
    @PreAuthorize("hasRole('MAIN_ADMIN')")
    public ResponseEntity<DeptAdminResponse> getDeptAdminById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getDeptAdminById(id));
    }

    /**
     * POST /api/users/dept-admin
     * Create a new Department Admin. ONLY MAIN_ADMIN.
     */
    @PostMapping("/dept-admin")
    @PreAuthorize("hasRole('MAIN_ADMIN')")
    public ResponseEntity<DeptAdminResponse> createDeptAdmin(
            @Valid @RequestBody DeptAdminCreateRequest request,
            Authentication authentication) {
        String mainAdminUsername = authentication.getName();
        DeptAdminResponse created = userService.createDeptAdmin(request, mainAdminUsername);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * PUT /api/users/dept-admins/{id}
     * Update Department Admin. ONLY MAIN_ADMIN.
     */
    @PutMapping("/dept-admins/{id}")
    @PreAuthorize("hasRole('MAIN_ADMIN')")
    public ResponseEntity<DeptAdminResponse> updateDeptAdmin(
            @PathVariable Long id,
            @Valid @RequestBody DeptAdminUpdateRequest request) {
        return ResponseEntity.ok(userService.updateDeptAdmin(id, request));
    }

    /**
     * PATCH /api/users/dept-admins/{id}/status
     * Activate/Deactivate Department Admin. ONLY MAIN_ADMIN.
     */
    @PatchMapping("/dept-admins/{id}/status")
    @PreAuthorize("hasRole('MAIN_ADMIN')")
    public ResponseEntity<DeptAdminResponse> toggleStatus(
            @PathVariable Long id,
            @Valid @RequestBody StatusUpdateRequest request) {
        return ResponseEntity.ok(userService.toggleDeptAdminStatus(id, request.getActive()));
    }
}
