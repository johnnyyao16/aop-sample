package com.maycur.aop.interceptor;

import com.maycur.aop.advisor.BeforeMethodAdvice;
import com.maycur.aop.invocation.MethodInvocation;

public class BeforeMethodAdviceInterceptor implements AopMethodInterceptor {

    private BeforeMethodAdvice advice;

    public BeforeMethodAdviceInterceptor(BeforeMethodAdvice advice) {
        this.advice = advice;
    }

    @Override
    public Object invoke(MethodInvocation mi) throws Throwable {
        advice.before(mi.getMethod(), mi.getArguments(), mi);
        return mi.proceed();
    }
}
