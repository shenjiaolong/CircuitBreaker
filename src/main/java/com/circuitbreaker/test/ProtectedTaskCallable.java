package com.circuitbreaker.test;

import com.circuitbreaker.cbmode.CircuitBreaker;
import com.circuitbreaker.cbmode.CircuitBreakerRun;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

public class ProtectedTaskCallable implements Runnable, Callable<Object> {
    @Autowired
    private CircuitBreaker circuitBreaker;
    @Autowired
    private Random random1;

    private CountDownLatch countDownLatch = null;

    public void run() {
        CircuitBreakerRun.run(circuitBreaker,this);
        //减缓一下执行速度方便观察
        try{
            Thread.sleep(100);
            countDownLatch.countDown();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public Object call() throws Exception {

        //用两个Random目的是增加 出现异常的随机性 来模拟失败调用
        if (random1.nextInt(5) == 2 || random1.nextInt(5) == 5){
            System.out.print("正常的业务逻辑调用失败了  xxxxxxxxxxxxxxx");
            throw new RuntimeException("未知异常！！");
        }else{
            System.out.println("执行正常的业务逻辑  -------------");
        }
        return null;
    }

    public void setCountDownLatch(CountDownLatch countDownLatch) {
        this.countDownLatch=countDownLatch;
    }
}