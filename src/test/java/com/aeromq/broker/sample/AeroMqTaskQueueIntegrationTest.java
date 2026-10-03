package com.aeromq.broker.sample;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AeroMqTaskQueueIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void healthCheckReturnsOk() {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/aeromq/health-check", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("OK");
    }

    @Test
    void sendDispatchesTransactionEvent() {
        ResponseEntity<Map> response = restTemplate.getForEntity(
                "/api/aeromq/send?message=integration-test&priority=2", Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsEntry("topic", "TRANSACTIONS");
        assertThat(response.getBody()).containsEntry("status", "dispatched");
    }

    @Test
    void priorityDemoDispatchesMultipleOrders() {
        ResponseEntity<Map> response = restTemplate.getForEntity("/api/sample/demo/priority", Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsEntry("scenario", "priority");
        assertThat(response.getBody().get("jobs")).isInstanceOf(List.class);
    }

    @Test
    void dashboardStatsReturnsMetrics() {
        restTemplate.getForEntity("/api/aeromq/send?message=stats-test&priority=3", Map.class);

        ResponseEntity<Map> response = restTemplate.getForEntity("/api/aeromq/dashboard/stats", Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsKeys(
                "eventsInQueue", "activeThreads", "poolSize", "totalCompleted", "dlqCount");
    }

    @Test
    void dlqDemoEventuallyPersistsFailedEvent() throws InterruptedException {
        restTemplate.getForEntity("/api/sample/demo/dlq", Map.class);

        Thread.sleep(8000);

        ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                "/api/aeromq/dashboard/dlq-recent",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {});

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotEmpty();
        assertThat(response.getBody().get(0)).containsKey("errorMessage");
    }
}
