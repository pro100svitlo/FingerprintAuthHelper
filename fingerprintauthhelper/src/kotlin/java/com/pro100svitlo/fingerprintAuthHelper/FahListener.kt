package com.pro100svitlo.fingerprintAuthHelper

/**
 * Created by pro100svitlo on 8/26/16.
 */
interface FahListener {
    fun onFingerprintStatus(authSuccessful: Boolean, errorType: Int, errorMess: CharSequence)
    fun onFingerprintListening(listening: Boolean, milliseconds: Long)
}
