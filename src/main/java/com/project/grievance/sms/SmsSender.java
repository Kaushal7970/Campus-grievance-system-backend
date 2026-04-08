package com.project.grievance.sms;

public interface SmsSender {
    void send(String toE164, String message);
}
