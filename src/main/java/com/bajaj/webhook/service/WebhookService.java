package com.bajaj.webhook.service;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class WebhookService {

    private final RestTemplate restTemplate = new RestTemplate();

    public void executeFlow() {
        try {
            String generateUrl = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";

            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("name", "John Doe");
            requestBody.put("regNo", "REG12347");
            requestBody.put("email", "john@example.com");

            ResponseEntity<Map> response = restTemplate.postForEntity(generateUrl, requestBody, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                String webhookUrl = (String) response.getBody().get("webhook");
                String accessToken = (String) response.getBody().get("accessToken");

                System.out.println("Webhook URL: " + webhookUrl);
                System.out.println("Access Token: " + accessToken);

                String finalQuery =
                        "SELECT d.DEPARTMENT_NAME, " +
                        "AVG(TIMESTAMPDIFF(YEAR, e.DOB, CURDATE())) AS AVERAGE_AGE, " +
                        "GROUP_CONCAT(CONCAT(e.FIRST_NAME, ' ', e.LAST_NAME) " +
                        "ORDER BY e.EMP_ID SEPARATOR ', ' LIMIT 10) AS EMPLOYEE_LIST " +
                        "FROM DEPARTMENT d " +
                        "JOIN EMPLOYEE e ON d.DEPARTMENT_ID = e.DEPARTMENT " +
                        "JOIN PAYMENTS p ON e.EMP_ID = p.EMP_ID " +
                        "WHERE p.AMOUNT > 70000 " +
                        "GROUP BY d.DEPARTMENT_ID, d.DEPARTMENT_NAME " +
                        "ORDER BY d.DEPARTMENT_ID DESC;";

                Map<String, String> solutionBody = new HashMap<>();
                solutionBody.put("finalQuery", finalQuery);

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.setBearerAuth(accessToken);

                HttpEntity<Map<String, String>> entity = new HttpEntity<>(solutionBody, headers);

                ResponseEntity<String> submitResponse =
                        restTemplate.postForEntity(webhookUrl, entity, String.class);

                System.out.println("Submission status: " + submitResponse.getStatusCode());
                System.out.println("Response body: " + submitResponse.getBody());
            } else {
                System.out.println("Failed to generate webhook. Status: " + response.getStatusCode());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
