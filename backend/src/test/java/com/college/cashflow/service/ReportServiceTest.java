package com.college.cashflow.service;

import com.college.cashflow.dto.DailyCashflowDTO;
import com.college.cashflow.dto.DepartmentSummaryDTO;
import com.college.cashflow.dto.MonthlySummaryDTO;
import com.college.cashflow.entity.Department;
import com.college.cashflow.entity.Transaction;
import com.college.cashflow.entity.User;
import com.college.cashflow.enums.PaymentMethod;
import com.college.cashflow.enums.Role;
import com.college.cashflow.enums.TransactionType;
import com.college.cashflow.repository.TransactionRepository;
import com.college.cashflow.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ReportService — stub-based, no Mockito (avoids byte-buddy agent issues on JDK 21).
 */
class ReportServiceTest {

    private User mainAdmin;
    private User deptAdmin;
    private Department cseDept;

    @BeforeEach
    void setUp() {
        mainAdmin = User.builder()
                .id(1L)
                .username("admin")
                .role(Role.MAIN_ADMIN)
                .active(true)
                .build();

        cseDept = Department.builder()
                .id(10L)
                .name("Computer Science")
                .code("CSE")
                .active(true)
                .build();

        deptAdmin = User.builder()
                .id(2L)
                .username("cseadmin")
                .role(Role.DEPT_ADMIN)
                .department(cseDept)
                .active(true)
                .build();
    }

    private UserRepository userRepo(Map<String, User> byUsername) {
        return new UserRepository() {
            @Override public Optional<User> findByUsername(String u) { return Optional.ofNullable(byUsername.get(u)); }
            @Override public Optional<User> findByUsernameAndRole(String u, Role r) { return Optional.empty(); }
            @Override public List<User> findByRole(Role r) { return Collections.emptyList(); }
            @Override public boolean existsByUsername(String u) { return false; }
            @Override public boolean existsByUsernameAndIdNot(String u, Long id) { return false; }
            @Override public boolean existsByEmail(String e) { return false; }
            @Override public boolean existsByEmailAndIdNot(String e, Long id) { return false; }
            @Override public <S extends User> S save(S e) { return e; }
            @Override public Optional<User> findById(Long id) { return Optional.empty(); }
            @Override public List<User> findAll() { return Collections.emptyList(); }
            @Override public List<User> findAll(Sort s) { return Collections.emptyList(); }
            @Override public Page<User> findAll(Pageable p) { return Page.empty(); }
            @Override public List<User> findAllById(Iterable<Long> ids) { return Collections.emptyList(); }
            @Override public <S extends User> List<S> saveAll(Iterable<S> es) { return Collections.emptyList(); }
            @Override public boolean existsById(Long id) { return false; }
            @Override public long count() { return 0; }
            @Override public void deleteById(Long id) {}
            @Override public void delete(User e) {}
            @Override public void deleteAllById(Iterable<? extends Long> ids) {}
            @Override public void deleteAll(Iterable<? extends User> es) {}
            @Override public void deleteAll() {}
            @Override public <S extends User> Optional<S> findOne(Example<S> ex) { return Optional.empty(); }
            @Override public <S extends User> List<S> findAll(Example<S> ex) { return Collections.emptyList(); }
            @Override public <S extends User> List<S> findAll(Example<S> ex, Sort s) { return Collections.emptyList(); }
            @Override public <S extends User> Page<S> findAll(Example<S> ex, Pageable p) { return Page.empty(); }
            @Override public <S extends User> long count(Example<S> ex) { return 0; }
            @Override public <S extends User> boolean exists(Example<S> ex) { return false; }
            @Override public <S extends User, R> R findBy(Example<S> ex, Function<FluentQuery.FetchableFluentQuery<S>, R> q) { return null; }
            @Override public void flush() {}
            @Override public <S extends User> S saveAndFlush(S e) { return e; }
            @Override public <S extends User> List<S> saveAllAndFlush(Iterable<S> es) { return Collections.emptyList(); }
            @Override public void deleteAllInBatch(Iterable<User> es) {}
            @Override public void deleteAllByIdInBatch(Iterable<Long> ids) {}
            @Override public void deleteAllInBatch() {}
            @Override public User getOne(Long id) { return null; }
            @Override public User getById(Long id) { return null; }
            @Override public User getReferenceById(Long id) { return null; }
        };
    }

