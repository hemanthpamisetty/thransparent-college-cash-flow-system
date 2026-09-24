package com.college.cashflow.service;

import com.college.cashflow.dto.CashflowSummaryResponse;
import com.college.cashflow.dto.TransactionRequest;
import com.college.cashflow.dto.TransactionResponse;
import com.college.cashflow.entity.Category;
import com.college.cashflow.entity.Department;
import com.college.cashflow.entity.Transaction;
import com.college.cashflow.entity.User;
import com.college.cashflow.enums.CategoryType;
import com.college.cashflow.enums.PaymentMethod;
import com.college.cashflow.enums.Role;
import com.college.cashflow.enums.TransactionType;
import com.college.cashflow.exception.ResourceNotFoundException;
import com.college.cashflow.repository.CategoryRepository;
import com.college.cashflow.repository.DepartmentRepository;
import com.college.cashflow.repository.TransactionRepository;
import com.college.cashflow.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;

    public TransactionService(TransactionRepository transactionRepository,
                              CategoryRepository categoryRepository,
                              DepartmentRepository departmentRepository,
                              UserRepository userRepository) {
        this.transactionRepository = transactionRepository;
        this.categoryRepository = categoryRepository;
        this.departmentRepository = departmentRepository;
        this.userRepository = userRepository;
    }

    /**
     * Record a new Money IN or Money OUT transaction.
     */
    @Transactional
    public TransactionResponse createTransaction(TransactionRequest request, String username) {
        User currentUser = getUserByUsername(username);

        // 1. Determine & validate Department
        Department department;
        if (currentUser.getRole() == Role.DEPT_ADMIN) {
            department = currentUser.getDepartment();
            if (department == null || !Boolean.TRUE.equals(department.getActive())) {
                throw new IllegalStateException("Department Admin is not assigned to an active department");
            }
        } else {
            // MAIN_ADMIN must specify the department
            if (request.getDepartmentId() == null) {
                throw new IllegalArgumentException("Department ID is required for recording transactions");
            }
            department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + request.getDepartmentId()));

            if (!Boolean.TRUE.equals(department.getActive())) {
                throw new IllegalArgumentException("Cannot record transaction for an inactive department");
            }
        }

        // 2. Validate Category
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));

        if (!Boolean.TRUE.equals(category.getActive())) {
            throw new IllegalArgumentException("Cannot record transaction with an inactive category");
        }

        // 3. Validate category type matches transaction type
        CategoryType expectedCategoryType = request.getTransactionType() == TransactionType.IN ? CategoryType.IN : CategoryType.OUT;
        if (category.getType() != expectedCategoryType) {
            throw new IllegalArgumentException(
                    String.format("Category type mismatch: %s category '%s' cannot be used for a Money %s transaction",
                            category.getType(), category.getName(), request.getTransactionType())
            );
        }

        // 4. Build and save transaction
        Transaction transaction = Transaction.builder()
                .transactionType(request.getTransactionType())
                .amount(request.getAmount())
                .paymentMethod(request.getPaymentMethod())
                .category(category)
                .department(department)
                .description(StringUtils.hasText(request.getDescription()) ? request.getDescription().trim() : null)
                .referenceNumber(StringUtils.hasText(request.getReferenceNumber()) ? request.getReferenceNumber().trim() : null)
                .transactionDate(request.getTransactionDate())
                .createdBy(currentUser)
                .build();

        Transaction saved = transactionRepository.save(transaction);
        return mapToResponse(saved);
    }

    /**
     * List and filter transactions with role-based access control.
     */
    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactions(Long departmentId,
                                                    TransactionType transactionType,
                                                    Long categoryId,
                                                    PaymentMethod paymentMethod,
                                                    LocalDate startDate,
                                                    LocalDate endDate,
                                                    String search,
                                                    String username) {
        User currentUser = getUserByUsername(username);

        // Role-based department scoping
        Long targetDeptId = departmentId;
        if (currentUser.getRole() == Role.DEPT_ADMIN) {
            if (currentUser.getDepartment() == null) {
                throw new AccessDeniedException("User does not have an assigned department");
            }
            // Enforce Dept Admin's department strictly
            targetDeptId = currentUser.getDepartment().getId();
        }

        String searchKeyword = StringUtils.hasText(search) ? search.trim() : null;

        List<Transaction> transactions = transactionRepository.searchTransactions(
                targetDeptId,
                transactionType,
                categoryId,
                paymentMethod,
                startDate,
                endDate,
                searchKeyword
        );

        return transactions.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get transaction by ID with permission checks.
     */
    @Transactional(readOnly = true)
    public TransactionResponse getTransactionById(Long id, String username) {
        User currentUser = getUserByUsername(username);
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + id));

        // Dept Admin cannot view other departments' transactions
        if (currentUser.getRole() == Role.DEPT_ADMIN) {
            if (currentUser.getDepartment() == null ||
                    !transaction.getDepartment().getId().equals(currentUser.getDepartment().getId())) {
                throw new AccessDeniedException("Access denied: You cannot view transactions of other departments");
            }
        }

        return mapToResponse(transaction);
    }

    /**
     * Update an existing transaction.
     */
    @Transactional
    public TransactionResponse updateTransaction(Long id, TransactionRequest request, String username) {
        User currentUser = getUserByUsername(username);
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + id));

        // Dept Admin access check
        if (currentUser.getRole() == Role.DEPT_ADMIN) {
            if (currentUser.getDepartment() == null ||
                    !transaction.getDepartment().getId().equals(currentUser.getDepartment().getId())) {
                throw new AccessDeniedException("Access denied: You cannot update transactions of other departments");
            }
        } else if (currentUser.getRole() == Role.MAIN_ADMIN) {
            if (request.getDepartmentId() != null && !request.getDepartmentId().equals(transaction.getDepartment().getId())) {
                Department newDept = departmentRepository.findById(request.getDepartmentId())
                        .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + request.getDepartmentId()));
                if (!Boolean.TRUE.equals(newDept.getActive())) {
                    throw new IllegalArgumentException("Cannot assign transaction to an inactive department");
                }
                transaction.setDepartment(newDept);
            }
        }

        // Validate Category
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));

        if (!Boolean.TRUE.equals(category.getActive())) {
            throw new IllegalArgumentException("Cannot use an inactive category");
        }

        CategoryType expectedCategoryType = request.getTransactionType() == TransactionType.IN ? CategoryType.IN : CategoryType.OUT;
        if (category.getType() != expectedCategoryType) {
            throw new IllegalArgumentException(
                    String.format("Category type mismatch: %s category '%s' cannot be used for a Money %s transaction",
                            category.getType(), category.getName(), request.getTransactionType())
            );
        }

        transaction.setTransactionType(request.getTransactionType());
        transaction.setAmount(request.getAmount());
        transaction.setPaymentMethod(request.getPaymentMethod());
        transaction.setCategory(category);
        transaction.setDescription(StringUtils.hasText(request.getDescription()) ? request.getDescription().trim() : null);
        transaction.setReferenceNumber(StringUtils.hasText(request.getReferenceNumber()) ? request.getReferenceNumber().trim() : null);
        transaction.setTransactionDate(request.getTransactionDate());

        Transaction updated = transactionRepository.save(transaction);
        return mapToResponse(updated);
    }

    /**
     * Delete transaction.
     */
    @Transactional
    public void deleteTransaction(Long id, String username) {
        User currentUser = getUserByUsername(username);
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + id));

        if (currentUser.getRole() == Role.DEPT_ADMIN) {
            if (currentUser.getDepartment() == null ||
                    !transaction.getDepartment().getId().equals(currentUser.getDepartment().getId())) {
                throw new AccessDeniedException("Access denied: You cannot delete transactions of other departments");
            }
        }

        transactionRepository.delete(transaction);
    }

    /**
     * Get aggregate cashflow metrics (Total IN, Total OUT, Net Balance, Counts).
     */
    @Transactional(readOnly = true)
    public CashflowSummaryResponse getCashflowSummary(Long departmentId, String username) {
        User currentUser = getUserByUsername(username);

        Long targetDeptId = departmentId;
        if (currentUser.getRole() == Role.DEPT_ADMIN) {
            if (currentUser.getDepartment() == null) {
                throw new AccessDeniedException("User does not have an assigned department");
            }
            targetDeptId = currentUser.getDepartment().getId();
        }

        BigDecimal totalIncome = transactionRepository.sumAmountByTypeAndDepartment(TransactionType.IN, targetDeptId);
        BigDecimal totalExpense = transactionRepository.sumAmountByTypeAndDepartment(TransactionType.OUT, targetDeptId);
        if (totalIncome == null) totalIncome = BigDecimal.ZERO;
        if (totalExpense == null) totalExpense = BigDecimal.ZERO;

        BigDecimal netBalance = totalIncome.subtract(totalExpense);
        long totalCount = transactionRepository.countTotalByDepartment(targetDeptId);
        long incomeCount = transactionRepository.countByTypeAndDepartment(TransactionType.IN, targetDeptId);
        long expenseCount = transactionRepository.countByTypeAndDepartment(TransactionType.OUT, targetDeptId);

        return CashflowSummaryResponse.builder()
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .netBalance(netBalance)
                .totalTransactions(totalCount)
                .incomeCount(incomeCount)
                .expenseCount(expenseCount)
                .build();
    }

    private User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
    }

    private TransactionResponse mapToResponse(Transaction t) {
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
}
