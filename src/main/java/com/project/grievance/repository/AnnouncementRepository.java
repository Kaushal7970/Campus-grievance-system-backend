package com.project.grievance.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.grievance.enums.Role;
import com.project.grievance.model.Announcement;

public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {

    List<Announcement> findAllByOrderByCreatedAtDesc();

    List<Announcement> findByAudienceRoleIsNullOrAudienceRoleOrderByCreatedAtDesc(Role audienceRole);
}
