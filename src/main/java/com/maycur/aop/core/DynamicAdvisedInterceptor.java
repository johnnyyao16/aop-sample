package com.maycur.aop.core;

import com.maycur.aop.advisor.TargetSource;
import com.maycur.aop.interceptor.AopMethodInterceptor;
import com.maycur.aop.invocation.CglibMethodInvocation;
import com.maycur.aop.invocation.MethodInvocation;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;
import java.util.List;

public class DynamicAdvisedInterceptor implements MethodInterceptor {
    protected final List<AopMethodInterceptor> interceptorList;
    protected final TargetSource targetSource;

    public DynamicAdvisedInterceptor(List<AopMethodInterceptor> interceptorList, TargetSource targetSource) {
        this.interceptorList = interceptorList;
        this.targetSource = targetSource;
    }

    @Override
    public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
        MethodInvocation invocation =
            new CglibMethodInvocation(obj, targetSource.getTargetObject(), method, args, interceptorList, proxy);
        return invocation.proceed();
    }
}
