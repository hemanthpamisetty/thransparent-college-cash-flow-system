package com.college.cashflow.controller;

import com.college.cashflow.dto.CashflowSummaryResponse;
import com.college.cashflow.dto.TransactionRequest;
import com.college.cashflow.dto.TransactionResponse;
import com.college.cashflow.enums.PaymentMethod;
import com.college.cashflow.enums.TransactionType;
import com.college.cashflow.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    /**
     * POST /api/transactions
     * Record a new Money IN or Money OUT transaction.
     * Allowed: MAIN_ADMIN, DEPT_ADMIN
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('MAIN_ADMIN', 'DEPT_ADMIN')")
    public ResponseEntity<TransactionResponse> createTransaction(
            @Valid @RequestBody TransactionRequest request,
            Authentication authentication) {
        TransactionResponse response = transactionService.createTransaction(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /api/transactions
     * List and filter transactions.
     * Allowed: MAIN_ADMIN, DEPT_ADMIN
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('MAIN_ADMIN', 'DEPT_ADMIN')")
    public ResponseEntity<List<TransactionResponse>> getTransactions(
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) TransactionType transactionType,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) PaymentMethod paymentMethod,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String search,
            Authentication authentication) {
        List<TransactionResponse> responses = transactionService.getTransactions(
                departmentId, transactionType, categoryId, paymentMethod, startDate, endDate, search, authentication.getName()
        );
        return ResponseEntity.ok(responses);
    }

    /**
     * GET /api/transactions/{id}
     * Get transaction by ID.
     * Allowed: MAIN_ADMIN, DEPT_ADMIN
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MAIN_ADMIN', 'DEPT_ADMIN')")
    public ResponseEntity<TransactionResponse> getTransactionById(
            @PathVariable Long id,
            Authentication authentication) {
        return ResponseEntity.ok(transactionService.getTransactionById(id, authentication.getName()));
    }

    /**
     * PUT /api/transactions/{id}
     * Update an existing transaction.
     * Allowed: MAIN_ADMIN, DEPT_ADMIN
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('MAIN_ADMIN', 'DEPT_ADMIN')")
    public ResponseEntity<TransactionResponse> updateTransaction(
            @PathVariable Long id,
            @Valid @RequestBody TransactionRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(transactionService.updateTransaction(id, request, authentication.getName()));
    }

    /**
     * DELETE /api/transactions/{id}
     * Delete a transaction.
     * Allowed: MAIN_ADMIN, DEPT_ADMIN
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('MAIN_ADMIN', 'DEPT_ADMIN')")
    public ResponseEntity<Void> deleteTransaction(
            @PathVariable Long id,
            Authentication authentication) {
        transactionService.deleteTransaction(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/transactions/summary
     * Get aggregate cashflow summary (Total Income, Expense, Balance, Counts).
     * Allowed: MAIN_ADMIN, DEPT_ADMIN
     */
    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('MAIN_ADMIN', 'DEPT_ADMIN')")
    public ResponseEntity<CashflowSummaryResponse> getCashflowSummary(
            @RequestParam(required = false) Long departmentId,
            Authentication authentication) {
        return ResponseEntity.ok(transactionService.getCashflowSummary(departmentId, authentication.getName()));
    }
}
