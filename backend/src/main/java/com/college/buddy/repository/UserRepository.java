package com.college.buddy.repository;

import com.college.buddy.entity.Role;
import com.college.buddy.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    // Find users by role with pagination
    Page<User> findByRole(Role role, Pageable pageable);

    // Find users by multiple roles with pagination
    Page<User> findByRoleIn(List<Role> roles, Pageable pageable);

    // Find users by role and department code (via enrollment)
    @Query("SELECT DISTINCT u FROM User u " +
            "JOIN Enrollment e ON e.user.id = u.id " +
            "WHERE u.role = :role AND e.department.code = :departmentCode")
    Page<User> findByRoleAndDepartmentCode(@Param("role") Role role,
            @Param("departmentCode") String departmentCode,
            Pageable pageable);

    // Find users by multiple roles and department code
    @Query("SELECT DISTINCT u FROM User u " +
            "JOIN Enrollment e ON e.user.id = u.id " +
            "WHERE u.role IN :roles AND e.department.code = :departmentCode")
    Page<User> findByRoleInAndDepartmentCode(@Param("roles") List<Role> roles,
            @Param("departmentCode") String departmentCode,
            Pageable pageable);
}