    private TransactionRepository txRepo(
            List<Object[]> dailyTrend,
            List<Object[]> deptSummary,
            List<Object[]> monthlySummary
    ) {
        return new TransactionRepository() {
            @Override public List<Object[]> findDailyCashflowRaw(LocalDate sd, LocalDate ed, Long dId) {
                return dailyTrend != null ? dailyTrend : Collections.emptyList();
            }
            @Override public List<Object[]> findDepartmentSummaryRaw(LocalDate sd, LocalDate ed) {
                return deptSummary != null ? deptSummary : Collections.emptyList();
            }
            @Override public List<Object[]> findMonthlySummaryRaw(int year, Long dId) {
                return monthlySummary != null ? monthlySummary : Collections.emptyList();
            }
            @Override public List<Object[]> findCategoryBreakdownRaw(LocalDate sd, LocalDate ed, Long dId, String tt) {
                return Collections.emptyList();
            }
            @Override public List<Transaction> findByDateAndOptionalDepartment(LocalDate date, Long dId) {
                return Collections.emptyList();
            }
            @Override public List<Transaction> findByDepartmentIdOrderByTransactionDateDescIdDesc(Long dId) {
                return Collections.emptyList();
            }
            @Override public List<Transaction> findAllByOrderByTransactionDateDescIdDesc() {
                return Collections.emptyList();
            }
            @Override public List<Transaction> searchTransactions(Long dId, TransactionType tt, Long cId, PaymentMethod pm, LocalDate sd, LocalDate ed, String s) {
                return Collections.emptyList();
            }
            @Override public BigDecimal sumAmountByTypeAndDepartment(TransactionType type, Long dId) {
                return BigDecimal.ZERO;
            }
            @Override public long countByTypeAndDepartment(TransactionType type, Long dId) {
                return 0;
            }
            @Override public long countTotalByDepartment(Long dId) {
                return 0;
            }
            @Override public <S extends Transaction> S save(S e) { return e; }
            @Override public Optional<Transaction> findById(Long id) { return Optional.empty(); }
            @Override public List<Transaction> findAll() { return Collections.emptyList(); }
            @Override public List<Transaction> findAll(Sort s) { return Collections.emptyList(); }
            @Override public Page<Transaction> findAll(Pageable p) { return Page.empty(); }
            @Override public List<Transaction> findAllById(Iterable<Long> ids) { return Collections.emptyList(); }
            @Override public <S extends Transaction> List<S> saveAll(Iterable<S> es) { return Collections.emptyList(); }
            @Override public boolean existsById(Long id) { return false; }
            @Override public long count() { return 0; }
            @Override public void deleteById(Long id) {}
            @Override public void delete(Transaction e) {}
            @Override public void deleteAllById(Iterable<? extends Long> ids) {}
            @Override public void deleteAll(Iterable<? extends Transaction> es) {}
            @Override public void deleteAll() {}
            @Override public <S extends Transaction> Optional<S> findOne(Example<S> ex) { return Optional.empty(); }
            @Override public <S extends Transaction> List<S> findAll(Example<S> ex) { return Collections.emptyList(); }
            @Override public <S extends Transaction> List<S> findAll(Example<S> ex, Sort s) { return Collections.emptyList(); }
            @Override public <S extends Transaction> Page<S> findAll(Example<S> ex, Pageable p) { return Page.empty(); }
            @Override public <S extends Transaction> long count(Example<S> ex) { return 0; }
            @Override public <S extends Transaction> boolean exists(Example<S> ex) { return false; }
            @Override public <S extends Transaction, R> R findBy(Example<S> ex, Function<FluentQuery.FetchableFluentQuery<S>, R> q) { return null; }
            @Override public void flush() {}
            @Override public <S extends Transaction> S saveAndFlush(S e) { return e; }
            @Override public <S extends Transaction> List<S> saveAllAndFlush(Iterable<S> es) { return Collections.emptyList(); }
            @Override public void deleteAllInBatch(Iterable<Transaction> es) {}
            @Override public void deleteAllByIdInBatch(Iterable<Long> ids) {}
            @Override public void deleteAllInBatch() {}
            @Override public Transaction getOne(Long id) { return null; }
            @Override public Transaction getById(Long id) { return null; }
            @Override public Transaction getReferenceById(Long id) { return null; }
        };
    }

