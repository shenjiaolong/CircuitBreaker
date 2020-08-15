package com.circuitbreaker.cbmode;

import com.circuitbreaker.cbmode.status.CbState;

import java.util.concurrent.Callable;

public class CircuitBreakerRun {

    public static Object run(CircuitBreaker circuitBreaker, Callable callable){

        Object result = null;
        try {
            //先获取到当前熔断器的状态
            switch ( circuitBreaker.getCurrentState()){
                //熔断器关闭状态
                case CbState.CLOSE: {
                    result = callable.call();
                    //能执行到此处 肯定执行成功
                    circuitBreaker.successOrFailCount(false);
                    break;
                }
                //熔断器打开状态
                case CbState.OPEN:{
                    result = circuitBreaker.executeFallback(null);
                    break;
                }
                //熔断器尝试恢复状态  限流尝试恢复
                case CbState.ATTEMPT_CLOSE: {
                    //获取到令牌的 正常调用
                    if (circuitBreaker.acquireToken()) {
                        result = callable.call();
                        //能执行到此处 肯定执行成功
                        circuitBreaker.successOrFailCount(false);
                    } else {
                        System.out.print("获取不到通过令牌了  ");
                        result = circuitBreaker.executeFallback(null);
                    }
                    break;
                }
            }
        }catch (Exception e){
            circuitBreaker.successOrFailCount(true);
            //失败 则降级 应急计划执行Fallback
            result = circuitBreaker.executeFallback(null);
        }

        return result;
    }

}
