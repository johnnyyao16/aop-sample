package com.maycur.aop.factory;

import com.google.common.collect.Maps;
import com.maycur.aop.bean.BeanDefinition;
import com.maycur.aop.util.BeanUtil;
import com.maycur.aop.util.ClassUtil;

import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

public class BeanFactory {
    // 已加载的bean的map，如果map里有则不需要重新加载
    private static final ConcurrentMap<String, Object> beanMap = Maps.newConcurrentMap();
    // bean定义的map
    private static final ConcurrentMap<String, BeanDefinition> beanDefineMap = Maps.newConcurrentMap();

    public Object getBean(String name) throws Exception {
        //查找对象是否已经实例化过
        Object bean = beanMap.get(name);
        if (bean != null) {
            return bean;
        }
        //如果没有实例化，那就需要调用createBean来创建对象
        bean = createBean(beanDefineMap.get(name));
        if (bean != null) {
            //再把对象存入Map中方便下次使用。
            beanMap.put(name, bean);
            //对象创建成功以后，注入对象需要的参数
           // populateBean(bean);
        }
        return bean;
    }

    private Object createBean(BeanDefinition beanDefinition) throws Exception {
        String beanName = beanDefinition.getClassName();
        Class clz = ClassUtil.loadClass(beanName);
        if (clz == null) {
            throw new Exception("Can not find bean class by bean name");
        }
        return BeanUtil.constructBean(clz, null);
    }

    private void populateBean(Object bean) throws Exception {

        Field[] fields = bean.getClass().getDeclaredFields();
        if (fields != null && fields.length > 0) {
            for (Field field : fields) {
                Object fieldBean = getBean(field.getName());
                if (fieldBean != null) {
                    BeanUtil.injectField(field, bean, fieldBean);
                }
            }
        }
    }

    protected void setBeanDefineMap(List<BeanDefinition> beanDefinitionList) {
        beanDefinitionList.stream().forEach(def -> beanDefineMap.putIfAbsent(def.getBeanName(), def));
    }

    protected void registerBean(String name, BeanDefinition beanDefinition) {
        beanDefineMap.put(name, beanDefinition);
    }
}
