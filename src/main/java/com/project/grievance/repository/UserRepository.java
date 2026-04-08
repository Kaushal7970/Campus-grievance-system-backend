package com.project.grievance.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.project.grievance.model.User;
import com.project.grievance.enums.Department;

public interface UserRepository extends JpaRepository<User, Long> {

    @Query(value = "SELECT * FROM users WHERE email = :email LIMIT 1", nativeQuery = true)
    Optional<User> findByEmail(@Param("email") String email);

    List<User> findByRoleIgnoreCase(String role);

    Optional<User> findFirstByRoleIgnoreCaseOrderByIdAsc(String role);

    @Query("SELECT u FROM User u WHERE u.department = :department AND UPPER(u.role) <> 'STUDENT' ORDER BY u.id ASC")
    List<User> findAssignableByDepartmentOrderByIdAsc(@Param("department") Department department);

    @Query("SELECT u FROM User u WHERE u.department = :department AND UPPER(u.role) <> 'STUDENT' AND UPPER(u.role) = UPPER(:role) ORDER BY u.id ASC")
    List<User> findAssignableByDepartmentAndRoleOrderByIdAsc(@Param("department") Department department, @Param("role") String role);
}