package com.maycur.aop.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestService {
    private static final Logger logger = LoggerFactory.getLogger(TestService.class);

    public void testMethod() throws InterruptedException {
        logger.info("this is a test method");
        Thread.sleep(1000);
    }
}
