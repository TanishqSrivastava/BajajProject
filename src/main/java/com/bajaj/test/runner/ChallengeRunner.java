package com.bajaj.test.runner;

import com.bajaj.test.dto.WebhookRequest;
import com.bajaj.test.dto.WebhookResponse;
import com.bajaj.test.service.ChallengeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;


@Component
public class ChallengeRunner implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(ChallengeRunner.class);

    private final ChallengeService challengeService;


    @Value("${app.user.name}")
    private String name;

    @Value("${app.user.regNo}")
    private String regNo;

    @Value("${app.user.email}")
    private String email;


    public ChallengeRunner(ChallengeService challengeService) {
        this.challengeService = challengeService;
    }

    @Override
    public void run(String... args) {
        logger.info("==================================================");
        logger.info("   Starting Bajaj Finserv Health Qualifier Task   ");
        logger.info("==================================================");

        try {

            WebhookRequest request = new WebhookRequest(name, regNo, email);


            challengeService.processChallenge(request);

        } catch (Exception e) {
            logger.error("A critical unexpected error occurred during the process.", e);
        } finally {
            logger.info("==================================================");
            logger.info("          Task Execution Finished.              ");
            logger.info("==================================================");
        }
    }
}
