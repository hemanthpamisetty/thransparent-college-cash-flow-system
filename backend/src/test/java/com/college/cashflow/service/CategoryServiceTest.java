package com.college.cashflow.service;

import com.college.cashflow.dto.CategoryRequest;
import com.college.cashflow.dto.CategoryResponse;
import com.college.cashflow.entity.Category;
import com.college.cashflow.enums.CategoryType;
import com.college.cashflow.exception.ResourceConflictException;
import com.college.cashflow.repository.CategoryRepository;
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
 * Unit tests for CategoryService — stub-based, no Mockito.
 */
class CategoryServiceTest {

    private final Category category = Category.builder()
            .id(1L).name("Student Fees").type(CategoryType.IN)
            .description("Tuition").active(true).build();

    // ── Stub ─────────────────────────────────────────────────────────────────────

    private CategoryRepository stubRepo(boolean nameExists, Category saved, Category foundById) {
        return new CategoryRepository() {
            @Override public boolean existsByName(String n) { return nameExists; }
            @Override public boolean existsByNameAndIdNot(String n, Long id) { return false; }
            @Override public Optional<Category> findByName(String n) { return Optional.empty(); }
            @Override public List<Category> findByType(CategoryType t) { return Collections.emptyList(); }
            @Override public <S extends Category> S save(S e) {
                if (saved != null) { @SuppressWarnings("unchecked") S r = (S) saved; return r; }
                return e;
            }
            @Override public Optional<Category> findById(Long id) { return Optional.ofNullable(foundById); }
            // ── JpaRepository boilerplate ──────────────────────────────────────
            @Override public List<Category> findAll() { return Collections.emptyList(); }
            @Override public List<Category> findAll(Sort s) { return Collections.emptyList(); }
            @Override public Page<Category> findAll(Pageable p) { return Page.empty(); }
            @Override public List<Category> findAllById(Iterable<Long> ids) { return Collections.emptyList(); }
            @Override public <S extends Category> List<S> saveAll(Iterable<S> es) { return Collections.emptyList(); }
            @Override public boolean existsById(Long id) { return foundById != null; }
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
            @Override public Category getReferenceById(Long id) { return foundById; }
        };
    }

    // ── Tests ─────────────────────────────────────────────────────────────────────

    @Test
    void createCategory_success() {
        CategoryService svc = new CategoryService(stubRepo(false, category, null));
        CategoryRequest req = new CategoryRequest("Student Fees", CategoryType.IN, "Tuition");

        CategoryResponse resp = svc.createCategory(req);

        assertNotNull(resp);
        assertEquals("Student Fees", resp.getName());
        assertEquals(CategoryType.IN, resp.getType());
    }

    @Test
    void createCategory_duplicateName_throwsConflict() {
        CategoryService svc = new CategoryService(stubRepo(true, null, null));
        CategoryRequest req = new CategoryRequest("Student Fees", CategoryType.IN, "Tuition");

        assertThrows(ResourceConflictException.class, () -> svc.createCategory(req));
    }

    @Test
    void toggleStatus_deactivatesCategory() {
        CategoryService svc = new CategoryService(stubRepo(false, category, category));

        CategoryResponse resp = svc.toggleStatus(1L, false);

        assertNotNull(resp);
        assertFalse(category.getActive());
    }
}
