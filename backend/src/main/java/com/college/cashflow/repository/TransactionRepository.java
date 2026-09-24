package com.college.cashflow.repository;

import com.college.cashflow.entity.Transaction;
import com.college.cashflow.enums.PaymentMethod;
import com.college.cashflow.enums.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByDepartmentIdOrderByTransactionDateDescIdDesc(Long departmentId);

    List<Transaction> findAllByOrderByTransactionDateDescIdDesc();

    @Query("SELECT t FROM Transaction t WHERE " +
            "(:departmentId IS NULL OR t.department.id = :departmentId) AND " +
            "(:transactionType IS NULL OR t.transactionType = :transactionType) AND " +
            "(:categoryId IS NULL OR t.category.id = :categoryId) AND " +
            "(:paymentMethod IS NULL OR t.paymentMethod = :paymentMethod) AND " +
            "(:startDate IS NULL OR t.transactionDate >= :startDate) AND " +
            "(:endDate IS NULL OR t.transactionDate <= :endDate) AND " +
            "(:search IS NULL OR LOWER(t.description) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(t.referenceNumber) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "ORDER BY t.transactionDate DESC, t.id DESC")
    List<Transaction> searchTransactions(
            @Param("departmentId") Long departmentId,
            @Param("transactionType") TransactionType transactionType,
            @Param("categoryId") Long categoryId,
            @Param("paymentMethod") PaymentMethod paymentMethod,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("search") String search
    );

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.transactionType = :type AND (:departmentId IS NULL OR t.department.id = :departmentId)")
    BigDecimal sumAmountByTypeAndDepartment(@Param("type") TransactionType type, @Param("departmentId") Long departmentId);

    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.transactionType = :type AND (:departmentId IS NULL OR t.department.id = :departmentId)")
    long countByTypeAndDepartment(@Param("type") TransactionType type, @Param("departmentId") Long departmentId);

    @Query("SELECT COUNT(t) FROM Transaction t WHERE (:departmentId IS NULL OR t.department.id = :departmentId)")
    long countTotalByDepartment(@Param("departmentId") Long departmentId);

    // ---- Week 4: Report aggregate queries ----

    /**
     * Daily cashflow trend — returns [date, transactionType, sumAmount, count] per day.
     */
    @Query(value = "SELECT t.transaction_date, t.transaction_type, " +
            "COALESCE(SUM(t.amount), 0), COUNT(t.id) " +
            "FROM transactions t " +
            "WHERE t.transaction_date BETWEEN :startDate AND :endDate " +
            "AND (:departmentId IS NULL OR t.department_id = :departmentId) " +
            "GROUP BY t.transaction_date, t.transaction_type " +
            "ORDER BY t.transaction_date ASC", nativeQuery = true)
    List<Object[]> findDailyCashflowRaw(@Param("startDate") LocalDate startDate,
                                        @Param("endDate") LocalDate endDate,
                                        @Param("departmentId") Long departmentId);

    /**
     * Category breakdown — returns [categoryId, categoryName, categoryType, sumAmount, count].
     */
    @Query(value = "SELECT c.id, c.name, c.type, " +
            "COALESCE(SUM(t.amount), 0), COUNT(t.id) " +
            "FROM transactions t " +
            "JOIN categories c ON t.category_id = c.id " +
            "WHERE t.transaction_date BETWEEN :startDate AND :endDate " +
            "AND (:departmentId IS NULL OR t.department_id = :departmentId) " +
            "AND (:transactionType IS NULL OR t.transaction_type = :transactionType) " +
            "GROUP BY c.id, c.name, c.type " +
            "ORDER BY SUM(t.amount) DESC", nativeQuery = true)
    List<Object[]> findCategoryBreakdownRaw(@Param("startDate") LocalDate startDate,
                                            @Param("endDate") LocalDate endDate,
                                            @Param("departmentId") Long departmentId,
                                            @Param("transactionType") String transactionType);

    /**
     * Department summary — returns [departmentId, departmentName, departmentCode, transactionType, sumAmount, count].
     */
    @Query(value = "SELECT d.id, d.name, d.code, t.transaction_type, " +
            "COALESCE(SUM(t.amount), 0), COUNT(t.id) " +
            "FROM transactions t " +
            "JOIN departments d ON t.department_id = d.id " +
            "WHERE t.transaction_date BETWEEN :startDate AND :endDate " +
            "GROUP BY d.id, d.name, d.code, t.transaction_type " +
            "ORDER BY d.name ASC", nativeQuery = true)
    List<Object[]> findDepartmentSummaryRaw(@Param("startDate") LocalDate startDate,
                                            @Param("endDate") LocalDate endDate);

    /**
     * Transactions for a specific date, optionally filtered by department.
     */
    @Query("SELECT t FROM Transaction t WHERE t.transactionDate = :date " +
            "AND (:departmentId IS NULL OR t.department.id = :departmentId) " +
            "ORDER BY t.id DESC")
    List<Transaction> findByDateAndOptionalDepartment(@Param("date") LocalDate date,
                                                      @Param("departmentId") Long departmentId);

    /**
     * Monthly summary — returns [year, month, transactionType, sumAmount, count].
     */
    @Query(value = "SELECT YEAR(t.transaction_date), MONTH(t.transaction_date), " +
            "t.transaction_type, COALESCE(SUM(t.amount), 0), COUNT(t.id) " +
            "FROM transactions t " +
            "WHERE YEAR(t.transaction_date) = :year " +
            "AND (:departmentId IS NULL OR t.department_id = :departmentId) " +
            "GROUP BY YEAR(t.transaction_date), MONTH(t.transaction_date), t.transaction_type " +
            "ORDER BY MONTH(t.transaction_date) ASC", nativeQuery = true)
    List<Object[]> findMonthlySummaryRaw(@Param("year") int year,
                                         @Param("departmentId") Long departmentId);
}
