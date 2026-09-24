package com.college.cashflow.controller;

import com.college.cashflow.dto.CategoryRequest;
import com.college.cashflow.dto.CategoryResponse;
import com.college.cashflow.dto.StatusUpdateRequest;
import com.college.cashflow.enums.CategoryType;
import com.college.cashflow.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    /**
     * GET /api/categories
     * Get all categories, optionally filtered by ?type=IN or ?type=OUT.
     */
    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAllCategories(
            @RequestParam(required = false) CategoryType type) {
        return ResponseEntity.ok(categoryService.getAllCategories(type));
    }

    /**
     * GET /api/categories/{id}
     * Get category by id.
     */
    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getCategoryById(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }

    /**
     * POST /api/categories
     * Create category. ONLY MAIN_ADMIN.
     */
    @PostMapping
    @PreAuthorize("hasRole('MAIN_ADMIN')")
    public ResponseEntity<CategoryResponse> createCategory(
            @Valid @RequestBody CategoryRequest request) {
        CategoryResponse created = categoryService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * PUT /api/categories/{id}
     * Update category. ONLY MAIN_ADMIN.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MAIN_ADMIN')")
    public ResponseEntity<CategoryResponse> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.ok(categoryService.updateCategory(id, request));
    }

    /**
     * PATCH /api/categories/{id}/status
     * Activate/Deactivate category. ONLY MAIN_ADMIN.
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('MAIN_ADMIN')")
    public ResponseEntity<CategoryResponse> toggleStatus(
            @PathVariable Long id,
            @Valid @RequestBody StatusUpdateRequest request) {
        return ResponseEntity.ok(categoryService.toggleStatus(id, request.getActive()));
    }
}
