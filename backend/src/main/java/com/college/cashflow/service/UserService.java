package com.college.cashflow.service;

import com.college.cashflow.dto.DeptAdminCreateRequest;
import com.college.cashflow.dto.DeptAdminResponse;
import com.college.cashflow.dto.DeptAdminUpdateRequest;
import com.college.cashflow.entity.Department;
import com.college.cashflow.entity.User;
import com.college.cashflow.enums.Role;
import com.college.cashflow.exception.ResourceConflictException;
import com.college.cashflow.exception.ResourceNotFoundException;
import com.college.cashflow.repository.DepartmentRepository;
import com.college.cashflow.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       DepartmentRepository departmentRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<DeptAdminResponse> getAllDeptAdmins() {
        return userRepository.findByRole(Role.DEPT_ADMIN).stream()
                .map(this::mapToDeptAdminResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DeptAdminResponse getDeptAdminById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department Admin not found with id: " + id));

        if (user.getRole() != Role.DEPT_ADMIN) {
            throw new ResourceNotFoundException("User with id " + id + " is not a Department Admin");
        }

        return mapToDeptAdminResponse(user);
    }

    @Transactional
    public DeptAdminResponse createDeptAdmin(DeptAdminCreateRequest request, String mainAdminUsername) {
        // 1. Verify authenticated Main Admin
        User mainAdmin = userRepository.findByUsername(mainAdminUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Main Admin not found"));

        if (mainAdmin.getRole() != Role.MAIN_ADMIN) {
            throw new IllegalArgumentException("Only Main Admin can create Department Admins");
        }

        // 2. Check unique username
        String username = request.getUsername().trim();
        if (userRepository.existsByUsername(username)) {
            throw new ResourceConflictException("Username '" + username + "' is already taken");
        }

        // 3. Check unique email if provided
        if (StringUtils.hasText(request.getEmail())) {
            String email = request.getEmail().trim();
            if (userRepository.existsByEmail(email)) {
                throw new ResourceConflictException("Email '" + email + "' is already registered");
            }
        }

        // 4. Verify department exists and is active
        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + request.getDepartmentId()));

        if (!Boolean.TRUE.equals(department.getActive())) {
            throw new IllegalArgumentException("Cannot assign Department Admin to an inactive department");
        }

        // 5. Build and save user with server-assigned values
        User deptAdmin = User.builder()
                .username(username)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName().trim())
                .email(StringUtils.hasText(request.getEmail()) ? request.getEmail().trim() : null)
                .role(Role.DEPT_ADMIN)               // Server enforced
                .department(department)              // Validated department
                .parentAdmin(mainAdmin)              // Authenticated Main Admin
                .active(true)
                .build();

        User saved = userRepository.save(deptAdmin);
        return mapToDeptAdminResponse(saved);
    }

    @Transactional
    public DeptAdminResponse updateDeptAdmin(Long id, DeptAdminUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department Admin not found with id: " + id));

        if (user.getRole() != Role.DEPT_ADMIN) {
            throw new ResourceNotFoundException("User with id " + id + " is not a Department Admin");
        }

        // Check email uniqueness if modified
        if (StringUtils.hasText(request.getEmail())) {
            String email = request.getEmail().trim();
            if (userRepository.existsByEmailAndIdNot(email, id)) {
                throw new ResourceConflictException("Email '" + email + "' is already in use by another user");
            }
            user.setEmail(email);
        } else {
            user.setEmail(null);
        }

        // Verify and update department
        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + request.getDepartmentId()));

        if (!Boolean.TRUE.equals(department.getActive())) {
            throw new IllegalArgumentException("Cannot assign Department Admin to an inactive department");
        }

        user.setFullName(request.getFullName().trim());
        user.setDepartment(department);

        // Update password if provided
        if (StringUtils.hasText(request.getPassword())) {
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }

        User updated = userRepository.save(user);
        return mapToDeptAdminResponse(updated);
    }

    @Transactional
    public DeptAdminResponse toggleDeptAdminStatus(Long id, Boolean active) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department Admin not found with id: " + id));

        if (user.getRole() != Role.DEPT_ADMIN) {
            throw new ResourceNotFoundException("User with id " + id + " is not a Department Admin");
        }

        user.setActive(active);
        User updated = userRepository.save(user);
        return mapToDeptAdminResponse(updated);
    }

    private DeptAdminResponse mapToDeptAdminResponse(User user) {
        return DeptAdminResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .departmentId(user.getDepartment() != null ? user.getDepartment().getId() : null)
                .departmentName(user.getDepartment() != null ? user.getDepartment().getName() : null)
                .departmentCode(user.getDepartment() != null ? user.getDepartment().getCode() : null)
                .parentAdminId(user.getParentAdmin() != null ? user.getParentAdmin().getId() : null)
                .parentAdminUsername(user.getParentAdmin() != null ? user.getParentAdmin().getUsername() : null)
                .active(user.getActive())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
