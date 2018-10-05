package com.maycur.aop.test;

import com.maycur.aop.advisor.BeforeMethodAdvice;
import com.maycur.aop.util.ThreadLocalUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

public class StartTimeBeforeMethod implements BeforeMethodAdvice {
    private static final Logger logger = LoggerFactory.getLogger(StartTimeBeforeMethod.class);

    @Override
    public void before(Method method, Object[] args, Object target) {
        long startTime = System.currentTimeMillis();
        logger.info("开始计时");
        ThreadLocalUtil.set(startTime);
    }
}