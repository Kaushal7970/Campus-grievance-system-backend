package com.project.grievance.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

import com.project.grievance.model.AiAdminRules;

public interface AiAdminRulesRepository extends JpaRepository<AiAdminRules, Long> {

	Optional<AiAdminRules> findTopByOrderByIdDesc();
}
