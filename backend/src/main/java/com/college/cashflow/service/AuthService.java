package com.college.cashflow.service;

import com.college.cashflow.dto.*;
import com.college.cashflow.entity.User;
import com.college.cashflow.enums.Role;
import com.college.cashflow.repository.UserRepository;
import com.college.cashflow.security.JwtTokenProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /**
     * Main Admin Login Flow:
     * 1. Find user by username
     * 2. Verify role = MAIN_ADMIN
     * 3. Verify user is active
     * 4. Verify password matches
     * 5. Generate and return JWT
     */
    public LoginResponse mainAdminLogin(MainAdminLoginRequest request) {

        // 1. Find user
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));

        // 2. Verify MAIN_ADMIN role
        if (user.getRole() != Role.MAIN_ADMIN) {
            throw new BadCredentialsException("Invalid username or password");
        }

        // 3. Verify active
        if (!user.getActive()) {
            throw new BadCredentialsException("Account is deactivated");
        }

        // 4. Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid username or password");
        }

        // 5. Generate token
        String token = jwtTokenProvider.generateToken(user);

        return LoginResponse.builder()
                .token(token)
                .username(user.getUsername())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .departmentId(null)
                .departmentName(null)
                .build();
    }

    /**
     * Department Admin Login Flow:
     * 1. Find and verify the Main Admin exists
     * 2. Find the Department Admin by username
     * 3. Verify role = DEPT_ADMIN
     * 4. Verify Department Admin is active
     * 5. Verify parent_admin_id matches the Main Admin
     * 6. Verify department is assigned
     * 7. Verify password matches
     * 8. Generate and return JWT with department info
     */
    public LoginResponse deptAdminLogin(DeptAdminLoginRequest request) {

        // 1. Find Main Admin
        User mainAdmin = userRepository.findByUsernameAndRole(
                        request.getAdminUsername(), Role.MAIN_ADMIN)
                .orElseThrow(() -> new BadCredentialsException("Invalid admin credentials"));

        // Verify Main Admin is active
        if (!mainAdmin.getActive()) {
            throw new BadCredentialsException("Admin account is deactivated");
        }

        // 2. Find Department Admin
        User deptAdmin = userRepository.findByUsername(request.getDepartmentAdminUsername())
                .orElseThrow(() -> new BadCredentialsException("Invalid department admin credentials"));

        // 3. Verify DEPT_ADMIN role
        if (deptAdmin.getRole() != Role.DEPT_ADMIN) {
            throw new BadCredentialsException("Invalid department admin credentials");
        }

        // 4. Verify active
        if (!deptAdmin.getActive()) {
            throw new BadCredentialsException("Department admin account is deactivated");
        }

        // 5. Verify hierarchy — parent_admin_id must match the Main Admin's id
        if (deptAdmin.getParentAdmin() == null ||
                !deptAdmin.getParentAdmin().getId().equals(mainAdmin.getId())) {
            throw new BadCredentialsException("Invalid admin hierarchy");
        }

        // 6. Verify department is assigned
        if (deptAdmin.getDepartment() == null) {
            throw new BadCredentialsException("No department assigned to this admin");
        }

        // 7. Verify password
        if (!passwordEncoder.matches(request.getPassword(), deptAdmin.getPasswordHash())) {
            throw new BadCredentialsException("Invalid department admin credentials");
        }

        // 8. Generate token
        String token = jwtTokenProvider.generateToken(deptAdmin);

        return LoginResponse.builder()
                .token(token)
                .username(deptAdmin.getUsername())
                .fullName(deptAdmin.getFullName())
                .role(deptAdmin.getRole().name())
                .departmentId(deptAdmin.getDepartment().getId())
                .departmentName(deptAdmin.getDepartment().getName())
                .build();
    }
}
