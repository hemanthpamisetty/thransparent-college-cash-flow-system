package com.college.cashflow.controller;

import com.college.cashflow.dto.DepartmentRequest;
import com.college.cashflow.dto.DepartmentResponse;
import com.college.cashflow.dto.StatusUpdateRequest;
import com.college.cashflow.service.DepartmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    /**
     * GET /api/departments
     * List all departments. Accessible by authenticated users.
     */
    @GetMapping
    public ResponseEntity<List<DepartmentResponse>> getAllDepartments() {
        return ResponseEntity.ok(departmentService.getAllDepartments());
    }

    /**
     * GET /api/departments/{id}
     * Get one department by id.
     */
    @GetMapping("/{id}")
    public ResponseEntity<DepartmentResponse> getDepartmentById(@PathVariable Long id) {
        return ResponseEntity.ok(departmentService.getDepartmentById(id));
    }

    /**
     * POST /api/departments
     * Create department. ONLY MAIN_ADMIN.
     */
    @PostMapping
    @PreAuthorize("hasRole('MAIN_ADMIN')")
    public ResponseEntity<DepartmentResponse> createDepartment(
            @Valid @RequestBody DepartmentRequest request) {
        DepartmentResponse created = departmentService.createDepartment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * PUT /api/departments/{id}
     * Update department. ONLY MAIN_ADMIN.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MAIN_ADMIN')")
    public ResponseEntity<DepartmentResponse> updateDepartment(
            @PathVariable Long id,
            @Valid @RequestBody DepartmentRequest request) {
        return ResponseEntity.ok(departmentService.updateDepartment(id, request));
    }

    /**
     * PATCH /api/departments/{id}/status
     * Activate/Deactivate department. ONLY MAIN_ADMIN.
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('MAIN_ADMIN')")
    public ResponseEntity<DepartmentResponse> toggleStatus(
            @PathVariable Long id,
            @Valid @RequestBody StatusUpdateRequest request) {
        return ResponseEntity.ok(departmentService.toggleStatus(id, request.getActive()));
    }
}
