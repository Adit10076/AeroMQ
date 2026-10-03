package com.aeromq.broker.sample.listener;

import com.aeromq.broker.core.AeroListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class OrderListeners {

    private static final Logger log = LoggerFactory.getLogger(OrderListeners.class);

    @AeroListener(topic = "ORDERS")
    public void processOrder(String orderId) throws InterruptedException {
        long workMs = orderId.startsWith("blocker-") ? 10_000 : 1_500;
        log.info("[ORDERS][Processor] Started order {} (priority work {}ms)", orderId, workMs);
        Thread.sleep(workMs);
        log.info("[ORDERS][Processor] Completed order {}", orderId);
    }

    @AeroListener(topic = "ORDERS")
    public void reserveInventory(String orderId) {
        log.info("[ORDERS][Inventory] Reserved stock for: {}", orderId);
    }

    @AeroListener(topic = "ORDERS")
    public void createShippingLabel(String orderId) {
        log.info("[ORDERS][Shipping] Created shipping label for: {}", orderId);
    }
}
