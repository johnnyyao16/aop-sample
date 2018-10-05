package com.maycur.aop.advisor;

import lombok.Data;
import lombok.ToString;

@ToString
@Data
public class TargetSource {
    private Class<?> targetClass;
    private Object targetObject;
}
