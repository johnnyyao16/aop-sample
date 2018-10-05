package com.maycur.aop.adapter;

import com.maycur.aop.advisor.Advisor;
import com.maycur.aop.advisor.AfterRunningAdvice;
import com.maycur.aop.interceptor.AfterRunningAdviceInterceptor;
import com.maycur.aop.interceptor.AopMethodInterceptor;

public class AfterRunningAdviceAdapter implements AdviceAdapter {

    private AfterRunningAdviceAdapter() {
    }

    private static final AfterRunningAdviceAdapter INSTANTS = new AfterRunningAdviceAdapter();

    public static AfterRunningAdviceAdapter getInstants() {
        return INSTANTS;
    }

    @Override
    public AopMethodInterceptor getInterceptor(Advisor advisor) {
        AfterRunningAdvice advice = (AfterRunningAdvice) advisor.getAdvice();
        return new AfterRunningAdviceInterceptor(advice);
    }
}
