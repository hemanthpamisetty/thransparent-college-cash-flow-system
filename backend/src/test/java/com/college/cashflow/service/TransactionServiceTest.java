package com.college.cashflow.service;

import com.college.cashflow.dto.CashflowSummaryResponse;
import com.college.cashflow.dto.TransactionRequest;
import com.college.cashflow.dto.TransactionResponse;
import com.college.cashflow.entity.Category;
import com.college.cashflow.entity.Department;
import com.college.cashflow.entity.Transaction;
import com.college.cashflow.entity.User;
import com.college.cashflow.enums.*;
import com.college.cashflow.repository.CategoryRepository;
import com.college.cashflow.repository.DepartmentRepository;
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
 * Unit tests for TransactionService — stub-based, no Mockito.
 */
class TransactionServiceTest {

    private User mainAdmin;
    private User deptAdmin;
    private Department dept1;
    private Department dept2;
    private Category incomeCategory;
    private Category expenseCategory;

    @BeforeEach
    void setUp() {
        mainAdmin = User.builder().id(1L).username("admin").role(Role.MAIN_ADMIN).active(true).build();

        dept1 = Department.builder().id(10L).name("Computer Science").code("CSE").active(true).build();
        dept2 = Department.builder().id(20L).name("Electronics").code("ECE").active(true).build();

        deptAdmin = User.builder().id(2L).username("cseadmin").role(Role.DEPT_ADMIN)
                .department(dept1).active(true).build();

        incomeCategory = Category.builder().id(100L).name("Tuition Fees")
                .type(CategoryType.IN).active(true).build();
        expenseCategory = Category.builder().id(200L).name("Lab Equipment")
                .type(CategoryType.OUT).active(true).build();
    }

    // ── Stub factories ────────────────────────────────────────────────────────────

