package com.pro100svitlo.fingerprintAuthHelper;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Handler;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

/**
 * Created by pro100svitlo on 10/3/16.
 */

@TargetApi(Build.VERSION_CODES.M)
public class FahManager extends FingerprintManager.AuthenticationCallback {

    public static class Builder {

        private Context mContext;
        private WeakReference<FahListener> mListener;
        private String mKeyName;
        private long mTryTimeOut;
        private int mMaxTryCount;
        private boolean mLoggingEnable;

        public Builder(Context c, FahListener l) {
            mContext = c;
            mListener = new WeakReference<>(l);
        }

        public Builder setKeyName(String keyName) {
            mKeyName = keyName;
            return this;
        }

        public Builder setLoggingEnable(boolean enable) {
            mLoggingEnable = enable;
            return this;
        }

        public Builder setMaxTryCount(int maxTryCount){
            mMaxTryCount = maxTryCount;
            return this;
        }

        public Builder setTryTimeOut(long milliseconds) {
            mTryTimeOut = milliseconds;
            return this;
        }

        public FahManager build() {
            return new FahManager(this);
        }
    }

    public final static String KEY_TIME_OUT_LEFT = "KEY_TIME_OUT_LEFT";

    private final static String KEY_TO_MANY_TRIES_ERROR = "KEY_TO_MANY_TRIES_ERROR";
    private final static String KEY_MAX_TRY_COUNT = "KEY_MAX_TRY_COUNT";
    private final static String KEY_TRY_COUNT = "KEY_TRY_COUNT";
    private final static String KEY_LOGGING_ENABLE = "KEY_LOGGING_ENABLE";
    private final static String KEY_SECURE_KEY_NAME = "KEY_SECURE_KEY_NAME";
    private final static String KEY_IS_LISTENING = "KEY_IS_LISTENING";
    private final int DEFAULT_TRY_COUNT = 5;

    private Context mContext;
    private WeakReference<FahListener> mListener;
    private FingerprintManager mFingerprintManager;
    private Cipher mCipher;
    private KeyStore mKeyStore;
    private KeyGenerator mKeyGenerator;
    private CancellationSignal mCancellationSignal;
    private FingerprintManager.CryptoObject mCryptoObject;
    private KeyguardManager keyguardManager;
    private SharedPreferences mShp;
    private SharedPreferences.Editor mEditor;
    private Intent mTimeOutIntent;

    private String mKeyName;
    private int mTryCount;
    private int mMaxTryCount;
    private long mTimeOutLeft;
    private long mTryTimeOut;
    private long mTryTimeOutDefault;
    private boolean mSelfCancelled;
    private boolean mIsListening;
    private boolean mLoggingEnable;
    private boolean mAfterStartListenTimeOut;
    private boolean mBroadcastRegistered;
    private boolean mSecureElementsReady;

    private FahManager(Builder b){
        mContext = b.mContext;
        mListener = b.mListener;
        mTryCount = mMaxTryCount;
        mLoggingEnable = b.mLoggingEnable;
        mMaxTryCount = b.mMaxTryCount;
        mKeyName = b.mKeyName;
        mTryTimeOutDefault = b.mTryTimeOut;

        mFingerprintManager = mContext.getSystemService(FingerprintManager.class);
        keyguardManager = (KeyguardManager) mContext.getSystemService(Context.KEYGUARD_SERVICE);

        initAdditionalComponents();

        if (isTimerActive() && !FahTimeOutService.isRunning()){
            mListener.get().onFingerprintStatus(
                    false,
                    FahErrorType.Auth.AUTH_TO_MANY_TRIES,
                    getToManyTriesErrorStr());
            runTimeOutService();
        }
    }

    @Override
    public void onAuthenticationError(int errMsgId,
                                      CharSequence errString) {
        if (errMsgId == FingerprintManager.FINGERPRINT_ERROR_CANCELED && mAfterStartListenTimeOut){
            //this needs because if developer decide to stopListening in onPause method of activity
            //or fragment, than onAuthenticationError() will notify user about sensor is turnedOff
            //in the next onResume() method
            return;
        }
        if(!mSelfCancelled && !mAfterStartListenTimeOut) {
            logThis("onAuthenticationError called");
            logThis("error: " + FahErrorType.getErrorNameByCode(FahErrorType.AUTH_ERROR_BASE + errMsgId) +
            " (" + errString + ")");

            if (mListener != null && mTryCount > 0) {
                mListener.get().onFingerprintStatus(
                        false,
                        FahErrorType.AUTH_ERROR_BASE + errMsgId,
                        errString);
            }
            if (mTryCount <= 0) {
                mTryCount = mMaxTryCount;
            }
            logThis("mTryCount left = "+ mTryCount);

            stopListening();
            logThis("stopListening");

            if (mListener != null) {
                mListener.get().onFingerprintListening(false, 0);
            }
            if (errMsgId == FingerprintManager.FINGERPRINT_ERROR_LOCKOUT) {
                saveToManyTriesStr(errString.toString());
                runTimeOutService();
            }
        }
    }

