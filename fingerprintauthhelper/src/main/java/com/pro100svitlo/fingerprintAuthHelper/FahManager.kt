package com.pro100svitlo.fingerprintAuthHelper

import android.Manifest
import android.annotation.TargetApi
import android.app.KeyguardManager
import android.content.*
import android.content.pm.PackageManager
import android.hardware.fingerprint.FingerprintManager
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.os.Handler
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.KeyProperties
import android.support.annotation.NonNull
import android.support.v4.app.ActivityCompat
import android.util.Log
import java.io.IOException
import java.lang.ref.WeakReference
import java.security.*
import java.security.cert.CertificateException
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.NoSuchPaddingException
import javax.crypto.SecretKey

/**
 * Created by pro100svitlo on 11/23/16.
 */
@TargetApi(Build.VERSION_CODES.M)
internal class FahManager() : FingerprintManager.AuthenticationCallback() {

    class Builder(@NonNull internal val mContext: Context, l: FahListener) {

        internal val mListener: WeakReference<FahListener>
        internal var mKeyName: String? = null
        internal var mTryTimeOut: Long = 0
        internal var mLoggingEnable: Boolean = false

        init {
            mListener = WeakReference(l)
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
            mTryTimeOut = milliseconds
            return this
        }

        fun build(): FahManager {
            return FahManager(this)
        }
    }

    private val KEY_TO_MANY_TRIES_ERROR = "KEY_TO_MANY_TRIES_ERROR"
    private val KEY_LOGGING_ENABLE = "KEY_LOGGING_ENABLE"
    private val KEY_SECURE_KEY_NAME = "KEY_SECURE_KEY_NAME"
    private val KEY_IS_LISTENING = "KEY_IS_LISTENING"
    private val TRY_LEFT_DEFAULT = 5

    private var mContext: WeakReference<Context>? = null
    private var mListener: WeakReference<FahListener>? = null
    private var mFingerprintManager: FingerprintManager? = null
    private var mCipher: Cipher? = null
    private var mKeyStore: KeyStore? = null
    private var mKeyGenerator: KeyGenerator? = null
    private var mCancellationSignal: CancellationSignal? = null
    private var mCryptoObject: FingerprintManager.CryptoObject? = null
    private var keyguardManager: KeyguardManager? = null
    private var mShp: SharedPreferences? = null
    private var mTimeOutIntent: Intent? = null

    private var mKeyName: String? = null
    private var mTimeOutLeft: Long = 0
    private var mTryTimeOut: Long = 0
    private var mTriesCountLeft: Int = 0
    private var mTryTimeOutDefault: Long = 0
    private var mIsActivityForeground: Boolean = false
    private var mSelfCancelled: Boolean = false
    private var mIsListening: Boolean = false
    private var mLoggingEnable: Boolean = false
    private var mAfterStartListenTimeOut: Boolean = false
    private var mBroadcastRegistered: Boolean = false
    private var mSecureElementsReady: Boolean = false

    private constructor(b: Builder):this(){
        mContext = WeakReference(b.mContext)
        mListener = b.mListener
        mLoggingEnable = b.mLoggingEnable
        mKeyName = b.mKeyName
        mTryTimeOutDefault = b.mTryTimeOut

        mFingerprintManager = mContext!!.get()?.getSystemService(FingerprintManager::class.java)
        keyguardManager = mContext!!.get().getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager

        initAdditionalComponents()

        if (isTimerActive() && !FahTimeOutService.isRunning()) {
            mListener?.get()?.onFingerprintStatus(
                    false,
                    FahErrorType.Auth.AUTH_TO_MANY_TRIES,
                    getToManyTriesErrorStr())
            runTimeOutService()
        } else if (isTimerActive()) {
            registerBroadcast(true)
        }
    }


