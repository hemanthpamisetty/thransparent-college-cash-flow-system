package com.college.cashflow.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeptAdminResponse {

    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String role;
    private Long departmentId;
    private String departmentName;
    private String departmentCode;
    private Long parentAdminId;
    private String parentAdminUsername;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
