package com.maycur.aop.test;

import com.maycur.aop.advisor.AfterRunningAdvice;
import com.maycur.aop.util.ThreadLocalUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

public class EndTimeAfterMethod implements AfterRunningAdvice {
    private static final Logger logger = LoggerFactory.getLogger(EndTimeAfterMethod.class);

    @Override
    public Object after(Object returnVal, Method method, Object[] args, Object target) {
        long endTime = System.currentTimeMillis();
        long startTime = ThreadLocalUtil.get();
        ThreadLocalUtil.remove();
        logger.info("方法耗时：" + (endTime - startTime) + "ms");
        return returnVal;
    }
}
