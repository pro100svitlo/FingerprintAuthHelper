package com.pro100svitlo.fingerprintAuthHelper

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log

/**
 * Created by pro100svitlo on 11/23/16.
 */
@Suppress("unused")
@SuppressLint("NewApi")
class FingerprintAuthHelper private constructor(b: Builder) {

    private var fahSecurityDialog: FahSecureSettingsDialog? = null
    private var context: Context = b.context
    private var loggingEnable = b.loggingEnable
    private var canListenBySystem = false

    val isSdkVersionOk: Boolean by lazy {
        logThis("isSdkVersionOk called")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            logThis("sdkVersionOk")
            return@lazy true
        }
        logThis("fingerprintAuthHelper cant work with sdk version < 23 (Android M)")
        return@lazy false
    }

    @JvmName("_isSdkVersionOk")
    @Deprecated("Use isSdkVersionOk property instead", ReplaceWith("isSdkVersionOk"))
    fun isSdkVersionOk() = isSdkVersionOk

    private val fahManager = if (isSdkVersionOk) FahManager(b.context, b.listener, b.keyName, loggingEnable, b.timeOut) else null

    var timeOutLeft = fahManager?.timeOutLeft ?: -1
        private set
        get() {
            logThis("getTimeOutLeft called")
            if (fahManager == null) {
                serviceNotEnable("getTimeOutLeft")
                return -1
            }
            field = fahManager.timeOutLeft
            logThis("timeOutLeft = $field millisecond")
            return field

        }

    @JvmName("_getTimeOutLeft")
    @Deprecated("Use getTimeOutLeft property instead", ReplaceWith("timeOutLeft"))
    fun getTimeOutLeft() = timeOutLeft

    var triesCountLeft: Int = 0
        private set
        get() {
            logThis("getTriesCountLeft called")
            if (fahManager == null) {
                serviceNotEnable("getTriesCountLeft")
                return 0
            }
            field = fahManager.triesCountLeft
            logThis("triesCountLeft = $field")
            return field
        }

    @JvmName("_getTriesCountLeft")
    @Deprecated("Use getTriesCountLeft property instead", ReplaceWith("triesCountLeft"))
    fun getTriesCountLeft() = triesCountLeft

    var canListenByUser = true
        @JvmName("canListenByUser")
        get() {
            logThis("getCanListenByUser called")
            logThis("canListenByUser = $field")
            return field
        }
        set(value) {
            logThis("setCanListenByUser called")
            logThis("setCanListenByUser = $value")
            canListenByUser = value
        }

    @JvmName("_canListenByUser")
    @Deprecated("Use canListenByUser property instead", ReplaceWith("canListenByUser"))
    fun canListenByUser() = canListenByUser

    @JvmName("_setCanListenByUser")
    @Deprecated("Use canListenByUser property instead", ReplaceWith("canListenByUser = canListen"))
    fun setCanListenByUser(canListen: Boolean) = { canListenByUser = canListen }

    var isHardwareEnable = false
        private set
        get() {
            logThis("isHardwareEnable called")
            if (fahManager == null) {
                serviceNotEnable("isHardwareEnable")
                return false
            }
            field = fahManager.isHardwareEnabled()
            logThis("isHardwareEnable = $field")
            return field
        }

    @JvmName("_isHardwareEnable")
    @Deprecated("Use isHardwareEnable property instead", ReplaceWith("isHardwareEnable"))
    fun isHardwareEnable() = isHardwareEnable

    var isListening = false
        private set
        get() {
            logThis("isListening called")
            if (fahManager == null) {
                serviceNotEnable("isListening")
                return false
            }
            field = fahManager.isListening()
            logThis("isListening = $field")
            return field
        }

    @JvmName("_isListening")
    @Deprecated("Use isListening property instead", ReplaceWith("isListening"))
    fun isListening() = isListening

    var isFingerprintEnrolled = false
        private set
        get() {
            if (fahManager == null) {
                serviceNotEnable("isFingerprintEnrolled")
                return false
            }
            field = fahManager.isFingerprintEnrolled()
            logThis("isFingerprintEnrolled = " + field)
            return field
        }

    @JvmName("_isFingerprintEnrolled")
    @Deprecated("Use isFingerprintEnrolled property instead", ReplaceWith("isFingerprintEnrolled"))
    fun isFingerprintEnrolled() = isFingerprintEnrolled

    fun cleanTimeOut(): Boolean {
        logThis("cleanTimeOut called")
        if (fahManager == null) {
            serviceNotEnable("cleanTimeOut")
            return false
        }
        val isCleaned = fahManager.cleanTimeOut()
        logThis("timeOutCleaned = $isCleaned")
        return isCleaned
    }

    fun startListening(): Boolean {
        logThis("startListening called")
        if (fahManager == null) {
            serviceNotEnable("startListening")
            return false
        }
        if (!canListenByUser) {
            return false
        }
        isListening = fahManager.startListening() && timeOutLeft <= 0
        logThis("isListening = $isListening")
        return isListening
    }

    fun stopListening(): Boolean {
        logThis("stopListening called")
        if (fahManager == null) {
            serviceNotEnable("stopListening")
            return false
        }
        if (!canListenByUser) {
            return false
        }
        isListening = fahManager.stopListening()
        logThis("isListening = $isListening")
        return isListening
    }

    fun onSaveInstanceState(outState: Bundle): Boolean {
        logThis("onSaveInstanceState called")
        if (fahManager == null) {
            serviceNotEnable("onSaveInstanceState")
            return false
        }
        fahManager.onSaveInstanceState(outState)
        logThis("onSaveInstanceState successful")
        return true
    }

    fun onRestoreInstanceState(savedInstanceState: Bundle): Boolean {
        logThis("onRestoreInstanceState called")
        if (fahManager == null) {
            serviceNotEnable("onRestoreInstanceState")
            return false
        }
        fahManager.onRestoreInstanceState(savedInstanceState)
        logThis("onRestoreInstanceState successful")
        return true
    }

    fun onDestroy(): Boolean {
        logThis("onDestroy called")

        if (fahManager == null) {
            serviceNotEnable("onDestroy")
            return false
        }
        fahManager.onDestroy()
        logThis("onDestroy successful")
        return true
    }

    fun canListen(showError: Boolean): Boolean {
        logThis("canListen called")
        if (fahManager == null) {
            serviceNotEnable("canListen")
            return false
        }
        canListenByUser
        canListenBySystem = fahManager.canListen(showError)
        logThis("canListenBySystem = $canListenBySystem")
        logThis("can listen = ${canListenByUser && canListenBySystem}")
        return canListenByUser && canListenBySystem
    }

    fun openSecuritySettings() {
        logThis("openSecuritySettings called")
        context.startActivity(Intent(Settings.ACTION_SECURITY_SETTINGS))
    }

    fun showSecuritySettingsDialog() {
        if (fahSecurityDialog == null) {
            fahSecurityDialog = FahSecureSettingsDialog.Builder(context, this)
                    .build()
        }
        fahSecurityDialog?.show()
    }

    private fun logThis(mess: String) {
        if (loggingEnable) Log.d(FahConstants.TAG, mess)
    }

    private fun serviceNotEnable(methodName: String) {
        logThis("method '$methodName' can't be finished, because of fingerprintService not enable")
    }

    class Builder(c: Context, internal val listener: FahListener) {

        internal var timeOut = FahConstants.DEF_TRY_TIME_OUT
        internal var context: Context = c as? Activity ?: throw IllegalArgumentException("Context for FingerprintAuthHelper must be instance of Activity")
        internal var keyName = FahConstants.TAG
        internal var loggingEnable: Boolean = false

        fun setKeyName(keyName: String): Builder {
            this.keyName = keyName
            return this
        }

        fun setLoggingEnable(enable: Boolean): Builder {
            loggingEnable = enable
            return this
        }

        fun setTryTimeOut(milliseconds: Long): Builder {
            if (milliseconds < FahConstants.DEF_TRY_TIME_OUT) {
                throw IllegalArgumentException("tryTimeout must be more than ${FahConstants.DEF_TRY_TIME_OUT} milliseconds!")
            }
            timeOut = milliseconds
            return this
        }

        fun build() = FingerprintAuthHelper(this)
    }
}