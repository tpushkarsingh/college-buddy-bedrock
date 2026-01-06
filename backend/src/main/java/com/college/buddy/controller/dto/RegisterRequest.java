package com.college.buddy.controller.dto;

import com.college.buddy.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {
    private String fullName;
    private String email;
    private String password;
    private Role role; // The role they are applying for

    // Enrollment Details
    private String departmentCode;
    private Integer year;
    private String section;

    // ID Fields
    private String studentId; // For STUDENT role
    private String employeeId; // For FACULTY and FACULTY_ASSISTANT roles
}
