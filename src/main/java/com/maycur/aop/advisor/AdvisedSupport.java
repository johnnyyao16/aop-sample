package com.maycur.aop.advisor;

import com.maycur.aop.interceptor.AopMethodInterceptor;
import lombok.Data;
import lombok.ToString;

import java.util.LinkedList;
import java.util.List;

@Data
@ToString
public class AdvisedSupport extends Advisor {
    //目标对象
    private TargetSource targetSource;
    //拦截器列表
    private List<AopMethodInterceptor> list = new LinkedList<>();

    public void addAopMethodInterceptor(AopMethodInterceptor interceptor) {
        list.add(interceptor);
    }

}
