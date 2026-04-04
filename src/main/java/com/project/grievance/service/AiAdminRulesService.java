package com.project.grievance.service;

import java.util.LinkedHashMap;
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
        // NOTE: Map.of(...) does not allow null values.
        // updatedAt may legitimately be null when rules are not configured yet.
        return repo.findTopByOrderByIdDesc()
                .<Map<String, Object>>map(r -> {
                    Map<String, Object> view = new LinkedHashMap<>();
                    view.put("rules", r.getRules() == null ? "" : r.getRules());
                    view.put("updatedAt", r.getUpdatedAt());
                    view.put("updatedByEmail", r.getUpdatedByEmail() == null ? "" : r.getUpdatedByEmail());
                    return view;
                })
                .orElseGet(() -> {
                    Map<String, Object> view = new LinkedHashMap<>();
                    view.put("rules", "");
                    view.put("updatedAt", null);
                    view.put("updatedByEmail", "");
                    return view;
                });
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

        Map<String, Object> view = new LinkedHashMap<>();
        view.put("rules", saved.getRules() == null ? "" : saved.getRules());
        view.put("updatedAt", saved.getUpdatedAt());
        view.put("updatedByEmail", saved.getUpdatedByEmail() == null ? "" : saved.getUpdatedByEmail());
        return view;
    }
}
