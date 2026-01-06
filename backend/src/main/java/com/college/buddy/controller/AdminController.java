package com.college.buddy.controller;

import com.college.buddy.entity.AcademicSession;
import com.college.buddy.entity.Department;
import com.college.buddy.entity.User;
import com.college.buddy.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users/pending")
    public ResponseEntity<List<User>> getPendingUsers() {
        return ResponseEntity.ok(adminService.getPendingUsers());
    }

    @PostMapping("/users/{userId}/approve")
    public ResponseEntity<Void> approveUser(@PathVariable UUID userId) {
        adminService.approveUser(userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/departments")
    public ResponseEntity<Department> createDepartment(@RequestBody Department department) {
        return ResponseEntity.ok(adminService.createDepartment(department));
    }

    @PostMapping("/sessions")
    public ResponseEntity<AcademicSession> createSession(@RequestBody AcademicSession session) {
        return ResponseEntity.ok(adminService.createSession(session));
    }
}
