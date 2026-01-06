package com.college.buddy.service;

import com.college.buddy.config.JwtUtils;
import com.college.buddy.controller.dto.AuthenticationRequest;
import com.college.buddy.controller.dto.AuthenticationResponse;
import com.college.buddy.controller.dto.RegisterRequest;
import com.college.buddy.entity.*;
import com.college.buddy.repository.AcademicSessionRepository;
import com.college.buddy.repository.DepartmentRepository;
import com.college.buddy.repository.EnrollmentRepository;
import com.college.buddy.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

        private final UserRepository userRepository;
        private final DepartmentRepository departmentRepository;
        private final AcademicSessionRepository sessionRepository;
        private final EnrollmentRepository enrollmentRepository;
        private final PasswordEncoder passwordEncoder;
        private final JwtUtils jwtUtils;
        private final AuthenticationManager authenticationManager;

        @Transactional
        public AuthenticationResponse register(RegisterRequest request) {
                if (userRepository.existsByEmail(request.getEmail())) {
                        throw new RuntimeException("Email already exists");
                }

                var user = User.builder()
                                .fullName(request.getFullName())
                                .email(request.getEmail())
                                .passwordHash(passwordEncoder.encode(request.getPassword()))
                                .role(request.getRole())
                                .isActive(true)
                                .isApproved(false) // Requires Admin Approval
                                .tempPasswordActive(false) // Self-registered users set their own password
                                .studentId(request.getStudentId())
                                .employeeId(request.getEmployeeId())
                                .build();

                var savedUser = userRepository.save(user);

                // If user provided enrollment details, create an inactive enrollment
                if (request.getDepartmentCode() != null && request.getYear() != null && request.getSection() != null) {
                        var department = departmentRepository.findByCode(request.getDepartmentCode())
                                        .orElseThrow(() -> new RuntimeException("Department not found"));

                        var currentSession = sessionRepository.findByIsCurrentTrue()
                                        .orElseThrow(() -> new RuntimeException("No active academic session found"));

                        var enrollment = Enrollment.builder()
                                        .user(savedUser)
                                        .department(department)
                                        .session(currentSession)
                                        .year(request.getYear())
                                        .section(request.getSection())
                                        .isActive(false) // Enrollment is inactive until User is approved
                                        .build();

                        enrollmentRepository.save(enrollment);
                }

                var jwtToken = jwtUtils.generateToken(user);
                return AuthenticationResponse.builder()
                                .token(jwtToken)
                                .fullName(user.getFullName())
                                .role(user.getRole())
                                .isApproved(user.isApproved())
                                .build();
        }

        public AuthenticationResponse authenticate(AuthenticationRequest request) {
                try {
                        authenticationManager.authenticate(
                                        new UsernamePasswordAuthenticationToken(
                                                        request.getEmail(),
                                                        request.getPassword()));
                } catch (Exception e) {
                        throw e;
                }

                var user = userRepository.findByEmail(request.getEmail())
                                .orElseThrow();

                if (!user.isApproved()) {
                        throw new RuntimeException("Account not approved by Admin yet.");
                }

                var jwtToken = jwtUtils.generateToken(user);
                return AuthenticationResponse.builder()
                                .token(jwtToken)
                                .fullName(user.getFullName())
                                .role(user.getRole())
                                .isApproved(user.isApproved())
                                .build();
        }
}
