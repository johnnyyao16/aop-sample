package com.maycur.aop.adapter;

import com.maycur.aop.advisor.Advisor;
import com.maycur.aop.interceptor.AopMethodInterceptor;

public interface AdviceAdapter {
    AopMethodInterceptor getInterceptor(Advisor advisor);

}
