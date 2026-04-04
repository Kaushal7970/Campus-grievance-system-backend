package com.project.grievance.service;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.grievance.model.AiAdminRules;
import com.project.grievance.repository.AiAdminRulesRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AiAdminRulesService {

    private final AiAdminRulesRepository repo;

    /**
     * Returns the latest configured rules (single-record model). If none exist, returns empty.
     */
    @Transactional(readOnly = true)
    public String getRulesText() {
        return repo.findTopByOrderByIdDesc()
            .map(AiAdminRules::getRules)
            .orElse("");
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getRulesView() {
        return repo.findTopByOrderByIdDesc()
            .<Map<String, Object>>map(r -> Map.of(
                "rules", r.getRules() == null ? "" : r.getRules(),
                "updatedAt", r.getUpdatedAt(),
                "updatedByEmail", r.getUpdatedByEmail() == null ? "" : r.getUpdatedByEmail()
            ))
                .orElse(Map.of(
                        "rules", "",
                        "updatedAt", null,
                        "updatedByEmail", ""
                ));
    }

    @Transactional
    public Map<String, Object> updateRules(String rules, String updatedByEmail) {
        String normalized = rules == null ? "" : rules.trim();
        if (normalized.length() > 10000) {
            throw new IllegalArgumentException("Rules too long (max 10000 characters)");
        }

        AiAdminRules entity = repo.findTopByOrderByIdDesc().orElseGet(AiAdminRules::new);
        entity.setRules(normalized);
        entity.setUpdatedAt(LocalDateTime.now());
        entity.setUpdatedByEmail(updatedByEmail == null ? "" : updatedByEmail);
        AiAdminRules saved = repo.save(entity);

        return Map.of(
                "rules", saved.getRules() == null ? "" : saved.getRules(),
                "updatedAt", saved.getUpdatedAt(),
                "updatedByEmail", saved.getUpdatedByEmail() == null ? "" : saved.getUpdatedByEmail()
        );
    }
}
