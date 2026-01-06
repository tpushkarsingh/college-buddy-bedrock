package com.college.buddy.controller;

import com.college.buddy.controller.dto.UserDTO;
import com.college.buddy.service.UserManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UserManagementController {

    private final UserManagementService userManagementService;

    @GetMapping("/students")
    public ResponseEntity<Page<UserDTO>> getStudents(
            @RequestParam(required = false) String departmentCode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<UserDTO> students = userManagementService.getStudents(departmentCode, page, size);
        return ResponseEntity.ok(students);
    }

    @GetMapping("/faculty")
    public ResponseEntity<Page<UserDTO>> getFaculty(
            @RequestParam(required = false) String departmentCode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<UserDTO> faculty = userManagementService.getFaculty(departmentCode, page, size);
        return ResponseEntity.ok(faculty);
    }

    @PostMapping("/{userId}/toggle-active")
    public ResponseEntity<UserDTO> toggleUserActive(@PathVariable UUID userId) {
        UserDTO user = userManagementService.toggleUserActive(userId);
        return ResponseEntity.ok(user);
    }
}