    @Test
    void testGetCashflowTrendDateValidation() {
        ReportService svc = new ReportService(txRepo(null, null, null), userRepo(Map.of("admin", mainAdmin)));
        LocalDate start = LocalDate.of(2026, 10, 20);
        LocalDate end = LocalDate.of(2026, 10, 10);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> svc.getCashflowTrend(start, end, null, "admin"));
        assertEquals("Start date cannot be after end date", ex.getMessage());
    }

    @Test
    void testGetCashflowTrendCalculatesNetFlowCorrectly() {
        LocalDate date = LocalDate.of(2026, 10, 5);
        Object[] rowIn = new Object[]{date, "IN", new BigDecimal("1000.00"), 2L};
        Object[] rowOut = new Object[]{date, "OUT", new BigDecimal("400.00"), 1L};

        ReportService svc = new ReportService(
                txRepo(List.of(rowIn, rowOut), null, null),
                userRepo(Map.of("admin", mainAdmin))
        );

        List<DailyCashflowDTO> trend = svc.getCashflowTrend(date, date, null, "admin");

        assertEquals(1, trend.size());
        DailyCashflowDTO dto = trend.get(0);
        assertEquals(date, dto.getDate());
        assertEquals(new BigDecimal("1000.00"), dto.getTotalIn());
        assertEquals(new BigDecimal("400.00"), dto.getTotalOut());
        assertEquals(new BigDecimal("600.00"), dto.getNetFlow());
    }

    @Test
    void testGetDepartmentSummaryMainAdminOnly() {
        LocalDate start = LocalDate.of(2026, 10, 1);
        LocalDate end = LocalDate.of(2026, 10, 10);

        ReportService svc = new ReportService(
                txRepo(null, null, null),
                userRepo(Map.of("cseadmin", deptAdmin))
        );

        assertThrows(AccessDeniedException.class,
                () -> svc.getDepartmentSummary(start, end, "cseadmin"));
    }

    @Test
    void testGetDepartmentSummaryCalculatesNetBalance() {
        LocalDate start = LocalDate.of(2026, 10, 1);
        LocalDate end = LocalDate.of(2026, 10, 10);

        Object[] rowIn = new Object[]{10L, "Computer Science", "CSE", "IN", new BigDecimal("5000.00"), 2L};
        Object[] rowOut = new Object[]{10L, "Computer Science", "CSE", "OUT", new BigDecimal("1500.00"), 1L};

        ReportService svc = new ReportService(
                txRepo(null, List.of(rowIn, rowOut), null),
                userRepo(Map.of("admin", mainAdmin))
        );

        List<DepartmentSummaryDTO> summaries = svc.getDepartmentSummary(start, end, "admin");

        assertEquals(1, summaries.size());
        DepartmentSummaryDTO dto = summaries.get(0);
        assertEquals(10L, dto.getDepartmentId());
        assertEquals("Computer Science", dto.getDepartmentName());
        assertEquals(new BigDecimal("5000.00"), dto.getTotalIncome());
        assertEquals(new BigDecimal("1500.00"), dto.getTotalExpense());
        assertEquals(new BigDecimal("3500.00"), dto.getNetBalance());
        assertEquals(3L, dto.getTransactionCount());
    }

    @Test
    void testGetMonthlySummaryCalculations() {
        Object[] rowIn = new Object[]{2026, 9, "IN", new BigDecimal("8000.00"), 4L};
        Object[] rowOut = new Object[]{2026, 9, "OUT", new BigDecimal("3000.00"), 2L};

        ReportService svc = new ReportService(
                txRepo(null, null, List.of(rowIn, rowOut)),
                userRepo(Map.of("admin", mainAdmin))
        );

        List<MonthlySummaryDTO> monthly = svc.getMonthlySummary(2026, null, "admin");

        assertEquals(1, monthly.size());
        MonthlySummaryDTO dto = monthly.get(0);
        assertEquals(2026, dto.getYear());
        assertEquals(9, dto.getMonth());
        assertEquals("September", dto.getMonthName());
        assertEquals(new BigDecimal("8000.00"), dto.getTotalIncome());
        assertEquals(new BigDecimal("3000.00"), dto.getTotalExpense());
        assertEquals(new BigDecimal("5000.00"), dto.getNetBalance());
        assertEquals(6L, dto.getTransactionCount());
    }

    @Test
    void deptAdmin_withoutAssignedDepartment_throwsAccessDenied() {
        User unassigned = User.builder().id(99L).username("unassigned").role(Role.DEPT_ADMIN).department(null).active(true).build();
        ReportService svc = new ReportService(txRepo(null, null, null), userRepo(Map.of("unassigned", unassigned)));
        LocalDate date = LocalDate.of(2026, 10, 5);

        assertThrows(AccessDeniedException.class,
                () -> svc.getCashflowTrend(date, date, null, "unassigned"));
    }

    @Test
    void deptAdmin_scopesCashflowTrendToOwnDepartment() {
        java.util.concurrent.atomic.AtomicReference<Long> capturedDeptId = new java.util.concurrent.atomic.AtomicReference<>();
        TransactionRepository customRepo = new TransactionRepository() {
            @Override public List<Object[]> findDailyCashflowRaw(LocalDate sd, LocalDate ed, Long dId) {
                capturedDeptId.set(dId);
                return Collections.emptyList();
            }
            @Override public List<Object[]> findDepartmentSummaryRaw(LocalDate sd, LocalDate ed) { return Collections.emptyList(); }
            @Override public List<Object[]> findMonthlySummaryRaw(int year, Long dId) { return Collections.emptyList(); }
            @Override public List<Object[]> findCategoryBreakdownRaw(LocalDate sd, LocalDate ed, Long dId, String tt) { return Collections.emptyList(); }
            @Override public List<Transaction> findByDateAndOptionalDepartment(LocalDate date, Long dId) { return Collections.emptyList(); }
            @Override public List<Transaction> findByDepartmentIdOrderByTransactionDateDescIdDesc(Long dId) { return Collections.emptyList(); }
            @Override public List<Transaction> findAllByOrderByTransactionDateDescIdDesc() { return Collections.emptyList(); }
            @Override public List<Transaction> searchTransactions(Long dId, TransactionType tt, Long cId, PaymentMethod pm, LocalDate sd, LocalDate ed, String s) { return Collections.emptyList(); }
            @Override public BigDecimal sumAmountByTypeAndDepartment(TransactionType type, Long dId) { return BigDecimal.ZERO; }
            @Override public long countByTypeAndDepartment(TransactionType type, Long dId) { return 0; }
            @Override public long countTotalByDepartment(Long dId) { return 0; }
            @Override public <S extends Transaction> S save(S e) { return e; }
            @Override public Optional<Transaction> findById(Long id) { return Optional.empty(); }
            @Override public List<Transaction> findAll() { return Collections.emptyList(); }
            @Override public List<Transaction> findAll(Sort s) { return Collections.emptyList(); }
            @Override public Page<Transaction> findAll(Pageable p) { return Page.empty(); }
            @Override public List<Transaction> findAllById(Iterable<Long> ids) { return Collections.emptyList(); }
            @Override public <S extends Transaction> List<S> saveAll(Iterable<S> es) { return Collections.emptyList(); }
            @Override public boolean existsById(Long id) { return false; }
            @Override public long count() { return 0; }
            @Override public void deleteById(Long id) {}
            @Override public void delete(Transaction e) {}
            @Override public void deleteAllById(Iterable<? extends Long> ids) {}
            @Override public void deleteAll(Iterable<? extends Transaction> es) {}
            @Override public void deleteAll() {}
            @Override public <S extends Transaction> Optional<S> findOne(Example<S> ex) { return Optional.empty(); }
            @Override public <S extends Transaction> List<S> findAll(Example<S> ex) { return Collections.emptyList(); }
            @Override public <S extends Transaction> List<S> findAll(Example<S> ex, Sort s) { return Collections.emptyList(); }
            @Override public <S extends Transaction> Page<S> findAll(Example<S> ex, Pageable p) { return Page.empty(); }
            @Override public <S extends Transaction> long count(Example<S> ex) { return 0; }
            @Override public <S extends Transaction> boolean exists(Example<S> ex) { return false; }
            @Override public <S extends Transaction, R> R findBy(Example<S> ex, Function<FluentQuery.FetchableFluentQuery<S>, R> q) { return null; }
            @Override public void flush() {}
            @Override public <S extends Transaction> S saveAndFlush(S e) { return e; }
            @Override public <S extends Transaction> List<S> saveAllAndFlush(Iterable<S> es) { return Collections.emptyList(); }
            @Override public void deleteAllInBatch(Iterable<Transaction> es) {}
            @Override public void deleteAllByIdInBatch(Iterable<Long> ids) {}
            @Override public void deleteAllInBatch() {}
            @Override public Transaction getOne(Long id) { return null; }
            @Override public Transaction getById(Long id) { return null; }
            @Override public Transaction getReferenceById(Long id) { return null; }
        };

        ReportService svc = new ReportService(customRepo, userRepo(Map.of("cseadmin", deptAdmin)));
        LocalDate date = LocalDate.of(2026, 10, 5);

        // DeptAdmin passes departmentId = 999L
        svc.getCashflowTrend(date, date, 999L, "cseadmin");

        // Must be scoped to their own dept1 (id = 10L)
        assertEquals(10L, capturedDeptId.get());
    }
}
