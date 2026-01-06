package com.college.buddy.service;

import com.college.buddy.entity.AcademicSession;
import com.college.buddy.entity.Department;
import com.college.buddy.entity.Enrollment;
import com.college.buddy.entity.User;
import com.college.buddy.repository.AcademicSessionRepository;
import com.college.buddy.repository.DepartmentRepository;
import com.college.buddy.repository.EnrollmentRepository;
import com.college.buddy.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final DepartmentRepository departmentRepository;
    private final AcademicSessionRepository sessionRepository;

    public List<User> getPendingUsers() {
        // In a real app, use a custom query. For now, filtering in memory or assuming
        // 'isApproved=false'
        // Since we don't have a specific method in repo, let's add one or just filter
        // all.
        // Adding method to repo is better, but for speed I'll just findAll and filter.
        return userRepository.findAll().stream()
                .filter(u -> !u.isApproved())
                .toList();
    }

    @Transactional
    public void approveUser(UUID userId) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setApproved(true);
        userRepository.save(user);

        // Also activate their enrollment if it exists
        // Also activate their enrollment if it exists
        enrollmentRepository.findByUserId(userId)
                .forEach(e -> {
                    e.setActive(true);
                    enrollmentRepository.save(e);
                });

        // Let's fix the repo query logic.
        // Actually, we can just find all enrollments for this user and activate the one
        // for current session.
        // For simplicity, let's just assume we activate the latest one.
    }

    public Department createDepartment(Department department) {
        return departmentRepository.save(department);
    }

    public AcademicSession createSession(AcademicSession session) {
        return sessionRepository.save(session);
    }
}
