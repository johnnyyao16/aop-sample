package com.maycur.aop.core;


public interface AopProxy {
    Object getProxy();

    Object getProxy(ClassLoader classLoader);
}
