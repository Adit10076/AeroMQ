package com.aeromq.broker.controller;

import com.aeromq.broker.service.AeroMqService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/aeromq")
public class AeroMqController {

    private final AeroMqService aeroMqService;

    public AeroMqController(AeroMqService aeroMqService) {
        this.aeroMqService = aeroMqService;
    }

    @GetMapping("/send")
    public ResponseEntity<Map<String, Object>> send(
            @RequestParam String message,
            @RequestParam int priority) {
        return ResponseEntity.ok(aeroMqService.send(message, priority));
    }

    @GetMapping("/send-fail")
    public ResponseEntity<Map<String, Object>> sendFail(
            @RequestParam String message,
            @RequestParam(defaultValue = "3") int priority) {
        return ResponseEntity.ok(aeroMqService.sendFail(message, priority));
    }

    @GetMapping("/publish")
    public ResponseEntity<Map<String, Object>> publish(
            @RequestParam String topic,
            @RequestParam String message,
            @RequestParam(defaultValue = "3") int priority) {
        return ResponseEntity.ok(aeroMqService.publish(topic, message, priority));
    }

    @PostMapping("/replay/{id}")
    public ResponseEntity<Map<String, Object>> replay(@PathVariable Long id) {
        return ResponseEntity.ok(aeroMqService.replay(id));
    }

    @GetMapping("/health-check")
    public String healthCheck() {
        return "OK";
    }
}
