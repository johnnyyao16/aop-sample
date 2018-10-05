package com.maycur.aop.bean;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class BeanDefinition {
    private String beanName;
    private String className;
}
