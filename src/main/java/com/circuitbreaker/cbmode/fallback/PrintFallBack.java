package com.circuitbreaker.cbmode.fallback;

import org.springframework.context.annotation.Bean;

/**
 * 简单打印应急方案
 */
public  class PrintFallBack extends FallBack {

    /**
     * 执行应急方案
     * @return
     */
    public  Object executeFallBack(Object ... args){
        System.out.println("执行的应急方案 FallBack 执行的应急方案 ！！！");
        return null;
    }

}
