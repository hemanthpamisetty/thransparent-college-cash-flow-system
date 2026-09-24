package com.college.cashflow.repository;

import com.college.cashflow.entity.Category;
import com.college.cashflow.enums.CategoryType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByName(String name);

    List<Category> findByType(CategoryType type);

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);
}
