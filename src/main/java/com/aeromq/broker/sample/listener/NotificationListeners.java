package com.aeromq.broker.sample.listener;

import com.aeromq.broker.core.AeroListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class NotificationListeners {

    private static final Logger log = LoggerFactory.getLogger(NotificationListeners.class);

    @AeroListener(topic = "NOTIFICATIONS")
    public void sendNotification(String notification) {
        log.info("[NOTIFICATIONS][Dispatcher] Sending: {}", notification);
        try {
            if (notification.equals("blocker-1")) {
                throw new RuntimeException("Blocker notification error");
            }
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("[NOTIFICATIONS][Dispatcher] Interrupted: {}", notification, e);
        }
        log.info("[NOTIFICATIONS][Dispatcher] Sent: {}", notification);
    }

    @AeroListener(topic = "NOTIFICATIONS")
    public void sendEmail(String notification) {
        log.info("[NOTIFICATIONS][Email] Queued email for: {}", notification);
    }

    @AeroListener(topic = "NOTIFICATIONS")
    public void sendPush(String notification) {
        log.info("[NOTIFICATIONS][Push] Sent push notification for: {}", notification);
    }
}
