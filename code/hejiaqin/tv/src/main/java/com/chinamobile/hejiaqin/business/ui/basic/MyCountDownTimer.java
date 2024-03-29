package com.chinamobile.hejiaqin.business.ui.basic;

import android.os.CountDownTimer;

/**
 * desc:
 * project:hejiaqin
 * version 001
 * author:
 * Created: 2016/5/10.
 */
public class MyCountDownTimer extends CountDownTimer {

    public final static long MILL_IS_INFUTURE = 60000;

    public final static long COUNTDOWN_INTERVAL = 1000;

    private static MyCountDownTimer myCountDownTimer;

    private long millisUntilFinished;

    /**
     * @param millisInFuture 总的时间
     */
    public MyCountDownTimer(long millisInFuture) {
        super(millisInFuture, COUNTDOWN_INTERVAL);
        if (myCountDownTimer != null) {
            myCountDownTimer.cancel();
        }
        myCountDownTimer = this;
        millisUntilFinished = millisInFuture;
    }

    @Override
    public void onTick(long millisUntilFinished) {
        this.millisUntilFinished = millisUntilFinished;
    }

    @Override
    public void onFinish() {
        this.millisUntilFinished = 0;
    }

    public long getMillisUntilFinished() {
        return millisUntilFinished;
    }

    //    public void stop() {
    //        super.cancel();
    //        myCountDownTimer=null;
    //    }

    //    public static long getMyMillisUntilFinished()
    //    {
    //        if(myCountDownTimer==null)
    //        {
    //            return 0;
    //        }
    //        return  myCountDownTimer.getMillisUntilFinished();
    //    }

}
