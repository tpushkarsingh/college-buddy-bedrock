package com.college.buddy.service;

import com.college.buddy.controller.dto.UserDTO;
import com.college.buddy.entity.Role;
import com.college.buddy.entity.User;
import com.college.buddy.repository.EnrollmentRepository;
import com.college.buddy.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserManagementService {

    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;

    public Page<UserDTO> getStudents(String departmentCode, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("fullName").ascending());
        Page<User> users;

        if (departmentCode != null && !departmentCode.isEmpty()) {
            users = userRepository.findByRoleAndDepartmentCode(Role.STUDENT, departmentCode, pageable);
        } else {
            users = userRepository.findByRole(Role.STUDENT, pageable);
        }

        return users.map(this::mapToDTO);
    }

    public Page<UserDTO> getFaculty(String departmentCode, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("fullName").ascending());
        Page<User> users;

        if (departmentCode != null && !departmentCode.isEmpty()) {
            // For faculty, we'll filter by department if they have enrollments or
            // assignments
            users = userRepository.findByRoleInAndDepartmentCode(
                    java.util.List.of(Role.FACULTY, Role.FACULTY_ASSISTANT),
                    departmentCode,
                    pageable);
        } else {
            users = userRepository.findByRoleIn(
                    java.util.List.of(Role.FACULTY, Role.FACULTY_ASSISTANT),
                    pageable);
        }

        return users.map(this::mapToDTO);
    }

    @Transactional
    public UserDTO toggleUserActive(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setActive(!user.isActive());
        User savedUser = userRepository.save(user);

        return mapToDTO(savedUser);
    }

    private UserDTO mapToDTO(User user) {
        var enrollment = enrollmentRepository.findByUserIdAndIsActiveTrue(user.getId())
                .orElse(null);

        return UserDTO.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .departmentCode(enrollment != null ? enrollment.getDepartment().getCode() : null)
                .departmentName(enrollment != null ? enrollment.getDepartment().getName() : null)
                .year(enrollment != null ? enrollment.getYear() : null)
                .section(enrollment != null ? enrollment.getSection() : null)
                .studentId(user.getStudentId())
                .employeeId(user.getEmployeeId())
                .isActive(user.isActive())
                .isApproved(user.isApproved())
                .build();
    }
}
