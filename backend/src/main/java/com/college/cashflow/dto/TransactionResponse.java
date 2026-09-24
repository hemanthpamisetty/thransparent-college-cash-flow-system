package com.college.cashflow.dto;

import com.college.cashflow.enums.CategoryType;
import com.college.cashflow.enums.PaymentMethod;
import com.college.cashflow.enums.TransactionType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionResponse {

    private Long id;
    private TransactionType transactionType;
    private BigDecimal amount;
    private PaymentMethod paymentMethod;
    private Long categoryId;
    private String categoryName;
    private CategoryType categoryType;
    private Long departmentId;
    private String departmentName;
    private String departmentCode;
    private String description;
    private String referenceNumber;
    private LocalDate transactionDate;
    private Long createdById;
    private String createdByUsername;
    private String createdByFullName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
