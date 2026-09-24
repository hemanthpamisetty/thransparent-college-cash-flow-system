package com.college.cashflow.dto;

import com.college.cashflow.enums.CategoryType;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryBreakdownDTO {

    private Long categoryId;
    private String categoryName;
    private CategoryType categoryType;
    private BigDecimal totalAmount;
    private Long transactionCount;
}
