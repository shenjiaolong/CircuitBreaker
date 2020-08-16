package com.circuitbreaker.cbmode.status;

public class CbState {

    /** 熔断器关闭状态 **/
    public static final int CLOSE = 0x01;

    /** 熔断器打开状态 **/
    public static final int OPEN = 0x02;

    /** 熔断器尝试关闭状态 **/
    public static final int ATTEMPT_CLOSE = 0x03;

    /** 熔断器状态  默认关闭状态**/
    private volatile int state = CLOSE;


    /** 熔断器状态变更为 OPEN 时的时间戳**/
    private volatile long circuitOpenTime = 0;


    public CbState(){
        System.out.println("初始状态为 CLOSE");
    }

    /** 获取当前状态 */
    public int getCurrentState(){
        return state;
    }

    /** 重置熔断器状态 */
    public synchronized void resetState(){
        state = CLOSE;
    }

    /** 改变熔断器状态 */
    public synchronized boolean changeState(int state){
        /**这里也是做个状态保护 保证状态机的正常流转  因为三个状态的变更 是有固定方向的
         * 如当前处于关闭状态只能变更为打开状态  即新状态依赖的值依赖当前状态  这样的状态变更用不到传入的参数state
         * ATTEMPT_CLOSE状态除外需要用到state参数 因为它可变更成两个状态 */
        //双重检查 防止并发时 其他线程已经改过state置为a了 当前线程还想要把 state置为a
        if(getCurrentState()==state){
            return false;
        }
        switch (getCurrentState()){
            case CLOSE:
                this.state = OPEN;
                //当熔断器 变更为OPEN 状态时记录此刻时间戳
                circuitOpenTime = System.currentTimeMillis();
                System.out.println("当前状态变更为------state = OPEN  接下来执行应急方案");
                break;
            case OPEN:
                this.state = ATTEMPT_CLOSE;
                System.out.println("当前状态变更为------state = ATTEMPT_CLOSE 要限流尝试恢复了 ");
                break;
            case ATTEMPT_CLOSE:
                this.state = state;
                if(state == OPEN){
                    //当熔断器 变更为OPEN 状态时记录此刻时间戳
                    circuitOpenTime = System.currentTimeMillis();
                }
                System.out.println("当前状态变更为------state = "+ (state==1?"CLOSE 接下来执行正常方案":"OPEN  接下来执行FallBack方案"));
                break;
        }
        return true;
    }

    /**
     * 是否可以由 OPEN 状态切换到 ATTEMPT_CLOSE 状态
     * @return
     */
    public void checkSwitchAttemptClose( int dalayTime){
        if(state != OPEN){
            return;
        }
        long minute = (System.currentTimeMillis()-circuitOpenTime);
        if(minute >= dalayTime*1000){
           changeState(ATTEMPT_CLOSE);
        }
    }

}
