package com.college.cashflow.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DeptAdminLoginRequest {

    @NotBlank(message = "Admin username is required")
    private String adminUsername;

    @NotBlank(message = "Department admin username is required")
    private String departmentAdminUsername;

    @NotBlank(message = "Password is required")
    private String password;
}