    override fun onAuthenticationError(errMsgId: Int,
                                       errString: CharSequence) {
        if (errMsgId == FingerprintManager.FINGERPRINT_ERROR_CANCELED && mAfterStartListenTimeOut) {
            //this needs because if developer decide to stopListening in onPause method of activity
            //or fragment, than onAuthenticationError() will notify user about sensor is turnedOff
            //in the next onResume() method
            return
        }
        if (!mSelfCancelled && !mAfterStartListenTimeOut) {
            logThis("onAuthenticationError called")
            logThis("error: " + FahErrorType.getErrorNameByCode(FahErrorType.AUTH_ERROR_BASE + errMsgId) +
                    " (" + errString + ")")

            if (mListener != null) {
                mListener?.get()?.onFingerprintStatus(
                        false,
                        FahErrorType.AUTH_ERROR_BASE + errMsgId,
                        errString)
            }

            logThis("stopListening")

            if (mListener != null) {
                mListener?.get()?.onFingerprintListening(false, 0)
            }
            if (errMsgId == FingerprintManager.FINGERPRINT_ERROR_LOCKOUT) {
                mShp!!.edit().putString(KEY_TO_MANY_TRIES_ERROR, errString.toString()).apply()
                runTimeOutService()
            }
        }
    }

    override fun onAuthenticationHelp(helpMsgId: Int,
                                      helpString: CharSequence) {
        if (helpMsgId == 0) {
            return
        }
        logThis("onAuthenticationHelp called")
        logThis("error: " + FahErrorType.getErrorNameByCode(FahErrorType.HELP_ERROR_BASE + helpMsgId) +
                " (" + helpString + ")")

        if (mListener != null) {
            mListener?.get()?.onFingerprintStatus(
                    false,
                    FahErrorType.HELP_ERROR_BASE + helpMsgId,
                    helpString)
        }
    }

    override fun onAuthenticationFailed() {
        logThis("AUTH_NOT_RECOGNIZED")
        mTriesCountLeft--
        if (mListener != null) {
            mListener?.get()?.onFingerprintStatus(
                    false,
                    FahErrorType.Auth.AUTH_NOT_RECOGNIZED,
                    mContext?.get()?.getString(R.string.FINGERPRINT_NOT_RECOGNIZED)!!)
        }
    }

    override fun onAuthenticationSucceeded(result: FingerprintManager.AuthenticationResult) {
        logThis("onAuthenticationSucceeded")
        mTriesCountLeft = TRY_LEFT_DEFAULT
        if (mListener != null) {
            mListener?.get()?.onFingerprintStatus(true, -1, "")
        }
    }

    internal fun startListening(): Boolean {
        mIsActivityForeground = true
        if (mTimeOutLeft > 0 || !canListen(true) || !initCipher()) {
            mIsListening = false
            mTriesCountLeft = 0
        } else {
            mAfterStartListenTimeOut = true
            Handler().postDelayed({ mAfterStartListenTimeOut = false }, 200)
            mCancellationSignal = CancellationSignal()
            mSelfCancelled = false
            // The line below prevents the false positive inspection from Android Studio

            mFingerprintManager?.authenticate(mCryptoObject, mCancellationSignal, 0 /* flags */, this, null)
            if (mListener != null) {
                mListener?.get()?.onFingerprintListening(true, 0)
            }
            mIsListening = true
            mTriesCountLeft = TRY_LEFT_DEFAULT
        }
        registerBroadcast(true)
        return mIsListening
    }

    internal fun stopListening(): Boolean {
        mIsActivityForeground = false
        if (mCancellationSignal != null) {
            mSelfCancelled = true
            mCancellationSignal?.cancel()
            mCancellationSignal = null
            mIsListening = false
        }
        registerBroadcast(false)
        mTriesCountLeft = TRY_LEFT_DEFAULT
        return mIsListening
    }

    internal fun onSaveInstanceState(outState: Bundle) {
        outState.putLong(FahConstants.TimeOutService.KEY_TRY_TIME_OUT, mTryTimeOut)
        outState.putLong(FahConstants.Manager.KEY_TIME_OUT_LEFT, mTimeOutLeft)
        outState.putBoolean(KEY_LOGGING_ENABLE, mLoggingEnable)
        outState.putString(KEY_SECURE_KEY_NAME, mKeyName)
        outState.putBoolean(KEY_IS_LISTENING, mIsListening)
    }

