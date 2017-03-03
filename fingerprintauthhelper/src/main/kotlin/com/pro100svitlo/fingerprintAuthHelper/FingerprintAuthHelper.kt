package com.pro100svitlo.fingerprintAuthHelper

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.support.annotation.NonNull
import android.util.Log

/**
 * Created by pro100svitlo on 11/23/16.
 */
class FingerprintAuthHelper private constructor(b: Builder) {

    private var mFahManager: FahManager? = null
    private var mFahSecurityDialog: FahSecureSettingsDialog? = null
    private var mContext: Context? = null

    private var mTimeOutLeft: Long = 0
    private var mTriesCountLeft: Int = 0
    private var mLoggingEnable = false
    private var mCanListenByUser = true
    private var mCanListenBySystem = false
    private var mIsHardwareEnable = false
    private var mIsListening = false
    private var mIsFingerprintEnrolled = false
    private var mIsTimeOutCleaned = false

    init {
        mContext = b.mContext
        mLoggingEnable = b.mLoggingEnable

        if (isSdkVersionOk()) {
            mFahManager = FahManager(b.mContext!!, b.mListener, b.mKeyName, mLoggingEnable, b.mTimeOut)
            mTimeOutLeft = mFahManager!!.mTimeOutLeft
        }
    }

    fun startListening(): Boolean {
        logThis("startListening called")
        if (mFahManager == null) {
            serviceNotEnable("startListening")
            return false
        }
        if (!canListenByUser()) {
            return false
        }
        mIsListening = mFahManager!!.startListening() && mTimeOutLeft <= 0
        logThis("mIsListening = " + mIsListening)
        return mIsListening
    }

    fun stopListening(): Boolean {
        logThis("stopListening called")
        if (mFahManager == null) {
            serviceNotEnable("stopListening")
            return false
        }
        if (!canListenByUser()) {
            return false
        }
        mIsListening = mFahManager!!.stopListening()
        logThis("mIsListening = " + mIsListening)
        return mIsListening
    }

    fun onSaveInstanceState(outState: Bundle): Boolean {
        logThis("onSaveInstanceState called")
        if (mFahManager == null) {
            serviceNotEnable("onSaveInstanceState")
            return false
        }
        mFahManager!!.onSaveInstanceState(outState)
        logThis("onSaveInstanceState successful")
        return true
    }

    fun onRestoreInstanceState(savedInstanceState: Bundle): Boolean {
        logThis("onRestoreInstanceState called")
        if (mFahManager == null) {
            serviceNotEnable("onRestoreInstanceState")
            return false
        }
        mFahManager!!.onRestoreInstanceState(savedInstanceState)
        logThis("onRestoreInstanceState successful")
        return true
    }

    fun onDestroy(): Boolean {
        logThis("onDestroy called")
        mContext = null

        if (mFahManager == null) {
            serviceNotEnable("onDestroy")
            return false
        }
        mFahManager!!.onDestroy()
        mFahManager = null
        logThis("onDestroy successful")
        return true
    }

    fun isListening(): Boolean {
        logThis("isListening called")
        if (mFahManager == null) {
            serviceNotEnable("isListening")
            return false
        }
        mIsListening = mFahManager!!.isListening()
        logThis("isListening = " + mIsListening)
        return mIsListening
    }

    fun canListen(showError: Boolean): Boolean {
        logThis("canListen called")
        if (mFahManager == null) {
            serviceNotEnable("canListen")
            return false
        }
        canListenByUser()
        mCanListenBySystem = mFahManager!!.canListen(showError)
        logThis("canListenBySystem = " + mCanListenBySystem)
        logThis("can listen = " + (mCanListenByUser && mCanListenBySystem).toString())
        return mCanListenByUser && mCanListenBySystem
    }

    fun canListenByUser(): Boolean {
        logThis("canListenByUser called")
        logThis("canListenByUser = " + mCanListenByUser)
        return mCanListenByUser
    }

    fun setCanListenByUser(canListen: Boolean) {
        logThis("setCanListenByUser called")
        logThis("setCanListenByUser = " + canListen)
        mCanListenByUser = canListen
    }

