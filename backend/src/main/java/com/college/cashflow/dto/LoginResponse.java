package com.college.cashflow.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {

    private String token;
    private String username;
    private String fullName;
    private String role;
    private Long departmentId;
    private String departmentName;
}