    internal fun onRestoreInstanceState(savedInstanceState: Bundle) {
        mTryTimeOut = savedInstanceState.getLong(FahConstants.TimeOutService.KEY_TRY_TIME_OUT)
        mTimeOutLeft = savedInstanceState.getLong(FahConstants.Manager.KEY_TIME_OUT_LEFT)
        mLoggingEnable = savedInstanceState.getBoolean(KEY_LOGGING_ENABLE)
        mKeyName = savedInstanceState.getString(KEY_SECURE_KEY_NAME)
        mIsListening = savedInstanceState.getBoolean(KEY_IS_LISTENING, false)

        if (mTimeOutLeft > 0 && mListener?.get() != null) {
            mListener?.get()?.onFingerprintListening(false, mTimeOutLeft)
        }
    }

    internal fun onDestroy() {
        mContext = null
        mFingerprintManager = null
        keyguardManager = null
        mKeyStore = null
        mKeyGenerator = null
        mCipher = null
        mCryptoObject = null
        mListener?.clear()
        mListener = null
        mKeyName = null
        mTimeOutIntent = null
    }

    internal fun isListening(): Boolean {
        return mIsListening
    }

    internal fun canListen(showError: Boolean): Boolean {
        if (!isSecureComponentsInit(showError)) {
            return false
        }

        if (isPermissionNeeded(showError)) {
            return false
        }
        if (!isHardwareEnabled()) {
            if (mListener != null && showError) {
                mListener?.get()?.onFingerprintStatus(
                        false,
                        FahErrorType.General.HARDWARE_DISABLED,
                        mContext?.get()?.getString(R.string.HARDWARE_DISABLED)!!)
            }
            logThis("canListen failed. reason: " + mContext?.get()?.getString(R.string.HARDWARE_DISABLED))
            return false
        }

        if (!keyguardManager!!.isKeyguardSecure) {
            if (mListener != null && showError) {
                mListener?.get()?.onFingerprintStatus(
                        false,
                        FahErrorType.General.LOCK_SCREEN_DISABLED,
                        mContext?.get()?.getString(R.string.LOCK_SCREEN_DISABLED)!!)
            }
            logThis("canListen failed. reason: " + mContext?.get()?.getString(R.string.LOCK_SCREEN_DISABLED))
            return false
        }

        return true
    }

    internal fun isHardwareEnabled(): Boolean {
        if (isPermissionNeeded(false)) {
            throw SecurityException("Missing 'USE_FINGERPRINT' permission!")
        }
        return mFingerprintManager?.isHardwareDetected as Boolean
    }

    internal fun isFingerprintEnrolled(): Boolean {
        return isFingerprintEnrolled(false)
    }

    internal fun getTimeOutLeft(): Long {
        return mTimeOutLeft
    }

    internal fun getTriesCountLeft(): Int{
        return mTriesCountLeft
    }

    internal fun cleanTimeOut(): Boolean {
        if (isTimerActive() && FahTimeOutService.isRunning() && FahTimeOutService.tryToStopMe()) {
            mTimeOutLeft = 0
            saveTimeOut(-1)
            return true
        }
        return false
    }

