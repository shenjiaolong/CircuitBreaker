package com.circuitbreaker.cbmode.strategy;

import com.circuitbreaker.cbmode.fallback.FallBack;

import java.util.concurrent.atomic.AtomicInteger;


public abstract class CbStrategy {

    /**
     * 熔断机制持续时间
     */
    private int dalayTime;

    /**
     * 当进入熔断打开状态时 执行的默认逻辑实例
     */
    private FallBack fallBack;

    /**
     * 尝试恢复成功计数
     */
    private volatile int attemptRenewSuccessCount = 0;

    /**
     * 当熔断器状态处于 尝试关闭状态时  attemptRenewTimes代表限流尝试恢复正常的请求次数
     * 如果连续尝试 n 次都成功，则熔断器状态转换到 关闭状态CLOSE。
     */
    private int attemptRenewTimesThreshold;

    public CbStrategy( int dalayTime,int attemptRenewTimes,FallBack fallBack ){
        this.dalayTime=dalayTime;
        this.fallBack=fallBack;
        this.attemptRenewTimesThreshold =attemptRenewTimes;
    }

    public int getDalayTime() {
        return dalayTime;
    }

    public int getAttemptRenewTimesThreshold() {
        return attemptRenewTimesThreshold;
    }

    /**
     * 执行应急方案 Fallback
     * @param args
     * @return
     */
    public Object executeFallback(Object...args){
        return  fallBack.executeFallBack(args);
    }

    /**
     * 成功或失败计数  且到达熔断器状态转换的条件时返回 true
     * @param isFail
     * @return
     */
    public abstract boolean countAndTriggerCircuit( boolean isFail);


    /**
     * 尝试恢复成功次数计数 并返回
     * @return
     */
    public synchronized int attemptRenewSuccessCount( boolean isFail) {
        if(!isFail){
           return ++attemptRenewSuccessCount;
        }
        return attemptRenewSuccessCount;
    }

    //attemptRenewSuccessCount 清零重置
    public void attemptRenewSuccessCountReset(){
        attemptRenewSuccessCount = 0;
    }
}
