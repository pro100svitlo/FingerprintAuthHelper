package com.pro100svitlo.fingerprintAuthHelper;

import android.app.IntentService;
import android.content.Intent;
import android.os.Handler;

/**
 * Created by pro100svitlo on 8/29/16.
 */
public class FahTimeOutService extends IntentService {

    public final static String KEY_TRY_TIME_OUT = "KEY_TRY_TIME_OUT";
    public final static String TIME_OUT_BROADCAST = "TIME_OUT_BROADCAST";

    private Intent broadcastIntent;
    private final Handler mTimeOutHandler = new Handler();
    private long mTimeOutLeft = 0;
    private static boolean isRunning;

    public FahTimeOutService() {
        super(FahTimeOutService.class.getSimpleName());
    }

    public static boolean isRunning() {
        return isRunning;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        isRunning = true;
        broadcastIntent = new Intent(TIME_OUT_BROADCAST);
        mTimeOutLeft = intent.getLongExtra(KEY_TRY_TIME_OUT, -1);
        timeoutRunnable.run();
    }

    private final Runnable timeoutRunnable = new Runnable() {
        @Override
        public void run() {
            isRunning = true;
            mTimeOutLeft = mTimeOutLeft - 1000;
            if (mTimeOutLeft > 0){
                mTimeOutHandler.postDelayed(timeoutRunnable, 1000);
            } else {
                isRunning = false;
                mTimeOutLeft = 0;
            }
            broadcastIntent.putExtra(FahManager.KEY_TIME_OUT_LEFT, mTimeOutLeft);
            sendBroadcast(broadcastIntent);
            if (!isRunning){
                stopSelf();
            }
        }
    };
}
