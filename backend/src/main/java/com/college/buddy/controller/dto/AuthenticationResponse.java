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
public class AuthenticationResponse {
    private String token;
    private String fullName;
    private Role role;
    private boolean isApproved;
}
