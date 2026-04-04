package com.project.grievance.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.project.grievance.model.User;

public interface UserRepository extends JpaRepository<User, Long> {

    @Query(value = "SELECT * FROM users WHERE email = :email LIMIT 1", nativeQuery = true)
    Optional<User> findByEmail(@Param("email") String email);

    List<User> findByRoleIgnoreCase(String role);

    Optional<User> findFirstByRoleIgnoreCaseOrderByIdAsc(String role);
}