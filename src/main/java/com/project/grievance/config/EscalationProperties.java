package com.project.grievance.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.escalation")
public class EscalationProperties {

    private boolean enabled = true;

    private long facultyDays = 2;
    private long hodDays = 4;
    private long principalDays = 7;
    private long adminDays = 10;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public long getFacultyDays() {
        return facultyDays;
    }

    public void setFacultyDays(long facultyDays) {
        this.facultyDays = facultyDays;
    }

    public long getHodDays() {
        return hodDays;
    }

    public void setHodDays(long hodDays) {
        this.hodDays = hodDays;
    }

    public long getPrincipalDays() {
        return principalDays;
    }

    public void setPrincipalDays(long principalDays) {
        this.principalDays = principalDays;
    }

    public long getAdminDays() {
        return adminDays;
    }

    public void setAdminDays(long adminDays) {
        this.adminDays = adminDays;
    }
}
