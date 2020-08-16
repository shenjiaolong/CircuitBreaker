package com.circuitbreaker.cbmode;

import com.circuitbreaker.cbmode.status.CbState;
import com.circuitbreaker.cbmode.strategy.CbStrategy;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.concurrent.Semaphore;

@Service
public class CircuitBreaker {

    /** 熔断策略*/
    private CbStrategy strategy;

    /** 熔断器的状态对象*/
    public CbState state;

    /** 限流器 */
    private Semaphore semaphore;

    public CircuitBreaker(){
    }


    public CircuitBreaker(CbStrategy strategy) {
        this.strategy = strategy;
        this.state = new CbState();
        this.semaphore = new Semaphore(strategy.getAttemptRenewTimesThreshold());
    }

    public void setStrategy(CbStrategy strategy) {
        this.strategy = strategy;
        this.state = new CbState();
        this.semaphore = new Semaphore(strategy.getAttemptRenewTimesThreshold());
    }

    /**
     * 尝试获取可通过令牌
     */
    public boolean acquireToken(){
       return semaphore.tryAcquire();
    }

    /**
     * 释放可通过令牌
     */
    public void releaseToken(){
         semaphore.release(strategy.getAttemptRenewTimesThreshold());
    }


    /**
     * 成功或失败计数 也由此触发状态转换
     * @return
     */
    public void successOrFailCount( boolean isFail){

        //如果在熔断器关闭状态下 做计数 可能触发熔断器状态变更为OPEN
        if(state.getCurrentState()== CbState.CLOSE && strategy.countAndTriggerCircuit(isFail)){
            state.changeState(CbState.OPEN);
        }

        //如果在熔断器尝试关闭 即ATTEMPT_CLOSE状态下  做尝试恢复计数 可能触发熔断器状态变更为OPEN或CLOSE状态
        if(state.getCurrentState()== CbState.ATTEMPT_CLOSE ){
            int successCount = strategy.attemptRenewSuccessCount(isFail);
            //当限流令牌用完了 则说明尝试恢复正常调用次数已经执行完  看成功总次数有没有达到阈值
            if(semaphore.availablePermits() == 0 && successCount >= strategy.getAttemptRenewTimesThreshold() ){
                if(state.changeState(CbState.CLOSE)){
                    //状态由ATTEMPT_CLOSE 成功变更后 清零尝试成功数 并把令牌释放掉
                    strategy.attemptRenewSuccessCountReset();
                    this.releaseToken();
                    System.out.println("-------连续n 次试探调用成功  熔断器关闭！");
                }
            }else if(semaphore.availablePermits() == 0 && successCount < strategy.getAttemptRenewTimesThreshold() ){
                if(state.changeState(CbState.OPEN)){
                    //状态由ATTEMPT_CLOSE 成功变更后 清零尝试成功数 并把令牌释放掉
                    strategy.attemptRenewSuccessCountReset();
                    this.releaseToken();
                    System.out.println("-------n 次试探调用有失败   熔断器仍然打开！");
                }
            }
        }
    }

    /**
     * 执行应急方案 Fallback
     * @param args
     * @return
     */
    public Object executeFallback(Object...args){
        return  strategy.executeFallback(args);
    }


    /**
     * 获取熔断器状态
     * @return
     */
    public int getCurrentState(){

        switch (state.getCurrentState()){

            //当状态是OPEN时 检查是否可以切换到 尝试关闭状态 ATTEMPT_CLOSE
            case CbState.OPEN:{
                state.checkSwitchAttemptClose(strategy.getDalayTime());
                break;
            }

            //这里是做防范的 防止高并发下某个线程很迟久后才获取到cpu执行机会 在令牌用完了ATTEMPT_CLOSE状态就可以改变了
            // 其他线程可以照样有机会来把ATTEMPT_CLOSE状态改了
            case CbState.ATTEMPT_CLOSE:{
                //如果在熔断器尝试关闭 即ATTEMPT_CLOSE状态下  做尝试恢复计数 可能触发熔断器状态变更为OPEN或CLOSE状态
                int successCount = strategy.getAttemptRenewSuccessCount();
                System.out.println("successCount  "+successCount+"  semaphore.availablePermits()" +semaphore.availablePermits());
                //当限流令牌用完了 则说明尝试恢复正常调用次数已经执行完  看成功总次数有没有达到阈值
                if(semaphore.availablePermits() == 0 && successCount >= strategy.getAttemptRenewTimesThreshold() ){
                    if(state.changeState(CbState.CLOSE)){
                        //状态由ATTEMPT_CLOSE 成功变更后 清零尝试成功数 并把令牌释放掉
                        strategy.attemptRenewSuccessCountReset();
                        this.releaseToken();
                        System.out.println("-------连续n次试探调用成功  熔断器关闭！");
                    }
                }else if(semaphore.availablePermits() == 0 && successCount < strategy.getAttemptRenewTimesThreshold() ){
                    if(state.changeState(CbState.OPEN)){
                        //状态由ATTEMPT_CLOSE 成功变更后 清零尝试成功数 并把令牌释放掉
                        strategy.attemptRenewSuccessCountReset();
                        this.releaseToken();
                        System.out.println("-------n次试探调用有失败   熔断器仍然打开！");
                    }
                }
                break;
            }
        }

        return state.getCurrentState();
    }

}
