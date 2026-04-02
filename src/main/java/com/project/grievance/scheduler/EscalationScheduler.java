package com.project.grievance.scheduler;

import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.project.grievance.model.Grievance;
import com.project.grievance.repository.GrievanceRepository;
import com.project.grievance.service.GrievanceService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class EscalationScheduler {

    private final GrievanceRepository repo;
    private final GrievanceService grievanceService;

    @Scheduled(fixedRate = 3600000) // every 1 hour
    public void run() {
        List<Grievance> active = repo.findActiveExcludingStatuses(List.of("RESOLVED", "CLOSED"));
        for (Grievance g : active) {
            grievanceService.autoEscalateIfNeeded(g);
        }
    }
}