package com.college.cashflow.service;

import com.college.cashflow.dto.DepartmentRequest;
import com.college.cashflow.dto.DepartmentResponse;
import com.college.cashflow.entity.Department;
import com.college.cashflow.exception.ResourceConflictException;
import com.college.cashflow.repository.DepartmentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;

import java.util.*;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DepartmentService — stub-based, no Mockito.
 */
class DepartmentServiceTest {

    private final Department dept = Department.builder()
            .id(1L).name("Computer Science").code("CSE")
            .description("CSE Dept").active(true).build();

    // ── Stub ─────────────────────────────────────────────────────────────────────

    private DepartmentRepository stubRepo(boolean nameExists, boolean codeExists, Department foundById) {
        return new DepartmentRepository() {
            @Override public boolean existsByName(String n) { return nameExists; }
            @Override public boolean existsByCode(String c) { return codeExists; }
            @Override public boolean existsByNameAndIdNot(String n, Long id) { return false; }
            @Override public boolean existsByCodeAndIdNot(String c, Long id) { return false; }
            @Override public Optional<Department> findByCode(String c) { return Optional.empty(); }
            @Override public Optional<Department> findByName(String n) { return Optional.empty(); }
            @Override public <S extends Department> S save(S e) { return e; }
            @Override public Optional<Department> findById(Long id) { return Optional.ofNullable(foundById); }
            // ── JpaRepository boilerplate ──────────────────────────────────────
            @Override public List<Department> findAll() { return Collections.emptyList(); }
            @Override public List<Department> findAll(Sort s) { return Collections.emptyList(); }
            @Override public Page<Department> findAll(Pageable p) { return Page.empty(); }
            @Override public List<Department> findAllById(Iterable<Long> ids) { return Collections.emptyList(); }
            @Override public <S extends Department> List<S> saveAll(Iterable<S> es) { return Collections.emptyList(); }
            @Override public boolean existsById(Long id) { return foundById != null; }
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
            @Override public Department getReferenceById(Long id) { return foundById; }
        };
    }

    // ── Tests ─────────────────────────────────────────────────────────────────────

    @Test
    void createDepartment_success() {
        DepartmentService svc = new DepartmentService(stubRepo(false, false, null));
        DepartmentRequest req = new DepartmentRequest("Computer Science", "CSE", "CSE Dept");

        DepartmentResponse resp = svc.createDepartment(req);

        assertNotNull(resp);
        assertEquals("Computer Science", resp.getName());
        assertEquals("CSE", resp.getCode());
    }

    @Test
    void createDepartment_duplicateName_throwsConflict() {
        DepartmentService svc = new DepartmentService(stubRepo(true, false, null));
        assertThrows(ResourceConflictException.class,
                () -> svc.createDepartment(new DepartmentRequest("Computer Science", "CSE", "CSE Dept")));
    }

    @Test
    void createDepartment_duplicateCode_throwsConflict() {
        DepartmentService svc = new DepartmentService(stubRepo(false, true, null));
        assertThrows(ResourceConflictException.class,
                () -> svc.createDepartment(new DepartmentRequest("Computer Science", "CSE", "CSE Dept")));
    }

    @Test
    void toggleStatus_deactivatesDepartment() {
        DepartmentService svc = new DepartmentService(stubRepo(false, false, dept));

        DepartmentResponse resp = svc.toggleStatus(1L, false);

        assertNotNull(resp);
        assertFalse(dept.getActive());
    }
}
