package com.college.cashflow.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyCashflowDTO {

    private LocalDate date;
    private BigDecimal totalIn;
    private BigDecimal totalOut;
    private BigDecimal netFlow;
}
