package com.bajaj.test.service;

import com.bajaj.test.dto.SolutionRequest;
import com.bajaj.test.dto.WebhookRequest;
import com.bajaj.test.dto.WebhookResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;


@Service
public class ChallengeService {

    private static final Logger logger = LoggerFactory.getLogger(ChallengeService.class);
    private final RestTemplate restTemplate;


    @Value("${api.base.url}")
    private String apiBaseUrl;

    @Value("${api.generate.webhook.path}")
    private String apiWebhookPath;

    public ChallengeService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }



    public void processChallenge(WebhookRequest request) {

        WebhookResponse webhookResponse = generateWebhook(request);

        if (webhookResponse != null && webhookResponse.getWebhook() != null && webhookResponse.getAccessToken() != null) {
            logger.info("Step 1 SUCCESS: Webhook and Token received.");
            logger.info("Webhook URL: {}", webhookResponse.getWebhook());


            String finalQuery = getFinalSqlQueryForQuestion1();
            logger.info("Step 2 SUCCESS: SQL Query formulated.");
            logger.info("Final SQL Query: {}", finalQuery);


            submitSolution(webhookResponse.getWebhook(), webhookResponse.getAccessToken(), finalQuery);
        } else {
            logger.error("Step 1 FAILED: Did not receive a valid webhook or access token. Aborting process.");
        }
    }

    private WebhookResponse generateWebhook(WebhookRequest requestBody) {
        logger.info("Executing Step 1: Generating Webhook...");
        String url = apiBaseUrl + apiWebhookPath;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<WebhookRequest> requestEntity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<WebhookResponse> response = restTemplate.postForEntity(
                    url,
                    requestEntity,
                    WebhookResponse.class
            );
            return response.getBody();
        } catch (HttpClientErrorException e) {
            logger.error("HTTP Error during webhook generation: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            return null;
        }
    }

    private String getFinalSqlQueryForQuestion1() {

        return "SELECT p.AMOUNT AS SALARY, CONCAT(e.FIRST_NAME, ' ', e.LAST_NAME) AS NAME, TIMESTAMPDIFF(YEAR, e.DOB, CURDATE()) AS AGE, d.DEPARTMENT_NAME FROM PAYMENTS p JOIN EMPLOYEE e ON p.EMP_ID = e.EMP_ID JOIN DEPARTMENT d ON e.DEPARTMENT = d.DEPARTMENT_ID WHERE DAY(p.PAYMENT_TIME) <> 1 ORDER BY p.AMOUNT DESC LIMIT 1";
    }

    private void submitSolution(String webhookUrl, String accessToken, String finalQuery) {
        logger.info("Executing Step 3: Submitting solution...");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);


        headers.set("Authorization", accessToken);

        SolutionRequest requestBody = new SolutionRequest(finalQuery);
        HttpEntity<SolutionRequest> requestEntity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                    webhookUrl,
                    requestEntity,
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                logger.info("Step 3 SUCCESS: Solution submitted successfully!");
                logger.info("Response: {}", response.getBody());
            } else {
                logger.error("Step 3 FAILED: Failed to submit solution. Status code: {}", response.getStatusCode());
                logger.error("Response Body: {}", response.getBody());
            }
        } catch (HttpClientErrorException e) {
            logger.error("HTTP Error during solution submission: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
        }
    }
}
