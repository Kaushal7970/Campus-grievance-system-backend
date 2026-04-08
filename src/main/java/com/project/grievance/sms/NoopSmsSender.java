package com.project.grievance.sms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NoopSmsSender implements SmsSender {

    private static final Logger log = LoggerFactory.getLogger(NoopSmsSender.class);

    @Override
    public void send(String toE164, String message) {
        // Intentionally do nothing (demo/dev/testing).
        log.debug("SMS disabled/noop. to={} message={}", toE164, message);
    }
}
