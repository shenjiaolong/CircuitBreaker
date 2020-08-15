package com.circuitbreaker.cbmode.fallback;

public abstract class FallBack {

    /**
     * 执行应急方案
     * @return
     */
    public abstract Object executeFallBack(Object ... args);

}
