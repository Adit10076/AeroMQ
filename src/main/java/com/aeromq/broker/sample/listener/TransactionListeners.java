package com.aeromq.broker.sample.listener;

import com.aeromq.broker.core.AeroListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class TransactionListeners {

    private static final Logger log = LoggerFactory.getLogger(TransactionListeners.class);

    @AeroListener(topic = "TRANSACTIONS")
    public void processLedger(String data) throws InterruptedException {
        System.out.println("[TRANSACTIONS][Ledger] Started: " + data);
        Thread.sleep(2000);
        System.out.println("[TRANSACTIONS][Ledger] Completed: " + data);
    }

    @AeroListener(topic = "TRANSACTIONS")
    public void auditTransaction(String data) {
        log.info("[TRANSACTIONS][Audit] Recorded audit trail for: {}", data);
    }

    @AeroListener(topic = "TRANSACTIONS")
    public void sendReceipt(String data) {
        log.info("[TRANSACTIONS][Receipt] Emailed receipt for: {}", data);
    }
}
