package com.xugongming38.chatonwifi.activity;

/**
 * Created by dell on 2017/6/24.
 */

public abstract class MyTimerCheck {
    private int mCount = 0;
    private int mTimeOutCount = 1;
    private int mSleepTime = 1000; // 1s
    private boolean mExitFlag = false;
    private Thread mThread = null;

    /**
     * Do not process UI work in this.
     */
    public abstract void doTimerCheckWork();

    public abstract void doTimeOutWork();

    public MyTimerCheck() {
        mThread = new Thread(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                while (!mExitFlag) {
                    mCount++;
                    if (mCount < mTimeOutCount) {
                        doTimerCheckWork();
                        try {
                            mThread.sleep(mSleepTime);
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                            exit();
                        }
                    } else {
                        doTimeOutWork();
                    }
                }
            }
        });
    }



    public void start(int timeOutCount, int sleepTime) {
        mTimeOutCount = timeOutCount;
        mSleepTime = sleepTime;

        mThread.start();
    }

    public void exit() {
        mExitFlag = true;
    }

}