    private UserRepository userRepo(Map<String, User> byName) {
        return new UserRepository() {
            @Override public Optional<User> findByUsername(String u) { return Optional.ofNullable(byName.get(u)); }
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

    private CategoryRepository categoryRepo(Map<Long, Category> byId) {
        return new CategoryRepository() {
            @Override public Optional<Category> findById(Long id) { return Optional.ofNullable(byId.get(id)); }
            @Override public boolean existsByName(String n) { return false; }
            @Override public boolean existsByNameAndIdNot(String n, Long id) { return false; }
            @Override public Optional<Category> findByName(String n) { return Optional.empty(); }
            @Override public List<Category> findByType(CategoryType t) { return Collections.emptyList(); }
            @Override public <S extends Category> S save(S e) { return e; }
            @Override public List<Category> findAll() { return Collections.emptyList(); }
            @Override public List<Category> findAll(Sort s) { return Collections.emptyList(); }
            @Override public Page<Category> findAll(Pageable p) { return Page.empty(); }
            @Override public List<Category> findAllById(Iterable<Long> ids) { return Collections.emptyList(); }
            @Override public <S extends Category> List<S> saveAll(Iterable<S> es) { return Collections.emptyList(); }
            @Override public boolean existsById(Long id) { return byId.containsKey(id); }
            @Override public long count() { return 0; }
            @Override public void deleteById(Long id) {}
            @Override public void delete(Category e) {}
            @Override public void deleteAllById(Iterable<? extends Long> ids) {}
            @Override public void deleteAll(Iterable<? extends Category> es) {}
            @Override public void deleteAll() {}
            @Override public <S extends Category> Optional<S> findOne(Example<S> ex) { return Optional.empty(); }
            @Override public <S extends Category> List<S> findAll(Example<S> ex) { return Collections.emptyList(); }
            @Override public <S extends Category> List<S> findAll(Example<S> ex, Sort s) { return Collections.emptyList(); }
            @Override public <S extends Category> Page<S> findAll(Example<S> ex, Pageable p) { return Page.empty(); }
            @Override public <S extends Category> long count(Example<S> ex) { return 0; }
            @Override public <S extends Category> boolean exists(Example<S> ex) { return false; }
            @Override public <S extends Category, R> R findBy(Example<S> ex, Function<FluentQuery.FetchableFluentQuery<S>, R> q) { return null; }
            @Override public void flush() {}
            @Override public <S extends Category> S saveAndFlush(S e) { return e; }
            @Override public <S extends Category> List<S> saveAllAndFlush(Iterable<S> es) { return Collections.emptyList(); }
            @Override public void deleteAllInBatch(Iterable<Category> es) {}
            @Override public void deleteAllByIdInBatch(Iterable<Long> ids) {}
            @Override public void deleteAllInBatch() {}
            @Override public Category getOne(Long id) { return null; }
            @Override public Category getById(Long id) { return null; }
            @Override public Category getReferenceById(Long id) { return byId.get(id); }
        };
    }

    private DepartmentRepository deptRepo(Map<Long, Department> byId) {
        return new DepartmentRepository() {
            @Override public Optional<Department> findById(Long id) { return Optional.ofNullable(byId.get(id)); }
            @Override public boolean existsByName(String n) { return false; }
            @Override public boolean existsByCode(String c) { return false; }
            @Override public boolean existsByNameAndIdNot(String n, Long id) { return false; }
            @Override public boolean existsByCodeAndIdNot(String c, Long id) { return false; }
            @Override public Optional<Department> findByCode(String c) { return Optional.empty(); }
            @Override public Optional<Department> findByName(String n) { return Optional.empty(); }
            @Override public <S extends Department> S save(S e) { return e; }
            @Override public List<Department> findAll() { return Collections.emptyList(); }
            @Override public List<Department> findAll(Sort s) { return Collections.emptyList(); }
            @Override public Page<Department> findAll(Pageable p) { return Page.empty(); }
            @Override public List<Department> findAllById(Iterable<Long> ids) { return Collections.emptyList(); }
            @Override public <S extends Department> List<S> saveAll(Iterable<S> es) { return Collections.emptyList(); }
            @Override public boolean existsById(Long id) { return byId.containsKey(id); }
            @Override public long count() { return 0; }
            @Override public void deleteById(Long id) {}
            @Override public void delete(Department e) {}
            @Override public void deleteAllById(Iterable<? extends Long> ids) {}
            @Override public void deleteAll(Iterable<? extends Department> es) {}
            @Override public void deleteAll() {}
            @Override public <S extends Department> Optional<S> findOne(Example<S> ex) { return Optional.empty(); }
            @Override public <S extends Department> List<S> findAll(Example<S> ex) { return Collections.emptyList(); }
            @Override public <S extends Department> List<S> findAll(Example<S> ex, Sort s) { return Collections.emptyList(); }
            @Override public <S extends Department> Page<S> findAll(Example<S> ex, Pageable p) { return Page.empty(); }
            @Override public <S extends Department> long count(Example<S> ex) { return 0; }
            @Override public <S extends Department> boolean exists(Example<S> ex) { return false; }
            @Override public <S extends Department, R> R findBy(Example<S> ex, Function<FluentQuery.FetchableFluentQuery<S>, R> q) { return null; }
            @Override public void flush() {}
            @Override public <S extends Department> S saveAndFlush(S e) { return e; }
            @Override public <S extends Department> List<S> saveAllAndFlush(Iterable<S> es) { return Collections.emptyList(); }
            @Override public void deleteAllInBatch(Iterable<Department> es) {}
            @Override public void deleteAllByIdInBatch(Iterable<Long> ids) {}
            @Override public void deleteAllInBatch() {}
            @Override public Department getOne(Long id) { return null; }
            @Override public Department getById(Long id) { return null; }
            @Override public Department getReferenceById(Long id) { return byId.get(id); }
        };
    }

    /**
     * TransactionRepository stub with configurable saved return and findById.
     * Summary query methods are configured per-test via lambdas.
     */
    private TransactionRepository txRepo(Transaction saved, Map<Long, Transaction> byId,
                                         BigDecimal totalIn, BigDecimal totalOut,
                                         long totalCount, long inCount, long outCount) {
        return new TransactionRepository() {
            @Override public <S extends Transaction> S save(S e) {
                if (saved != null) { @SuppressWarnings("unchecked") S r = (S) saved; return r; }
                return e;
            }
            @Override public Optional<Transaction> findById(Long id) { return Optional.ofNullable(byId.get(id)); }
            @Override public BigDecimal sumAmountByTypeAndDepartment(TransactionType type, Long deptId) {
                return type == TransactionType.IN ? totalIn : totalOut;
            }
            @Override public long countByTypeAndDepartment(TransactionType type, Long deptId) {
                return type == TransactionType.IN ? inCount : outCount;
            }
            @Override public long countTotalByDepartment(Long deptId) { return totalCount; }
            @Override public List<Transaction> searchTransactions(Long dId, TransactionType tt, Long cId,
                    PaymentMethod pm, LocalDate sd, LocalDate ed, String s) { return Collections.emptyList(); }
            @Override public List<Transaction> findByDepartmentIdOrderByTransactionDateDescIdDesc(Long dId) { return Collections.emptyList(); }
            @Override public List<Transaction> findAllByOrderByTransactionDateDescIdDesc() { return Collections.emptyList(); }
            @Override public List<Object[]> findDailyCashflowRaw(LocalDate sd, LocalDate ed, Long dId) { return Collections.emptyList(); }
            @Override public List<Object[]> findCategoryBreakdownRaw(LocalDate sd, LocalDate ed, Long dId, String tt) { return Collections.emptyList(); }
            @Override public List<Object[]> findDepartmentSummaryRaw(LocalDate sd, LocalDate ed) { return Collections.emptyList(); }
            @Override public List<Transaction> findByDateAndOptionalDepartment(LocalDate date, Long dId) { return Collections.emptyList(); }
            @Override public List<Object[]> findMonthlySummaryRaw(int year, Long dId) { return Collections.emptyList(); }
            // ── JpaRepository boilerplate ──────────────────────────────────────
            @Override public List<Transaction> findAll() { return Collections.emptyList(); }
            @Override public List<Transaction> findAll(Sort s) { return Collections.emptyList(); }
            @Override public Page<Transaction> findAll(Pageable p) { return Page.empty(); }
            @Override public List<Transaction> findAllById(Iterable<Long> ids) { return Collections.emptyList(); }
            @Override public <S extends Transaction> List<S> saveAll(Iterable<S> es) { return Collections.emptyList(); }
            @Override public boolean existsById(Long id) { return byId.containsKey(id); }
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
            @Override public Transaction getReferenceById(Long id) { return byId.get(id); }
        };
    }

    private TransactionService buildSvc(UserRepository ur, CategoryRepository cr,
                                        DepartmentRepository dr, TransactionRepository tr) {
        return new TransactionService(tr, cr, dr, ur);
    }

    // ── Tests ─────────────────────────────────────────────────────────────────────

    @Test
    void mainAdmin_createTransaction_success() {
        Transaction saved = Transaction.builder().id(1L).transactionType(TransactionType.IN)
                .amount(new BigDecimal("5000.00")).paymentMethod(PaymentMethod.BANK_TRANSFER)
                .category(incomeCategory).department(dept1).createdBy(mainAdmin)
                .transactionDate(LocalDate.now()).build();

        TransactionService svc = buildSvc(
                userRepo(Map.of("admin", mainAdmin)),
                categoryRepo(Map.of(100L, incomeCategory)),
                deptRepo(Map.of(10L, dept1)),
                txRepo(saved, Collections.emptyMap(), null, null, 0, 0, 0)
        );

        TransactionRequest req = TransactionRequest.builder()
                .transactionType(TransactionType.IN).amount(new BigDecimal("5000.00"))
                .paymentMethod(PaymentMethod.BANK_TRANSFER).departmentId(10L).categoryId(100L)
                .transactionDate(LocalDate.now()).build();

        TransactionResponse resp = svc.createTransaction(req, "admin");
        assertNotNull(resp);
        assertEquals(TransactionType.IN, resp.getTransactionType());
        assertEquals(new BigDecimal("5000.00"), resp.getAmount());
        assertEquals(10L, resp.getDepartmentId());
    }

    @Test
    void mainAdmin_missingDepartmentId_throwsIllegalArgument() {
        TransactionService svc = buildSvc(
                userRepo(Map.of("admin", mainAdmin)),
                categoryRepo(Collections.emptyMap()),
                deptRepo(Collections.emptyMap()),
                txRepo(null, Collections.emptyMap(), null, null, 0, 0, 0)
        );

        TransactionRequest req = TransactionRequest.builder()
                .transactionType(TransactionType.IN).amount(new BigDecimal("5000.00"))
                .paymentMethod(PaymentMethod.BANK_TRANSFER).categoryId(100L)
                .departmentId(null).transactionDate(LocalDate.now()).build();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> svc.createTransaction(req, "admin"));
        assertTrue(ex.getMessage().contains("Department ID is required"));
    }

    @Test
    void deptAdmin_createTransaction_autoAssignsDepartment() {
        Transaction saved = Transaction.builder().id(2L).transactionType(TransactionType.IN)
                .amount(new BigDecimal("2500.00")).paymentMethod(PaymentMethod.UPI)
                .category(incomeCategory).department(dept1).createdBy(deptAdmin)
                .transactionDate(LocalDate.now()).build();

        TransactionService svc = buildSvc(
                userRepo(Map.of("cseadmin", deptAdmin)),
                categoryRepo(Map.of(100L, incomeCategory)),
                deptRepo(Collections.emptyMap()),
                txRepo(saved, Collections.emptyMap(), null, null, 0, 0, 0)
        );

        // Client passes departmentId 999, service must force dept1 (id=10)
        TransactionRequest req = TransactionRequest.builder()
                .transactionType(TransactionType.IN).amount(new BigDecimal("2500.00"))
                .paymentMethod(PaymentMethod.UPI).departmentId(999L).categoryId(100L)
                .transactionDate(LocalDate.now()).build();

        TransactionResponse resp = svc.createTransaction(req, "cseadmin");
        assertNotNull(resp);
        assertEquals(10L, resp.getDepartmentId());
    }

    @Test
    void createTransaction_categoryTypeMismatch_throwsIllegalArgument() {
        TransactionService svc = buildSvc(
                userRepo(Map.of("cseadmin", deptAdmin)),
                categoryRepo(Map.of(100L, incomeCategory)),  // IN category
                deptRepo(Collections.emptyMap()),
                txRepo(null, Collections.emptyMap(), null, null, 0, 0, 0)
        );

        TransactionRequest req = TransactionRequest.builder()
                .transactionType(TransactionType.OUT)  // OUT but category is IN
                .amount(new BigDecimal("1000.00")).paymentMethod(PaymentMethod.CASH)
                .categoryId(100L).transactionDate(LocalDate.now()).build();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> svc.createTransaction(req, "cseadmin"));
        assertTrue(ex.getMessage().contains("Category type mismatch"));
    }

