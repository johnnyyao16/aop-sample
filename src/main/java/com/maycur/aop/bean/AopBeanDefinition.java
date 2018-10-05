package com.maycur.aop.bean;

import lombok.Data;
import lombok.ToString;

import java.util.List;
@Data
@ToString
public class AopBeanDefinition extends BeanDefinition {
    private String target;
    private List<String> interceptorNames;
}
