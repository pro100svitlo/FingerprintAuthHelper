package com.pro100svitlo.fingerprintAuthHelper

/**
 * Created by pro100svitlo on 11/23/16.
 */
internal object FahConstants{

    val TAG: String = FingerprintAuthHelper::class.java.simpleName

    const val DEF_TRY_TIME_OUT = 45 * 1000L

    object TimeOutService{
        const val KEY_TRY_TIME_OUT = "KEY_TRY_TIME_OUT"
        const val TIME_OUT_BROADCAST = "TIME_OUT_BROADCAST"
    }

    object Manager{
        const val KEY_TIME_OUT_LEFT = "KEY_TIME_OUT_LEFT"
    }
}