package com.circuitbreaker.cbmode.strategy;

import com.circuitbreaker.cbmode.fallback.FallBack;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 连续失败 熔断策略
 */
public class FailContinuousCbStrategy extends CbStrategy {

    /** 为了方便演示 dalayTime 单位为妙 */
    public FailContinuousCbStrategy(int failContinuousCiruitThreshold,int attemptRenewTimesThreshold, int dalayTime, FallBack fallBack) {
        super(dalayTime,attemptRenewTimesThreshold,fallBack);
        this.failContinuousCiruitThreshold = failContinuousCiruitThreshold;
    }

    /**
     * 连续失败熔断阈值  如 3 代表连续失败3次数 则触发熔断
     */
    private int  failContinuousCiruitThreshold;

    /**
     * 当前连续失败次数
     */
    private volatile int failContinuousCount = 0;

    /**
     * 成功或失败计数  且到达熔断器状态转换的条件时返回 true
     * @param isFail
     * @return
     */
    public synchronized  boolean countAndTriggerCircuit( boolean isFail){

        if(isFail){
            //本次失败 则自增
            ++ failContinuousCount;
        }else{
            //本次成功 则清零
            failContinuousCount = 0;
        }

        if(failContinuousCount ==  failContinuousCiruitThreshold ){
            System.out.println("-------------连续三次失败，触发熔断！！");
            return true;
        }
        return false;
    }
}