    private fun isSecureComponentsInit(showError: Boolean): Boolean {
        logThis("isSecureComponentsInit start")
        if (isFingerprintEnrolled(showError) && !mSecureElementsReady) {
            try {
                mCipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/"
                        + KeyProperties.BLOCK_MODE_CBC + "/"
                        + KeyProperties.ENCRYPTION_PADDING_PKCS7)
            } catch (e: NoSuchAlgorithmException) {
                throw RuntimeException("Failed to get an instance of Cipher", e)
            } catch (e: NoSuchPaddingException) {
                throw RuntimeException("Failed to get an instance of Cipher", e)
            }

            try {
                mKeyStore = KeyStore.getInstance("AndroidKeyStore")
            } catch (e: Exception) {
                throw RuntimeException("create keyStore failed", e)
            }

            try {
                mKeyGenerator = KeyGenerator
                        .getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
            } catch (e: NoSuchAlgorithmException) {
                throw RuntimeException("Failed to get an instance of KeyGenerator", e)
            } catch (e: NoSuchProviderException) {
                throw RuntimeException("Failed to get an instance of KeyGenerator", e)
            }

            try {
                mKeyStore!!.load(null)
                // Set the alias of the entry in Android KeyStore where the key will appear
                // and the constrains (purposes) in the constructor of the Builder

                val builder = KeyGenParameterSpec.Builder(mKeyName,
                        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                        .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                        // Require the user to authenticate with a fingerprint to authorize every use
                        // of the key
                        .setUserAuthenticationRequired(true)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)

                // This is a workaround to avoid crashes on devices whose API level is < 24
                // because KeyGenParameterSpec.Builder#setInvalidatedByBiometricEnrollment is only
                // visible on API level +24.
                // Ideally there should be a compat library for KeyGenParameterSpec.Builder but
                // which isn't available yet.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    builder.setInvalidatedByBiometricEnrollment(true)
                }
                try {
                    mKeyGenerator!!.init(builder.build())
                    mKeyGenerator!!.generateKey()
                } catch (e: RuntimeException) {
                    mSecureElementsReady = false
                    logThis("isSecureComponentsInit failed. Reason: " + e.message)
                    return false
                }

            } catch (e: NoSuchAlgorithmException) {
                throw RuntimeException(e)
            } catch (e: InvalidAlgorithmParameterException) {
                throw RuntimeException(e)
            } catch (e: CertificateException) {
                throw RuntimeException(e)
            } catch (e: IOException) {
                throw RuntimeException(e)
            }

