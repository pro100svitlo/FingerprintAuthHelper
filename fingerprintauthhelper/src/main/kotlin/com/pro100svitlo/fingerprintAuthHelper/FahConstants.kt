package com.pro100svitlo.fingerprintAuthHelper

/**
 * Created by pro100svitlo on 11/23/16.
 */
object FahConstants{

    @JvmStatic
    val TAG = FingerprintAuthHelper::class.java.simpleName!!
    @JvmStatic
    val DEF_TRY_TIME_OUT = (45 * 1000).toLong()
    object TimeOutService{
        @JvmStatic
        val KEY_TRY_TIME_OUT = "KEY_TRY_TIME_OUT"
        @JvmStatic
        val TIME_OUT_BROADCAST = "TIME_OUT_BROADCAST"
    }
    object Manager{
        @JvmStatic
        val KEY_TIME_OUT_LEFT = "KEY_TIME_OUT_LEFT"
    }
}