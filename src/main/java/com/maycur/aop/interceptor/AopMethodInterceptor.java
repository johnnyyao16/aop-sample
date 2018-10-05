package com.maycur.aop.interceptor;

import com.maycur.aop.invocation.MethodInvocation;

public interface AopMethodInterceptor {

    Object invoke(MethodInvocation mi) throws Throwable;
}