    @Override
    public void onAuthenticationHelp(int helpMsgId,
                                     CharSequence helpString) {
        if (helpMsgId == 0){
            return;
        }
        logThis("onAuthenticationHelp called");
        logThis("error: " + FahErrorType.getErrorNameByCode(FahErrorType.HELP_ERROR_BASE + helpMsgId) +
                " (" + helpString + ")");

        if (mListener != null) {
            mListener.get().onFingerprintStatus(
                    false,
                    FahErrorType.HELP_ERROR_BASE + helpMsgId,
                    helpString);
        }
    }

    @Override
    public void onAuthenticationFailed() {
        logThis("AUTH_NOT_RECOGNIZED");
        mTryCount--;
        logThis("try #" + String.valueOf(mMaxTryCount - mTryCount));
        if (mTryCount > 0) {
            if (mListener != null) {
                mListener.get().onFingerprintStatus(
                        false,
                        FahErrorType.Auth.AUTH_NOT_RECOGNIZED,
                        mContext.getString(R.string.FINGERPRINT_NOT_RECOGNIZED));
            }
        }
        if (mMaxTryCount < DEFAULT_TRY_COUNT && mTryCount <= 0){
            mListener.get().onFingerprintStatus(
                    false,
                    FahErrorType.Auth.AUTH_TO_MANY_TRIES,
                    getToManyTriesErrorStr());
            runTimeOutService();
        }
    }

