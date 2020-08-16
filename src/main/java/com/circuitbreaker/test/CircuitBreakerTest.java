package com.circuitbreaker.test;

import com.circuitbreaker.ApplicationContextHolder;
import com.circuitbreaker.cbmode.CircuitBreaker;
import com.circuitbreaker.cbmode.fallback.FallBack;
import com.circuitbreaker.cbmode.fallback.PrintFallBack;
import com.circuitbreaker.cbmode.strategy.CbStrategy;
import com.circuitbreaker.cbmode.strategy.FailContinuousCbStrategy;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Random;
import java.util.concurrent.CountDownLatch;

@RunWith(SpringJUnit4ClassRunner.class)//使用junit4进行测试
@ContextConfiguration({"classpath*:applicationContext.xml"})
public class CircuitBreakerTest {

    //模拟短时间内的并发请求量
    private static final int threadNum = 700;

    CircuitBreaker circuitBreaker;

    //执行前的参数集中在这里配置
    @Before
    public void init() {

        /**代表50次调用中 有7次失败就触发熔断
         * 50次调用后还没有触达7次失败,符合预期不熔断，则俩个相关计数清零重新计数*/
        int [] ciruitThreshold={7,50};

        //有需要的话 具体的降级应急计划fallBack也可以继承抽象类变更
        FallBack fallBack = new PrintFallBack();

        /** 为了方便演示 dalayTime 单位为妙  具体的熔断策略可以变更 new出来其他熔断策略实例即可*/
        CbStrategy strategy = new FailContinuousCbStrategy(3,3,10,fallBack);
        //CbStrategy strategy = new FailSumCbStrategy(ciruitThreshold,3,10,fallBack);

        circuitBreaker =(CircuitBreaker)ApplicationContextHolder.getContext().getBean("circuitBreaker");
        circuitBreaker.setStrategy(strategy);
    }

    @Test
    public void testMain() throws InterruptedException {

         CountDownLatch countDownLatch = new CountDownLatch(threadNum);

         /**友情提示：如果要试高并发下的运行结果  可以把for循环中i<threadNum换为true,所有的sleep去掉
          * 把dalayTime时间调小点方便观察日志 这时可参考配置FailSumCbStrategy（{15,70}，3,1 ,不变）
          * FailContinuousCbStrategy（5,3,1,不变）再稍作下改动即可*/
        for (int i = 0; i<threadNum; i++) {
            ProtectedTaskCallable callable =(ProtectedTaskCallable)ApplicationContextHolder.getContext().getBean("protectedTaskCallable");
            callable.setCountDownLatch(countDownLatch);

            //用于模拟用户并发  不用线程池是希望并发线程量大一些
            new Thread(callable).start();

            try {
                Thread.sleep(new Random().nextInt(3) * 100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        try{
            countDownLatch.await();
            System.out.println("测试结束");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }
}

