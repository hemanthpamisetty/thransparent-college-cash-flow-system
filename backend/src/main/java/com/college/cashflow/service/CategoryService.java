package com.college.cashflow.service;

import com.college.cashflow.dto.CategoryRequest;
import com.college.cashflow.dto.CategoryResponse;
import com.college.cashflow.entity.Category;
import com.college.cashflow.enums.CategoryType;
import com.college.cashflow.exception.ResourceConflictException;
import com.college.cashflow.exception.ResourceNotFoundException;
import com.college.cashflow.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories(CategoryType type) {
        List<Category> categories = (type != null)
                ? categoryRepository.findByType(type)
                : categoryRepository.findAll();

        return categories.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        return mapToResponse(category);
    }

    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        String trimmedName = request.getName().trim();
        if (categoryRepository.existsByName(trimmedName)) {
            throw new ResourceConflictException("Category with name '" + request.getName() + "' already exists");
        }

        Category category = Category.builder()
                .name(trimmedName)
                .type(request.getType())
                .description(request.getDescription())
                .active(true)
                .build();

        Category saved = categoryRepository.save(category);
        return mapToResponse(saved);
    }

    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        String trimmedName = request.getName().trim();
        if (categoryRepository.existsByNameAndIdNot(trimmedName, id)) {
            throw new ResourceConflictException("Category with name '" + request.getName() + "' already exists");
        }

        category.setName(trimmedName);
        category.setType(request.getType());
        category.setDescription(request.getDescription());

        Category updated = categoryRepository.save(category);
        return mapToResponse(updated);
    }

    @Transactional
    public CategoryResponse toggleStatus(Long id, Boolean active) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        category.setActive(active);
        Category updated = categoryRepository.save(category);
        return mapToResponse(updated);
    }

    private CategoryResponse mapToResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .type(category.getType())
                .description(category.getDescription())
                .active(category.getActive())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }
}
