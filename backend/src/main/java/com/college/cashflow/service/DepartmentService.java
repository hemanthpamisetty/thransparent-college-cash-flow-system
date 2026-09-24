package com.college.cashflow.service;

import com.college.cashflow.dto.DepartmentRequest;
import com.college.cashflow.dto.DepartmentResponse;
import com.college.cashflow.entity.Department;
import com.college.cashflow.exception.ResourceConflictException;
import com.college.cashflow.exception.ResourceNotFoundException;
import com.college.cashflow.repository.DepartmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    public DepartmentService(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    @Transactional(readOnly = true)
    public List<DepartmentResponse> getAllDepartments() {
        return departmentRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DepartmentResponse getDepartmentById(Long id) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));
        return mapToResponse(dept);
    }

    @Transactional
    public DepartmentResponse createDepartment(DepartmentRequest request) {
        if (departmentRepository.existsByName(request.getName().trim())) {
            throw new ResourceConflictException("Department with name '" + request.getName() + "' already exists");
        }
        if (departmentRepository.existsByCode(request.getCode().trim().toUpperCase())) {
            throw new ResourceConflictException("Department with code '" + request.getCode() + "' already exists");
        }

        Department department = Department.builder()
                .name(request.getName().trim())
                .code(request.getCode().trim().toUpperCase())
                .description(request.getDescription())
                .active(true)
                .build();

        Department saved = departmentRepository.save(department);
        return mapToResponse(saved);
    }

    @Transactional
    public DepartmentResponse updateDepartment(Long id, DepartmentRequest request) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));

        String trimmedName = request.getName().trim();
        String trimmedCode = request.getCode().trim().toUpperCase();

        if (departmentRepository.existsByNameAndIdNot(trimmedName, id)) {
            throw new ResourceConflictException("Department with name '" + request.getName() + "' already exists");
        }
        if (departmentRepository.existsByCodeAndIdNot(trimmedCode, id)) {
            throw new ResourceConflictException("Department with code '" + request.getCode() + "' already exists");
        }

        dept.setName(trimmedName);
        dept.setCode(trimmedCode);
        dept.setDescription(request.getDescription());

        Department updated = departmentRepository.save(dept);
        return mapToResponse(updated);
    }

    @Transactional
    public DepartmentResponse toggleStatus(Long id, Boolean active) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));

        dept.setActive(active);
        Department updated = departmentRepository.save(dept);
        return mapToResponse(updated);
    }

    private DepartmentResponse mapToResponse(Department dept) {
        return DepartmentResponse.builder()
                .id(dept.getId())
                .name(dept.getName())
                .code(dept.getCode())
                .description(dept.getDescription())
                .active(dept.getActive())
                .createdAt(dept.getCreatedAt())
                .updatedAt(dept.getUpdatedAt())
                .build();
    }
}
