package com.maycur.aop.core;

import com.maycur.aop.bean.AopBeanDefinition;
import com.maycur.aop.bean.BeanDefinition;
import com.maycur.aop.bean.ProxyFactoryBean;
import com.maycur.aop.factory.AopBeanFactory;
import com.maycur.aop.util.ClassUtil;
import com.maycur.aop.util.YamlUtil;

import java.io.InputStream;
import java.util.List;

public class ApplicationContext extends AopBeanFactory {
    private String fileName;

    public ApplicationContext(String fileName) {
        this.fileName = fileName;
    }

    public void init() {
        loadFile();
    }

    private void loadFile() {
        InputStream is = getClass().getClassLoader().getResourceAsStream(fileName);
        List<AopBeanDefinition> beanDefinitions = YamlUtil.loadYaml(is);
        //setBeanDefineMap((List<BeanDefinition>)beanDefinitions);
        if (beanDefinitions != null && !beanDefinitions.isEmpty()) {
            for (AopBeanDefinition beanDefinition : beanDefinitions) {
                Class<?> clz = ClassUtil.loadClass(beanDefinition.getClassName());
                if (clz == ProxyFactoryBean.class) {
                    registerBean(beanDefinition.getBeanName(), beanDefinition);
                } else {
                    registerBean(beanDefinition.getBeanName(), (BeanDefinition) beanDefinition);
                }
                //                try {
                //                    getBean(beanDefinition.getBeanName());
                //                } catch (Exception e) {
                //                    e.printStackTrace();
                //                }
            }
        }
    }
}
