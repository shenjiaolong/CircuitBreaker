package com.circuitbreaker.cbmode.strategy;

import com.circuitbreaker.cbmode.fallback.FallBack;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 总数中失败几次 熔断策略
 */
public class FailSumCbStrategy extends CbStrategy {

    public FailSumCbStrategy(int[] ciruitThreshold,int attemptRenewTimesThreshold, int dalayTime, FallBack fallBack) {
        super(dalayTime,attemptRenewTimesThreshold,fallBack);
        if(ciruitThreshold.length == 2 && ciruitThreshold[0]<ciruitThreshold[1]){
            this.ciruitThreshold = ciruitThreshold;
        }else{
            this.ciruitThreshold =new int[]{10,50};
        }
    }

    /**
     * 熔断阈值 {a,b} 一个二元组  a代表失败次数  b代表最近执行从次数
     */
    private int [] ciruitThreshold;

    /** 当前失败总数 */
    private volatile int failCount = 0;

    /** 当前请求总数 */
    private volatile int  cunrrentSum = 0;

    /**
     * 成功或失败计数  且到达熔断器状态转换的条件时返回 true
     * @param isFail
     * @return
     */
    public synchronized  boolean countAndTriggerCircuit( boolean isFail){

        //请求总数  失败次数  自增
        if(isFail){
            failCount++;
        }
        cunrrentSum++;

        //达到失败阈值 触发熔断 清零返回true
        if(failCount ==  ciruitThreshold[0] && cunrrentSum <= ciruitThreshold[1]){
            cunrrentSum =0;
            failCount=0;
            System.out.println("当前(失败次数，总调用次数) = （"+failCount  +" , "+cunrrentSum+")" );
            return true;
        }
        //当总请求量达到阈值 b  失败数量<阈值a  不触发熔断  则清零重置
        if( cunrrentSum == ciruitThreshold[1]){
            cunrrentSum=0;
            failCount=0;
        }
        System.out.println("当前(失败次数，总调用次数) = （"+failCount  +" , "+cunrrentSum+")" );
        return false;
    }
}
