package com.maycur.aop.advisor;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class Advisor {
    private Advice advice;
    private Pointcut pointcut;
}
