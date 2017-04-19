package com.pro100svitlo.fingerprintAuthHelper

import android.app.IntentService
import android.content.Intent
import android.os.Handler

/**
 * Created by pro100svitlo on 11/23/16.
 */
class FahTimeOutService : IntentService("FahTimeOutService") {

    private val broadcastIntent: Intent by lazy { Intent(FahConstants.TimeOutService.TIME_OUT_BROADCAST) }
    private val mTimeOutHandler = Handler()

    companion object {
        private var timeOutLeft: Long = 0
        private var timeOut: Long = 0
        private var running: Boolean = false
        private var wasStoppedPreviously: Boolean = false

        fun isRunning(): Boolean {
            return running
        }

        fun tryToStopMe(): Boolean {
            if (wasStoppedPreviously) return false
            wasStoppedPreviously = true
            if (timeOut - timeOutLeft >= FahConstants.DEF_TRY_TIME_OUT) {
                timeOutLeft = 0
                return true
            } else {
                timeOutLeft = FahConstants.DEF_TRY_TIME_OUT - (timeOut - timeOutLeft)
            }
            return false
        }
    }

    override fun onHandleIntent(intent: Intent?) {
        running = true
        timeOut = intent?.getLongExtra(FahConstants.TimeOutService.KEY_TRY_TIME_OUT, -1) ?: -1
        timeOutLeft = timeOut
        if (timeOutLeft > 0) {
            timeoutRunnable.run()
        }
    }


    private var timeoutRunnable: Runnable = object :Runnable{
        override fun run() {
            running = true
            timeOutLeft -= 1000
            if (timeOutLeft > 0) {
                mTimeOutHandler.postDelayed(this, 1000)
            } else {
                running = false
                timeOutLeft = 0
            }
            broadcastIntent.putExtra(FahConstants.Manager.KEY_TIME_OUT_LEFT, timeOutLeft)
            sendBroadcast(broadcastIntent)
            if (!running) {
                stopSelf()
            }
        }
    }
}