package com.maycur.aop.test;

import com.maycur.aop.core.ApplicationContext;

public class MainTest {
    public static void main(String[] args) throws Exception {
        ApplicationContext applicationContext = new ApplicationContext("applicationBean.yaml");
        applicationContext.init();
        TestService testService = (TestService) applicationContext.getBean("testServiceProxy");
        testService.testMethod();
    }
}
