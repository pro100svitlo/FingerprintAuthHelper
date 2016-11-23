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
    private static long sTimeOutLeft = 0;
    private static long sTimeOut;
    private static boolean isRunning;

    public FahTimeOutService() {
        super(FahTimeOutService.class.getSimpleName());
    }

    public static boolean isRunning() {
        return isRunning;
    }

    public static boolean tryToStopMe() {
        if (sTimeOut - sTimeOutLeft >= FingerprintAuthHelper.Builder.DEF_TRY_TIME_OUT) {
            sTimeOutLeft = 0;
            return true;
        }
        return false;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        isRunning = true;
        broadcastIntent = new Intent(TIME_OUT_BROADCAST);
        sTimeOut = intent.getLongExtra(KEY_TRY_TIME_OUT, -1);
        sTimeOutLeft =  sTimeOut;
        if (sTimeOutLeft > 0) {
            timeoutRunnable.run();
        }
    }

    private final Runnable timeoutRunnable = new Runnable() {
        @Override
        public void run() {
            isRunning = true;
            sTimeOutLeft = sTimeOutLeft - 1000;
            if (sTimeOutLeft > 0){
                mTimeOutHandler.postDelayed(timeoutRunnable, 1000);
            } else {
                isRunning = false;
                sTimeOutLeft = 0;
            }
            broadcastIntent.putExtra(FahManager.KEY_TIME_OUT_LEFT, sTimeOutLeft);
            sendBroadcast(broadcastIntent);
            if (!isRunning){
                stopSelf();
            }
        }
    };
}
