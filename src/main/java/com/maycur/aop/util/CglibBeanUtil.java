package com.maycur.aop.util;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.NoOp;

import java.lang.reflect.Constructor;

public class CglibBeanUtil {

    public static <T> T instanceByCglib(Class<T> clz,Constructor ctr,Object[] args) {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(clz);
        enhancer.setCallback(NoOp.INSTANCE);

        if(ctr == null){
            return (T) enhancer.create();
        }else {
            return (T) enhancer.create(ctr.getParameterTypes(),args);
        }
    }

}
