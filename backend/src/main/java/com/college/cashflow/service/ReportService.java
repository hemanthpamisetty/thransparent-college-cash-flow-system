package com.college.cashflow.service;

import com.college.cashflow.dto.*;
import com.college.cashflow.entity.Transaction;
import com.college.cashflow.entity.User;
import com.college.cashflow.enums.CategoryType;
import com.college.cashflow.enums.Role;
import com.college.cashflow.enums.TransactionType;
import com.college.cashflow.exception.ResourceNotFoundException;
import com.college.cashflow.repository.TransactionRepository;
import com.college.cashflow.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    public ReportService(TransactionRepository transactionRepository,
                         UserRepository userRepository) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
    }

    /**
     * Get daily cashflow trend data for a date range.
     * Returns income, expense, and net flow for each day that has transactions.
     */
    @Transactional(readOnly = true)
    public List<DailyCashflowDTO> getCashflowTrend(LocalDate startDate, LocalDate endDate,
                                                    Long departmentId, String username) {
        Long targetDeptId = resolveTargetDepartmentId(departmentId, username);

        List<Object[]> rawData = transactionRepository.findDailyCashflowRaw(startDate, endDate, targetDeptId);

        // Group by date: each row is [date, transactionType, sumAmount, count]
        Map<LocalDate, DailyCashflowDTO> dailyMap = new LinkedHashMap<>();

        for (Object[] row : rawData) {
            LocalDate date = convertToLocalDate(row[0]);
            String type = (String) row[1];
            BigDecimal amount = convertToBigDecimal(row[2]);

            DailyCashflowDTO dto = dailyMap.computeIfAbsent(date, d ->
                    DailyCashflowDTO.builder()
                            .date(d)
                            .totalIn(BigDecimal.ZERO)
                            .totalOut(BigDecimal.ZERO)
                            .netFlow(BigDecimal.ZERO)
                            .build()
            );

            if ("IN".equals(type)) {
                dto.setTotalIn(amount);
            } else if ("OUT".equals(type)) {
                dto.setTotalOut(amount);
            }
        }

        // Calculate net flow for each day
        dailyMap.values().forEach(dto ->
                dto.setNetFlow(dto.getTotalIn().subtract(dto.getTotalOut()))
        );

        return new ArrayList<>(dailyMap.values());
    }

    /**
     * Get income/expense totals grouped by category.
     */
    @Transactional(readOnly = true)
    public List<CategoryBreakdownDTO> getCategoryBreakdown(LocalDate startDate, LocalDate endDate,
                                                            Long departmentId, String transactionType,
                                                            String username) {
        Long targetDeptId = resolveTargetDepartmentId(departmentId, username);

        List<Object[]> rawData = transactionRepository.findCategoryBreakdownRaw(
                startDate, endDate, targetDeptId, transactionType);

        List<CategoryBreakdownDTO> results = new ArrayList<>();
        for (Object[] row : rawData) {
            results.add(CategoryBreakdownDTO.builder()
                    .categoryId(convertToLong(row[0]))
                    .categoryName((String) row[1])
                    .categoryType(CategoryType.valueOf((String) row[2]))
                    .totalAmount(convertToBigDecimal(row[3]))
                    .transactionCount(convertToLong(row[4]))
                    .build());
        }

        return results;
    }

    /**
     * Get per-department income/expense summary (Main Admin only).
     */
    @Transactional(readOnly = true)
    public List<DepartmentSummaryDTO> getDepartmentSummary(LocalDate startDate, LocalDate endDate,
                                                            String username) {
        User currentUser = getUserByUsername(username);
        if (currentUser.getRole() != Role.MAIN_ADMIN) {
            throw new AccessDeniedException("Only Main Admin can view department-wise summary");
        }

        List<Object[]> rawData = transactionRepository.findDepartmentSummaryRaw(startDate, endDate);

        // Group by department: each row is [deptId, deptName, deptCode, transactionType, sumAmount, count]
        Map<Long, DepartmentSummaryDTO> deptMap = new LinkedHashMap<>();

        for (Object[] row : rawData) {
            Long deptId = convertToLong(row[0]);
            String deptName = (String) row[1];
            String deptCode = (String) row[2];
            String type = (String) row[3];
            BigDecimal amount = convertToBigDecimal(row[4]);
            Long count = convertToLong(row[5]);

            DepartmentSummaryDTO dto = deptMap.computeIfAbsent(deptId, id ->
                    DepartmentSummaryDTO.builder()
                            .departmentId(id)
                            .departmentName(deptName)
                            .departmentCode(deptCode)
                            .totalIncome(BigDecimal.ZERO)
                            .totalExpense(BigDecimal.ZERO)
                            .netBalance(BigDecimal.ZERO)
                            .transactionCount(0L)
                            .build()
            );

            if ("IN".equals(type)) {
                dto.setTotalIncome(amount);
            } else if ("OUT".equals(type)) {
                dto.setTotalExpense(amount);
            }
            dto.setTransactionCount(dto.getTransactionCount() + count);
        }

        // Calculate net balance
        deptMap.values().forEach(dto ->
                dto.setNetBalance(dto.getTotalIncome().subtract(dto.getTotalExpense()))
        );

        return new ArrayList<>(deptMap.values());
    }

    /**
     * Get detailed daily summary — all transactions for a specific date.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getDailySummary(LocalDate date, Long departmentId, String username) {
        Long targetDeptId = resolveTargetDepartmentId(departmentId, username);

        List<Transaction> transactions = transactionRepository.findByDateAndOptionalDepartment(date, targetDeptId);

        BigDecimal totalIn = BigDecimal.ZERO;
        BigDecimal totalOut = BigDecimal.ZERO;
        long inCount = 0;
        long outCount = 0;

        List<TransactionResponse> transactionList = new ArrayList<>();
        for (Transaction t : transactions) {
            transactionList.add(mapToTransactionResponse(t));
            if (t.getTransactionType() == TransactionType.IN) {
                totalIn = totalIn.add(t.getAmount());
                inCount++;
            } else {
                totalOut = totalOut.add(t.getAmount());
                outCount++;
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("date", date);
        result.put("totalIncome", totalIn);
        result.put("totalExpense", totalOut);
        result.put("netBalance", totalIn.subtract(totalOut));
        result.put("incomeCount", inCount);
        result.put("expenseCount", outCount);
        result.put("totalTransactions", transactions.size());
        result.put("transactions", transactionList);
        return result;
    }

    /**
     * Get monthly summary — aggregated totals per month for a given year.
     */
    @Transactional(readOnly = true)
    public List<MonthlySummaryDTO> getMonthlySummary(int year, Long departmentId, String username) {
        Long targetDeptId = resolveTargetDepartmentId(departmentId, username);

        List<Object[]> rawData = transactionRepository.findMonthlySummaryRaw(year, targetDeptId);

        // Group by month: each row is [year, month, transactionType, sumAmount, count]
        Map<Integer, MonthlySummaryDTO> monthMap = new LinkedHashMap<>();

        for (Object[] row : rawData) {
            Integer yr = convertToInt(row[0]);
            Integer month = convertToInt(row[1]);
            String type = (String) row[2];
            BigDecimal amount = convertToBigDecimal(row[3]);
            Long count = convertToLong(row[4]);

            MonthlySummaryDTO dto = monthMap.computeIfAbsent(month, m ->
                    MonthlySummaryDTO.builder()
                            .year(yr)
                            .month(m)
                            .monthName(Month.of(m).getDisplayName(TextStyle.FULL, Locale.ENGLISH))
                            .totalIncome(BigDecimal.ZERO)
                            .totalExpense(BigDecimal.ZERO)
                            .netBalance(BigDecimal.ZERO)
                            .transactionCount(0L)
                            .build()
            );

            if ("IN".equals(type)) {
                dto.setTotalIncome(amount);
            } else if ("OUT".equals(type)) {
                dto.setTotalExpense(amount);
            }
            dto.setTransactionCount(dto.getTransactionCount() + count);
        }

        // Calculate net balance
        monthMap.values().forEach(dto ->
                dto.setNetBalance(dto.getTotalIncome().subtract(dto.getTotalExpense()))
        );

        return new ArrayList<>(monthMap.values());
    }

    // --- Helpers ---

    private Long resolveTargetDepartmentId(Long departmentId, String username) {
        User currentUser = getUserByUsername(username);

        if (currentUser.getRole() == Role.DEPT_ADMIN) {
            if (currentUser.getDepartment() == null) {
                throw new AccessDeniedException("User does not have an assigned department");
            }
            return currentUser.getDepartment().getId();
        }

        // MAIN_ADMIN can specify any department or null for all
        return departmentId;
    }

    private User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
    }

    private TransactionResponse mapToTransactionResponse(Transaction t) {
        return TransactionResponse.builder()
                .id(t.getId())
                .transactionType(t.getTransactionType())
                .amount(t.getAmount())
                .paymentMethod(t.getPaymentMethod())
                .categoryId(t.getCategory() != null ? t.getCategory().getId() : null)
                .categoryName(t.getCategory() != null ? t.getCategory().getName() : null)
                .categoryType(t.getCategory() != null ? t.getCategory().getType() : null)
                .departmentId(t.getDepartment() != null ? t.getDepartment().getId() : null)
                .departmentName(t.getDepartment() != null ? t.getDepartment().getName() : null)
                .departmentCode(t.getDepartment() != null ? t.getDepartment().getCode() : null)
                .description(t.getDescription())
                .referenceNumber(t.getReferenceNumber())
                .transactionDate(t.getTransactionDate())
                .createdById(t.getCreatedBy() != null ? t.getCreatedBy().getId() : null)
                .createdByUsername(t.getCreatedBy() != null ? t.getCreatedBy().getUsername() : null)
                .createdByFullName(t.getCreatedBy() != null ? t.getCreatedBy().getFullName() : null)
                .createdAt(t.getCreatedAt())
                .updatedAt(t.getUpdatedAt())
                .build();
    }

    private LocalDate convertToLocalDate(Object obj) {
        if (obj instanceof java.sql.Date) {
            return ((java.sql.Date) obj).toLocalDate();
        }
        if (obj instanceof LocalDate) {
            return (LocalDate) obj;
        }
        return LocalDate.parse(obj.toString());
    }

    private BigDecimal convertToBigDecimal(Object obj) {
        if (obj == null) return BigDecimal.ZERO;
        if (obj instanceof BigDecimal) return (BigDecimal) obj;
        return new BigDecimal(obj.toString());
    }

    private Long convertToLong(Object obj) {
        if (obj == null) return 0L;
        if (obj instanceof Long) return (Long) obj;
        return ((Number) obj).longValue();
    }

    private Integer convertToInt(Object obj) {
        if (obj == null) return 0;
        if (obj instanceof Integer) return (Integer) obj;
        return ((Number) obj).intValue();
    }
}
