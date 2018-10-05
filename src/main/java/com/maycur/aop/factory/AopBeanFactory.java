package com.maycur.aop.factory;

import com.maycur.aop.adapter.AfterRunningAdviceAdapter;
import com.maycur.aop.advisor.*;
import com.maycur.aop.bean.AopBeanDefinition;
import com.maycur.aop.core.CglibAopProxy;
import com.maycur.aop.interceptor.AopMethodInterceptor;
import com.maycur.aop.interceptor.BeforeMethodAdviceInterceptor;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class AopBeanFactory extends BeanFactory {
    private static final ConcurrentHashMap<String, AopBeanDefinition> aopBeanDefinitionMap = new ConcurrentHashMap<>();

    private static final ConcurrentHashMap<String, Object> aopBeanMap = new ConcurrentHashMap<>();

    @Override
    public Object getBean(String name) throws Exception {
        Object aopBean = aopBeanMap.get(name);

        if (aopBean != null) {
            return aopBean;
        }
        if (aopBeanDefinitionMap.containsKey(name)) {
            AopBeanDefinition aopBeanDefinition = aopBeanDefinitionMap.get(name);
            AdvisedSupport advisedSupport = getAdvisedSupport(aopBeanDefinition);
            aopBean = new CglibAopProxy(advisedSupport).getProxy();
            aopBeanMap.put(name, aopBean);

        } else {
            aopBean = super.getBean(name);

        }
        return aopBean;
    }

    protected void registerBean(String name, AopBeanDefinition aopBeanDefinition) {
        aopBeanDefinitionMap.put(name, aopBeanDefinition);
    }

    private AdvisedSupport getAdvisedSupport(AopBeanDefinition aopBeanDefinition) throws Exception {

        AdvisedSupport advisedSupport = new AdvisedSupport();
        List<String> interceptorNames = aopBeanDefinition.getInterceptorNames();
        if (interceptorNames != null && !interceptorNames.isEmpty()) {
            for (String interceptorName : interceptorNames) {

                Advice advice = (Advice) getBean(interceptorName);
                Advisor advisor = new Advisor();
                advisor.setAdvice(advice);

                if (advice instanceof BeforeMethodAdvice) {
                    AopMethodInterceptor interceptor = new BeforeMethodAdviceInterceptor((BeforeMethodAdvice) advice);
                        //BeforeMethodAdviceAdapter.getInstants().getInterceptor(advisor);
                    advisedSupport.addAopMethodInterceptor(interceptor);
                }

                if (advice instanceof AfterRunningAdvice) {
                    AopMethodInterceptor interceptor = AfterRunningAdviceAdapter.getInstants().getInterceptor(advisor);
                    advisedSupport.addAopMethodInterceptor(interceptor);
                }

            }
        }

        TargetSource targetSource = new TargetSource();
        Object object = getBean(aopBeanDefinition.getTarget());
        targetSource.setTargetClass(object.getClass());
        targetSource.setTargetObject(object);
        advisedSupport.setTargetSource(targetSource);
        return advisedSupport;

    }
}
