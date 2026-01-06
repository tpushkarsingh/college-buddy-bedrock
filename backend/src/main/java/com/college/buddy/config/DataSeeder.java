package com.college.buddy.config;

import com.college.buddy.entity.AcademicSession;
import com.college.buddy.entity.Department;
import com.college.buddy.entity.Role;
import com.college.buddy.entity.User;
import com.college.buddy.repository.AcademicSessionRepository;
import com.college.buddy.repository.DepartmentRepository;
import com.college.buddy.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final AcademicSessionRepository sessionRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        seedAdmin();
        seedDepartments();
        seedSessions();
    }

    private void seedAdmin() {
        var adminEmail = "admin@college.com";
        var adminPassword = "admin123";

        userRepository.findByEmail(adminEmail).ifPresentOrElse(
                user -> {
                    user.setPasswordHash(passwordEncoder.encode(adminPassword));
                    user.setRole(Role.ADMIN);
                    user.setActive(true);
                    user.setApproved(true);
                    userRepository.save(user);
                    System.out.println("Admin user updated: " + adminEmail);
                },
                () -> {
                    var admin = User.builder()
                            .fullName("System Admin")
                            .email(adminEmail)
                            .passwordHash(passwordEncoder.encode(adminPassword))
                            .role(Role.ADMIN)
                            .isActive(true)
                            .isApproved(true)
                            .tempPasswordActive(false)
                            .build();
                    userRepository.save(admin);
                    System.out.println("Admin user seeded: " + adminEmail);
                });
    }

    private void seedDepartments() {
        if (departmentRepository.count() == 0) {
            departmentRepository.save(Department.builder().name("Computer Science").code("CSE").build());
            departmentRepository.save(Department.builder().name("Electronics").code("ECE").build());
            departmentRepository.save(Department.builder().name("Mechanical").code("MECH").build());
            System.out.println("Departments seeded.");
        }
    }

    private void seedSessions() {
        if (sessionRepository.count() == 0) {
            sessionRepository.save(AcademicSession.builder()
                    .name("2024-2025")
                    .startDate(LocalDate.of(2024, 8, 1))
                    .endDate(LocalDate.of(2025, 5, 31))
                    .isCurrent(true)
                    .build());
            System.out.println("Academic Session seeded.");
        }
    }
}
