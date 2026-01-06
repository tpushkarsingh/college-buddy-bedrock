package com.college.buddy.controller.dto;

import com.college.buddy.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {
    private UUID id;
    private String fullName;
    private String email;
    private Role role;
    private String departmentCode;
    private String departmentName;
    private Integer year;
    private String section;
    private String studentId;
    private String employeeId;
    private boolean isActive;
    private boolean isApproved;
}
