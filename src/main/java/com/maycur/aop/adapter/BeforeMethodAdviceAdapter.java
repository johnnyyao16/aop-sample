package com.maycur.aop.adapter;

import com.maycur.aop.advisor.Advisor;
import com.maycur.aop.advisor.BeforeMethodAdvice;
import com.maycur.aop.interceptor.AopMethodInterceptor;
import com.maycur.aop.interceptor.BeforeMethodAdviceInterceptor;

public class BeforeMethodAdviceAdapter implements AdviceAdapter {

    private BeforeMethodAdviceAdapter() {
    }

    private static final BeforeMethodAdviceAdapter INSTANTS = new BeforeMethodAdviceAdapter();

    public static BeforeMethodAdviceAdapter getInstants() {
        return INSTANTS;
    }

    @Override
    public AopMethodInterceptor getInterceptor(Advisor advisor) {
        BeforeMethodAdvice advice = (BeforeMethodAdvice) advisor.getAdvice();
        return new BeforeMethodAdviceInterceptor(advice);
    }
}