            mSecureElementsReady = true
        }
        logThis("mSecureElementsReady = " + mSecureElementsReady)
        return mSecureElementsReady
    }

    private fun initAdditionalComponents() {
        mShp = mContext!!.get().getSharedPreferences(mContext!!.get().getString(R.string.fah_app_name), Context.MODE_PRIVATE)
        mTryTimeOut = mTryTimeOutDefault
    }

    private fun initCipher(): Boolean {
        try {
            mKeyStore!!.load(null)
            val key = mKeyStore!!.getKey(mKeyName, null) as SecretKey
            mCipher!!.init(Cipher.ENCRYPT_MODE, key)
            mCryptoObject = FingerprintManager.CryptoObject(mCipher)
            return true
        } catch (e: KeyPermanentlyInvalidatedException) {
            logThis("initCipher failed. Reason: " + e.message)
            return false
        } catch (e: KeyStoreException) {
            throw RuntimeException("Failed to init Cipher", e)
        } catch (e: CertificateException) {
            throw RuntimeException("Failed to init Cipher", e)
        } catch (e: UnrecoverableKeyException) {
            throw RuntimeException("Failed to init Cipher", e)
        } catch (e: IOException) {
            throw RuntimeException("Failed to init Cipher", e)
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException("Failed to init Cipher", e)
        } catch (e: InvalidKeyException) {
            throw RuntimeException("Failed to init Cipher", e)
        }

    }

    private fun isPermissionNeeded(showError: Boolean): Boolean {
        if (mContext?.get()?.let { ActivityCompat.checkSelfPermission(it, Manifest.permission.USE_FINGERPRINT) } == PackageManager.PERMISSION_GRANTED) {
            logThis("USE_FINGERPRINT PERMISSION = PERMISSION_GRANTED")
            return false
        }
        logThis("USE_FINGERPRINT PERMISSION = PERMISSION_DENIED")
        if (mListener != null && showError) {
            mListener?.get()?.onFingerprintStatus(
                    false,
                    FahErrorType.General.PERMISSION_NEEDED,
                    mContext?.get()?.getString(R.string.PERMISSION_NEEDED)!!)
        }
        return true
    }

    private fun isFingerprintEnrolled(showError: Boolean): Boolean {
        if (!mFingerprintManager!!.hasEnrolledFingerprints()) {
            if (mListener != null && showError) {
                mListener?.get()?.onFingerprintStatus(
                        false,
                        FahErrorType.General.NO_FINGERPRINTS,
                        mContext?.get()?.getString(R.string.NO_FINGERPRINTS)!!)
            }
            logThis("canListen failed. reason: " + mContext?.get()?.getString(R.string.NO_FINGERPRINTS))
            mSecureElementsReady = false
            return false
        }
        return true
    }

    private fun registerBroadcast(register: Boolean) {
        if (mTimeOutLeft > 0 && register && !mBroadcastRegistered) {
            logThis("mBroadcastRegistered = " + true)
            mBroadcastRegistered = true
            mContext?.get()?.registerReceiver(timeOutBroadcast, IntentFilter(FahConstants.TimeOutService.TIME_OUT_BROADCAST))
        } else if (mTimeOutLeft > 0 && !register && mBroadcastRegistered && !FahTimeOutService.isRunning()) {
            logThis("mBroadcastRegistered = " + false)
            mBroadcastRegistered = false
            mContext?.get()?.unregisterReceiver(timeOutBroadcast)
        }
    }

    private fun runTimeOutService() {
        logThis("runTimeOutService")
        if (mTimeOutIntent == null) {
            mTimeOutIntent = Intent(mContext?.get(), FahTimeOutService::class.java)
        }
        mTimeOutLeft = mTryTimeOutDefault
        registerBroadcast(true)
        mTimeOutIntent!!.putExtra(FahConstants.TimeOutService.KEY_TRY_TIME_OUT, mTryTimeOut)
        mContext?.get()?.startService(mTimeOutIntent)
        saveTimeOut(System.currentTimeMillis() + mTryTimeOut)
    }

    private fun saveTimeOut(timesLeft: Long) {
        mShp!!.edit().putLong(FahConstants.Manager.KEY_TIME_OUT_LEFT, timesLeft).apply()
    }

    private fun isTimerActive(): Boolean {
        val i = mShp!!.getLong(FahConstants.Manager.KEY_TIME_OUT_LEFT, -1)
        val current = System.currentTimeMillis()
        if (current < i) {
            mTryTimeOut = i - current
            mTimeOutLeft = mTryTimeOut
            logThis("isTimeOutActive = " + true)
            return true
        }
        logThis("isTimeOutActive = " + false)
        return false
    }

    private fun getToManyTriesErrorStr(): String {
        return mShp!!.getString(KEY_TO_MANY_TRIES_ERROR, mContext?.get()?.getString(R.string.AUTH_TO_MANY_TRIES))
    }

    private fun logThis(mess: String) {
        if (mLoggingEnable) {
            Log.d(FahConstants.TAG, mess)
        }
    }

    private val timeOutBroadcast = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            mTimeOutLeft = intent.getLongExtra(FahConstants.Manager.KEY_TIME_OUT_LEFT, -1)

            logThis("mTimeOutLeft = " + (mTimeOutLeft / 1000).toString() + " sec")

            if (mTimeOutLeft > 0 && mListener != null) {
                if (mIsActivityForeground) {
                    mListener?.get()?.onFingerprintListening(false, mTimeOutLeft)
                }
                saveTimeOut(System.currentTimeMillis() + mTimeOutLeft)
            } else if (mTimeOutLeft <= 0){
                registerBroadcast(false)
                saveTimeOut(-1)
                mTriesCountLeft = TRY_LEFT_DEFAULT
                mTryTimeOut = mTryTimeOutDefault
                if (mIsActivityForeground) {
                    startListening()
                }
                logThis("startListening after timeout")
            }
        }
    }
}