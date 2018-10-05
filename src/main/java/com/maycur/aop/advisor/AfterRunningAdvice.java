package com.maycur.aop.advisor;

import java.lang.reflect.Method;

public interface AfterRunningAdvice extends Advice {
    Object after(Object returnVal, Method method, Object[] args, Object target);
}
