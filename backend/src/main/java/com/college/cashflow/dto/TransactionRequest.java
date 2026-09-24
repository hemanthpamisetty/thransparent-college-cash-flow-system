package com.college.cashflow.dto;

import com.college.cashflow.enums.PaymentMethod;
import com.college.cashflow.enums.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionRequest {

    @NotNull(message = "Transaction type is required (IN or OUT)")
    private TransactionType transactionType;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    // Optional for DEPT_ADMIN (auto-assigned from user's department), required or optional for MAIN_ADMIN
    private Long departmentId;

    private String description;

    @Size(max = 100, message = "Reference number must not exceed 100 characters")
    private String referenceNumber;

    @NotNull(message = "Transaction date is required")
    private LocalDate transactionDate;
}