    @Override
    public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
        logThis("onAuthenticationSucceeded");
        if (mListener != null) {
            mListener.get().onFingerprintStatus(true, -1, "");
        }
    }


    public boolean startListening() {
        if (mTimeOutLeft > 0 || !canListen(true) || !initCipher()) {
            mIsListening = false;
        } else {
            mAfterStartListenTimeOut = true;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mAfterStartListenTimeOut = false;
                }
            }, 200);
            mCancellationSignal = new CancellationSignal();
            mSelfCancelled = false;
            // The line below prevents the false positive inspection from Android Studio
            // noinspection ResourceType
            mFingerprintManager
                    .authenticate(mCryptoObject, mCancellationSignal, 0 /* flags */, this, null);
            if (mListener != null) {
                mListener.get().onFingerprintListening(true, 0);
            }
            mIsListening = true;
        }
        return mIsListening;
    }

    public boolean stopListening() {
        if (mCancellationSignal != null) {
            mSelfCancelled = true;
            mCancellationSignal.cancel();
            mCancellationSignal = null;
            mIsListening = false;
        }
        return mIsListening;
    }

    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(KEY_MAX_TRY_COUNT, mMaxTryCount);
        outState.putInt(KEY_TRY_COUNT, mTryCount);
        outState.putLong(FahTimeOutService.KEY_TRY_TIME_OUT, mTryTimeOut);
        outState.putLong(KEY_TIME_OUT_LEFT, mTimeOutLeft);
        outState.putBoolean(KEY_LOGGING_ENABLE, mLoggingEnable);
        outState.putString(KEY_SECURE_KEY_NAME, mKeyName);
        outState.putBoolean(KEY_IS_LISTENING, mIsListening);

        registerBroadcast(false);
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        mMaxTryCount = savedInstanceState.getInt(KEY_MAX_TRY_COUNT);
        mTryCount = savedInstanceState.getInt(KEY_TRY_COUNT);
        mTryTimeOut = savedInstanceState.getLong(FahTimeOutService.KEY_TRY_TIME_OUT);
        mTimeOutLeft = savedInstanceState.getLong(KEY_TIME_OUT_LEFT);
        mLoggingEnable = savedInstanceState.getBoolean(KEY_LOGGING_ENABLE);
        mKeyName = savedInstanceState.getString(KEY_SECURE_KEY_NAME);
        mIsListening = savedInstanceState.getBoolean(KEY_IS_LISTENING, false);

        if (mTimeOutLeft > 0 && mListener.get() != null){
            mListener.get().onFingerprintListening(false, mTimeOutLeft);
        }

        registerBroadcast(true);
    }

    public void onDestroy(){
        mContext = null;
        mFingerprintManager = null;
        keyguardManager = null;
        mKeyStore = null;
        mKeyGenerator = null;
        mCipher = null;
        mCryptoObject = null;
        mListener.clear();
        mListener = null;
        mKeyName = null;
        mTimeOutIntent = null;
    }

    public boolean isListening(){
        return mIsListening;
    }

    public int getTryCountLeft(){
        return mTryCount;
    }

    public boolean canListen(boolean showError){
        if (!isSecureComponentsInit(showError)){
            return false;
        }

        if (isPermissionNeeded(showError)){
            return false;
        }
        if (!isHardwareEnabled()) {
            if (mListener != null && showError) {
                mListener.get().onFingerprintStatus(
                        false,
                        FahErrorType.General.HARDWARE_DISABLED,
                        mContext.getString(R.string.HARDWARE_DISABLED));
            }
            logThis("canListen failed. reason: " +
                    mContext.getString(R.string.HARDWARE_DISABLED));
            return false;
        }

        if (!keyguardManager.isKeyguardSecure()) {
            if (mListener != null && showError) {
                mListener.get().onFingerprintStatus(
                        false,
                        FahErrorType.General.LOCK_SCREEN_DISABLED,
                        mContext.getString(R.string.LOCK_SCREEN_DISABLED));
            }
            logThis("canListen failed. reason: " +
                    mContext.getString(R.string.LOCK_SCREEN_DISABLED));
            return false;
        }

        return true;
    }

    public boolean isHardwareEnabled() {
        if (isPermissionNeeded(false)){
            throw new SecurityException("Missing 'USE_FINGERPRINT' permission!");
        }
        return mFingerprintManager.isHardwareDetected();
    }

    public boolean isFingerprintEnrolled(){
        return isFingerprintEnrolled(false);
    }

    public long getTimeOutLeft(){
        return mTimeOutLeft;
    }

    private boolean isSecureComponentsInit(boolean showError){
        logThis("isSecureComponentsInit start");
        if (isFingerprintEnrolled(showError) && !mSecureElementsReady) {
            try {
                mCipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/"
                        + KeyProperties.BLOCK_MODE_CBC + "/"
                        + KeyProperties.ENCRYPTION_PADDING_PKCS7);
            } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
                throw new RuntimeException("Failed to get an instance of Cipher", e);
            }

            try {
                mKeyStore = KeyStore.getInstance("AndroidKeyStore");
            } catch (Exception e) {
                throw new RuntimeException("create keyStore failed", e);
            }
            try {
                mKeyGenerator = KeyGenerator
                        .getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
            } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
                throw new RuntimeException("Failed to get an instance of KeyGenerator", e);
            }

            try {
                mKeyStore.load(null);
                // Set the alias of the entry in Android KeyStore where the key will appear
                // and the constrains (purposes) in the constructor of the Builder

                KeyGenParameterSpec.Builder builder = new KeyGenParameterSpec.Builder(mKeyName,
                        KeyProperties.PURPOSE_ENCRYPT |
                                KeyProperties.PURPOSE_DECRYPT)
                        .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                        // Require the user to authenticate with a fingerprint to authorize every use
                        // of the key
                        .setUserAuthenticationRequired(true)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7);

                // This is a workaround to avoid crashes on devices whose API level is < 24
                // because KeyGenParameterSpec.Builder#setInvalidatedByBiometricEnrollment is only
                // visible on API level +24.
                // Ideally there should be a compat library for KeyGenParameterSpec.Builder but
                // which isn't available yet.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    builder.setInvalidatedByBiometricEnrollment(true);
                }
                try {
                    mKeyGenerator.init(builder.build());
                    mKeyGenerator.generateKey();
                } catch (RuntimeException e){
                    mSecureElementsReady = false;
                    logThis("isSecureComponentsInit failed. Reason: " + e.getMessage());
                    return false;
                }
            } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException
                    | CertificateException | IOException e) {
                throw new RuntimeException(e);
            }
            mSecureElementsReady = true;
        }
        logThis("mSecureElementsReady = " + mSecureElementsReady);
        return mSecureElementsReady;
    }

    private void initAdditionalComponents(){
        mShp = mContext.getSharedPreferences(mContext.getString(R.string.fah_app_name), Context.MODE_PRIVATE);
        mEditor = mShp.edit();

        mTryCount = mMaxTryCount;

        mTryTimeOut = mTryTimeOutDefault;
    }

    private boolean initCipher() {
        try {
            mKeyStore.load(null);
            SecretKey key = (SecretKey) mKeyStore.getKey(mKeyName, null);
            mCipher.init(Cipher.ENCRYPT_MODE, key);
            mCryptoObject = new FingerprintManager.CryptoObject(mCipher);
            return true;
        } catch (KeyPermanentlyInvalidatedException e) {
            logThis("initCipher failed. Reason: " + e.getMessage());
            return false;
        } catch (KeyStoreException | CertificateException | UnrecoverableKeyException | IOException
                | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Failed to init Cipher", e);
        }
    }

    private boolean isPermissionNeeded(boolean showError){
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.USE_FINGERPRINT) ==
                PackageManager.PERMISSION_GRANTED) {
            logThis("USE_FINGERPRINT PERMISSION = PERMISSION_GRANTED");
            return false;
        }
        logThis("USE_FINGERPRINT PERMISSION = PERMISSION_DENIED");
        if (mListener != null && showError) {
            mListener.get().onFingerprintStatus(
                    false,
                    FahErrorType.General.PERMISSION_NEEDED,
                    mContext.getString(R.string.PERMISSION_NEEDED));
        }
        return true;
    }

    private boolean isFingerprintEnrolled(boolean showError){
        if (!mFingerprintManager.hasEnrolledFingerprints()) {
            if (mListener != null && showError) {
                mListener.get().onFingerprintStatus(
                        false,
                        FahErrorType.General.NO_FINGERPRINTS,
                        mContext.getString(R.string.NO_FINGERPRINTS));
            }
            logThis("canListen failed. reason: " +
                    mContext.getString(R.string.NO_FINGERPRINTS));
            mSecureElementsReady = false;
            return false;
        }
        return true;
    }

    private void registerBroadcast(boolean register){
        if (mTimeOutLeft > 0 && register && !mBroadcastRegistered){
            logThis("mBroadcastRegistered = " + true);
            mBroadcastRegistered = true;
            mContext.registerReceiver(timeOutBroadcast, new IntentFilter(FahTimeOutService.TIME_OUT_BROADCAST));
        } else if (!register && mBroadcastRegistered){
            logThis("mBroadcastRegistered = " + false);
            mBroadcastRegistered = false;
            mContext.unregisterReceiver(timeOutBroadcast);
        }
    }

    private void runTimeOutService(){
        logThis("runTimeOutService");
        if (mTimeOutIntent == null) {
            mTimeOutIntent = new Intent(mContext, FahTimeOutService.class);
        }
        mTimeOutLeft = mTryTimeOutDefault;
        registerBroadcast(true);
        mTimeOutIntent.putExtra(FahTimeOutService.KEY_TRY_TIME_OUT, mTryTimeOut);
        mContext.startService(mTimeOutIntent);
        saveTimeOut(System.currentTimeMillis() + mTryTimeOut);
    }

    private void saveTimeOut(long timesLeft){
        mEditor.putLong(KEY_TIME_OUT_LEFT, timesLeft);
        mEditor.commit();
    }

    private boolean isTimerActive(){
        long i = mShp.getLong(KEY_TIME_OUT_LEFT, -1);
        long current = System.currentTimeMillis();
        if (current < i) {
            mTryTimeOut = i - current;
            mTimeOutLeft = mTryTimeOut;
            logThis("isTimeOutActive = " + true);
            return true;
        }
        logThis("isTimeOutActive = " + false);
        return false;
    }

    private void saveToManyTriesStr(String toManyTries){
        mEditor.putString(KEY_TO_MANY_TRIES_ERROR, toManyTries);
        mEditor.commit();
    }

    private String getToManyTriesErrorStr(){
        return mShp.getString(KEY_TO_MANY_TRIES_ERROR, mContext.getString(R.string.AUTH_TO_MANY_TRIES));
    }

    private void logThis(String mess){
        if (mLoggingEnable){
            Log.d(FingerprintAuthHelper.TAG, mess);
        }
    }

    private final BroadcastReceiver timeOutBroadcast = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mTimeOutLeft = intent.getLongExtra(KEY_TIME_OUT_LEFT, -1);

            logThis("mTimeOutLeft = " + String.valueOf(mTimeOutLeft/1000) + " sec");

            if (mTimeOutLeft > 0 && mListener != null) {
                mListener.get().onFingerprintListening(false, mTimeOutLeft);
                saveTimeOut(System.currentTimeMillis() + mTimeOutLeft);
            } else if (mTimeOutLeft <= 0){
                registerBroadcast(false);
                saveTimeOut(-1);
                mTryTimeOut = mTryTimeOutDefault;
                startListening();
                logThis("startListening after timeout");
            }
        }
    };
}