    @Test
    void createTransaction_inactiveCategory_throwsIllegalArgument() {
        incomeCategory.setActive(false);
        TransactionService svc = buildSvc(
                userRepo(Map.of("cseadmin", deptAdmin)),
                categoryRepo(Map.of(100L, incomeCategory)),
                deptRepo(Collections.emptyMap()),
                txRepo(null, Collections.emptyMap(), null, null, 0, 0, 0)
        );

        TransactionRequest req = TransactionRequest.builder()
                .transactionType(TransactionType.IN).amount(new BigDecimal("1000.00"))
                .paymentMethod(PaymentMethod.CASH).categoryId(100L)
                .transactionDate(LocalDate.now()).build();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> svc.createTransaction(req, "cseadmin"));
        assertTrue(ex.getMessage().contains("inactive category"));
    }

    @Test
    void getTransactions_invalidDateRange_throwsIllegalArgument() {
        TransactionService svc = buildSvc(
                userRepo(Map.of("admin", mainAdmin)),
                categoryRepo(Collections.emptyMap()),
                deptRepo(Collections.emptyMap()),
                txRepo(null, Collections.emptyMap(), null, null, 0, 0, 0)
        );

        LocalDate start = LocalDate.of(2026, 10, 15);
        LocalDate end   = LocalDate.of(2026, 10,  1);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> svc.getTransactions(null, null, null, null, start, end, null, "admin"));
        assertEquals("Start date cannot be after end date", ex.getMessage());
    }

    @Test
    void deptAdmin_viewOtherDeptTransaction_throwsAccessDenied() {
        Transaction otherTx = Transaction.builder().id(50L).department(dept2).build();
        TransactionService svc = buildSvc(
                userRepo(Map.of("cseadmin", deptAdmin)),
                categoryRepo(Collections.emptyMap()),
                deptRepo(Collections.emptyMap()),
                txRepo(null, Map.of(50L, otherTx), null, null, 0, 0, 0)
        );

        assertThrows(AccessDeniedException.class, () -> svc.getTransactionById(50L, "cseadmin"));
    }

    @Test
    void deptAdmin_updateOtherDeptTransaction_throwsAccessDenied() {
        Transaction otherTx = Transaction.builder().id(50L).department(dept2).build();
        TransactionService svc = buildSvc(
                userRepo(Map.of("cseadmin", deptAdmin)),
                categoryRepo(Collections.emptyMap()),
                deptRepo(Collections.emptyMap()),
                txRepo(null, Map.of(50L, otherTx), null, null, 0, 0, 0)
        );

        TransactionRequest req = TransactionRequest.builder()
                .transactionType(TransactionType.IN).amount(new BigDecimal("100.00"))
                .paymentMethod(PaymentMethod.CASH).categoryId(100L)
                .transactionDate(LocalDate.now()).build();

        assertThrows(AccessDeniedException.class, () -> svc.updateTransaction(50L, req, "cseadmin"));
    }

    @Test
    void deptAdmin_deleteOtherDeptTransaction_throwsAccessDenied() {
        Transaction otherTx = Transaction.builder().id(50L).department(dept2).build();
        TransactionService svc = buildSvc(
                userRepo(Map.of("cseadmin", deptAdmin)),
                categoryRepo(Collections.emptyMap()),
                deptRepo(Collections.emptyMap()),
                txRepo(null, Map.of(50L, otherTx), null, null, 0, 0, 0)
        );

        assertThrows(AccessDeniedException.class, () -> svc.deleteTransaction(50L, "cseadmin"));
    }

    @Test
    void getCashflowSummary_calculatesNetBalance() {
        BigDecimal income  = new BigDecimal("10000.00");
        BigDecimal expense = new BigDecimal("3000.00");

        TransactionService svc = buildSvc(
                userRepo(Map.of("admin", mainAdmin)),
                categoryRepo(Collections.emptyMap()),
                deptRepo(Collections.emptyMap()),
                txRepo(null, Collections.emptyMap(), income, expense, 10L, 7L, 3L)
        );

        CashflowSummaryResponse summary = svc.getCashflowSummary(null, "admin");

        assertNotNull(summary);
        assertEquals(new BigDecimal("10000.00"), summary.getTotalIncome());
        assertEquals(new BigDecimal("3000.00"),  summary.getTotalExpense());
        assertEquals(new BigDecimal("7000.00"),  summary.getNetBalance());
        assertEquals(10L, summary.getTotalTransactions());
        assertEquals(7L,  summary.getIncomeCount());
        assertEquals(3L,  summary.getExpenseCount());
    }

    @Test
    void deptAdmin_getTransactions_scopesToOwnDepartment() {
        java.util.concurrent.atomic.AtomicReference<Long> capturedDeptId = new java.util.concurrent.atomic.AtomicReference<>();
        TransactionRepository customRepo = new TransactionRepository() {
            @Override public List<Transaction> searchTransactions(Long dId, TransactionType tt, Long cId,
                    PaymentMethod pm, LocalDate sd, LocalDate ed, String s) {
                capturedDeptId.set(dId);
                return Collections.emptyList();
            }
            @Override public Optional<Transaction> findById(Long id) { return Optional.empty(); }
            @Override public BigDecimal sumAmountByTypeAndDepartment(TransactionType type, Long deptId) { return BigDecimal.ZERO; }
            @Override public long countByTypeAndDepartment(TransactionType type, Long deptId) { return 0; }
            @Override public long countTotalByDepartment(Long deptId) { return 0; }
            @Override public List<Transaction> findByDepartmentIdOrderByTransactionDateDescIdDesc(Long dId) { return Collections.emptyList(); }
            @Override public List<Transaction> findAllByOrderByTransactionDateDescIdDesc() { return Collections.emptyList(); }
            @Override public List<Object[]> findDailyCashflowRaw(LocalDate sd, LocalDate ed, Long dId) { return Collections.emptyList(); }
            @Override public List<Object[]> findCategoryBreakdownRaw(LocalDate sd, LocalDate ed, Long dId, String tt) { return Collections.emptyList(); }
            @Override public List<Object[]> findDepartmentSummaryRaw(LocalDate sd, LocalDate ed) { return Collections.emptyList(); }
            @Override public List<Transaction> findByDateAndOptionalDepartment(LocalDate date, Long dId) { return Collections.emptyList(); }
            @Override public List<Object[]> findMonthlySummaryRaw(int year, Long dId) { return Collections.emptyList(); }
            @Override public <S extends Transaction> S save(S e) { return e; }
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

        TransactionService svc = buildSvc(
                userRepo(Map.of("cseadmin", deptAdmin)),
                categoryRepo(Collections.emptyMap()),
                deptRepo(Collections.emptyMap()),
                customRepo
        );

        // DeptAdmin passes departmentId = 999L (trying to view another department)
        svc.getTransactions(999L, null, null, null, null, null, null, "cseadmin");

        // Must be scoped to their own dept1 (id = 10L)
        assertEquals(10L, capturedDeptId.get());
    }

    @Test
    void deptAdmin_withoutAssignedDepartment_getTransactions_throwsAccessDenied() {
        User unassignedDeptAdmin = User.builder().id(9L).username("unassigned").role(Role.DEPT_ADMIN).department(null).active(true).build();
        TransactionService svc = buildSvc(
                userRepo(Map.of("unassigned", unassignedDeptAdmin)),
                categoryRepo(Collections.emptyMap()),
                deptRepo(Collections.emptyMap()),
                txRepo(null, Collections.emptyMap(), null, null, 0, 0, 0)
        );

        assertThrows(AccessDeniedException.class,
                () -> svc.getTransactions(null, null, null, null, null, null, null, "unassigned"));
    }
}