    fun getTimeOutLeft(): Long {
        logThis("getTimeOutLeft called")
        if (mFahManager == null) {
            serviceNotEnable("getTimeOutLeft")
            return -1
        }
        mTimeOutLeft = mFahManager!!.mTimeOutLeft
        logThis("timeOutLeft = $mTimeOutLeft millisecond")
        return mTimeOutLeft
    }

    fun getTriesCountLeft(): Int {
        logThis("getTriesCountLeft called")
        if (mFahManager == null) {
            serviceNotEnable("getTriesCountLeft")
            return 0
        }
        mTriesCountLeft = mFahManager!!.mTriesCountLeft
        logThis("triesCountLeft = $mTriesCountLeft")
        return mTriesCountLeft
    }

    fun cleanTimeOut(): Boolean {
        logThis("cleanTimeOut called")
        if (mFahManager == null) {
            serviceNotEnable("cleanTimeOut")
            return false
        }
        mIsTimeOutCleaned = mFahManager!!.cleanTimeOut()
        logThis("timeOutCleaned = " + mIsTimeOutCleaned)
        return mIsTimeOutCleaned
    }

    fun isHardwareEnable(): Boolean {
        logThis("isHardwareEnable called")
        if (mFahManager == null) {
            serviceNotEnable("isHardwareEnable")
            return false
        }
        mIsHardwareEnable = mFahManager!!.isHardwareEnabled()
        logThis("mIsHardwareEnable = " + mIsHardwareEnable)
        return mIsHardwareEnable
    }

    fun isFingerprintEnrolled(): Boolean {
        if (mFahManager == null) {
            serviceNotEnable("isFingerprintEnrolled")
            return false
        }
        mIsFingerprintEnrolled = mFahManager!!.isFingerprintEnrolled()
        logThis("mIsFingerprintEnrolled = " + mIsFingerprintEnrolled)
        return mIsFingerprintEnrolled
    }

    fun openSecuritySettings() {
        logThis("openSecuritySettings called")
        mContext?.startActivity(Intent(Settings.ACTION_SECURITY_SETTINGS))
    }

    fun showSecuritySettingsDialog() {
        if (mFahSecurityDialog == null && mContext != null) {
            mFahSecurityDialog = FahSecureSettingsDialog.Builder(mContext!!, this)
                    .build()
        }
        mFahSecurityDialog?.show()
    }

    private fun isSdkVersionOk(): Boolean {
        logThis("isSdkVersionOk called")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            logThis("sdkVersionOk")
            return true
        }
        logThis("fingerprintAuthHelper cant work with sdk version < 23 (Android M)")
        return false
    }

    private fun logThis(mess: String) {
        if (mLoggingEnable) Log.d(FahConstants.TAG, mess)
    }

    private fun serviceNotEnable(methodName: String) {
        logThis("method '$methodName' can't be finished, because of fingerprintService not enable")
    }

    class Builder(@NonNull c: Context, l: FahListener) {

        internal var mTimeOut = FahConstants.DEF_TRY_TIME_OUT
        internal var mContext: Context? = null
        internal val mListener: FahListener? = l
        internal var mKeyName = FahConstants.TAG
        internal var mLoggingEnable: Boolean = false

        init {
            if (c is Activity) {
                mContext = c
            } else {
                throw IllegalArgumentException("Context for FingerprintAuthHelper must be instance of Activity")
            }
            mContext = c
        }

        fun setKeyName(keyName: String): Builder {
            mKeyName = keyName
            return this
        }

        fun setLoggingEnable(enable: Boolean): Builder {
            mLoggingEnable = enable
            return this
        }

        fun setTryTimeOut(milliseconds: Long): Builder {
            if (milliseconds < FahConstants.DEF_TRY_TIME_OUT) {
                throw IllegalArgumentException("tryTimeout must be more than " + FahConstants.DEF_TRY_TIME_OUT + " milliseconds!")
            }
            mTimeOut = milliseconds
            return this
        }

        fun build(): FingerprintAuthHelper {
            return FingerprintAuthHelper(this)
        }
    }
}