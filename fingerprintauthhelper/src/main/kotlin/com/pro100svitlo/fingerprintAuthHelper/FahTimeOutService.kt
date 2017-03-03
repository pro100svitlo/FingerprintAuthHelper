package com.pro100svitlo.fingerprintAuthHelper

import android.app.IntentService
import android.content.Intent
import android.os.Handler

/**
 * Created by pro100svitlo on 11/23/16.
 */
class FahTimeOutService : IntentService("FahTimeOutService") {

    private var broadcastIntent: Intent? = null
    private val mTimeOutHandler = Handler()

    companion object {
        private var sTimeOutLeft: Long = 0
        private var sTimeOut: Long = 0
        private var running: Boolean = false

        fun isRunning(): Boolean {
            return running
        }

        fun tryToStopMe(): Boolean {
            if (sTimeOut - sTimeOutLeft >= FahConstants.DEF_TRY_TIME_OUT) {
                sTimeOutLeft = 0
                return true
            } else {
                sTimeOutLeft = FahConstants.DEF_TRY_TIME_OUT - (sTimeOut - sTimeOutLeft)
            }
            return false
        }
    }

    override fun onHandleIntent(intent: Intent?) {
        running = true
        broadcastIntent = Intent(FahConstants.TimeOutService.TIME_OUT_BROADCAST)
        sTimeOut = intent!!.getLongExtra(FahConstants.TimeOutService.KEY_TRY_TIME_OUT, -1)
        sTimeOutLeft = sTimeOut
        if (sTimeOutLeft > 0) {
            timeoutRunnable.run()
        }
    }


    private var timeoutRunnable: Runnable = object :Runnable{
        override fun run() {
            running = true
            sTimeOutLeft -= 1000
            if (sTimeOutLeft > 0) {
                mTimeOutHandler.postDelayed(this, 1000)
            } else {
                running = false
                sTimeOutLeft = 0
            }
            broadcastIntent?.putExtra(FahConstants.Manager.KEY_TIME_OUT_LEFT, sTimeOutLeft)
            sendBroadcast(broadcastIntent)
            if (!running) {
                stopSelf()
            }
        }
    }
}