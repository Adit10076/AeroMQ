package com.aeromq.broker.sample;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/sample")
public class SampleTaskQueueController {

    private final SampleTaskQueueService sampleTaskQueueService;

    public SampleTaskQueueController(SampleTaskQueueService sampleTaskQueueService) {
        this.sampleTaskQueueService = sampleTaskQueueService;
    }

    @GetMapping("/demo/priority")
    public ResponseEntity<Map<String, Object>> priorityDemo() {
        return ResponseEntity.ok(sampleTaskQueueService.runPriorityDemo());
    }

    @GetMapping("/demo/burst")
    public ResponseEntity<Map<String, Object>> burstDemo(
            @RequestParam(defaultValue = "10") int count) {
        return ResponseEntity.ok(sampleTaskQueueService.runBurstDemo(count));
    }

    @GetMapping("/demo/dlq")
    public ResponseEntity<Map<String, Object>> dlqDemo() {
        return ResponseEntity.ok(sampleTaskQueueService.runDlqDemo());
    }

    @GetMapping("/demo/full")
    public ResponseEntity<Map<String, Object>> fullDemo() {
        return ResponseEntity.ok(sampleTaskQueueService.runFullDemo());
    }
}
