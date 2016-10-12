package com.pro100svitlo.fingerprintAuthHelper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import java.lang.ref.WeakReference;

/**
 * Created by pro100svitlo on 10/4/16.
 */
public class FingerprintAuthHelper {

    public static class Builder {

        private Context mContext;
        private WeakReference<FahListener> mListener;
        private String mKeyName = TAG;
        private boolean mLoggingEnable;
        private long mTryTimeOut = 60 * 1000;
        private int mMaxTryCount = 5;

        public Builder(Context c, FahListener l) {
            if (c instanceof Activity){
                mContext = c;
            } else {
                throw new IllegalArgumentException("Context for FingerprintAuthHelper must be " +
                        "instance of Activity");
            }
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

        public Builder setTryTimeOut(long milliseconds) {
            if (milliseconds < mTryTimeOut) {
                throw new IllegalArgumentException("tryTimeout must be more than " + mTryTimeOut + " milliseconds!");
            }
            mTryTimeOut = milliseconds;
            return this;
        }

        public Builder setMaxTryCount(int maxTryCount){
            if (maxTryCount > mMaxTryCount){
                throw new IllegalArgumentException("maxTryCount must be less or equal than " + mMaxTryCount);
            } else if (maxTryCount <= 0){
                throw new IllegalArgumentException("maxTryCount must be more than 0 but less or equal than" + mMaxTryCount);
            }
            mMaxTryCount = maxTryCount;
            return this;
        }

        public FingerprintAuthHelper build() {
            return new FingerprintAuthHelper(this);
        }
    }

    public final static String TAG = FingerprintAuthHelper.class.getSimpleName();

    private FahManager mFahManager;
    private FahSecureSettingsDialog mFahSecurityDialog;
    private Context mContext;

    private int mTryCountLeft;
    private long mTimeOutLeft;
    private boolean mLoggingEnable = false;
    private boolean mCanListenByUser = true;
    private boolean mCanListenBySystem;
    private boolean mIsHardwareEnable;
    private boolean mIsListening;
    private boolean mIsFingerprintEnrolled;

    private FingerprintAuthHelper(Builder b) {
        mContext = b.mContext;
        mLoggingEnable = b.mLoggingEnable;

        if (isSdkVersionOk()){
            mFahManager = new FahManager.Builder(b.mContext, b.mListener.get())
                    .setKeyName(b.mKeyName)
                    .setLoggingEnable(mLoggingEnable)
                    .setTryTimeOut(b.mTryTimeOut)
                    .setMaxTryCount(b.mMaxTryCount)
                    .build();
            mTimeOutLeft = mFahManager.getTimeOutLeft();
            mTryCountLeft = mFahManager.getTryCountLeft();
        }
    }

    public boolean startListening() {
        logThis("startListening called");
        if (mFahManager == null){
            serviceNotEnable("startListening");
            return false;
        } if (!canListenByUser()){
            return false;
        }
        mIsListening = mTimeOutLeft <= 0 && mFahManager.startListening();
        logThis("mIsListening = " + mIsListening);
        return mIsListening;
    }

    public boolean stopListening(){
        logThis("stopListening called");
        if (mFahManager == null){
            serviceNotEnable("stopListening");
            return false;
        } if (!canListenByUser()){
            return false;
        }
        mIsListening = mFahManager.stopListening();
        logThis("mIsListening = " + mIsListening);
        return mIsListening;
    }

    public boolean onSaveInstanceState(Bundle outState) {
        logThis("onSaveInstanceState called");
        if (mFahManager == null){
            serviceNotEnable("onSaveInstanceState");
            return false;
        }
        mFahManager.onSaveInstanceState(outState);
        logThis("onSaveInstanceState successful");
        return true;
    }

    public boolean onRestoreInstanceState(Bundle savedInstanceState) {
        logThis("onRestoreInstanceState called");
        if (mFahManager == null){
            serviceNotEnable("onRestoreInstanceState");
            return false;
        }
        mFahManager.onRestoreInstanceState(savedInstanceState);
        logThis("onRestoreInstanceState successful");
        return true;
    }

    public boolean onDestroy(){
        logThis("onDestroy called");
        mContext = null;
        if (mFahManager == null){
            serviceNotEnable("onDestroy");
            return false;
        }
        mFahManager.onDestroy();
        mFahManager = null;
        logThis("onDestroy successful");
        return true;
    }

    public boolean isListening(){
        logThis("isListening called");
        if (mFahManager == null){
            serviceNotEnable("isListening");
            return false;
        }
        mIsListening = mFahManager.isListening();
        logThis("isListening = " + mIsListening);
        return mIsListening;
    }

    public int getTryCountLeft(){
        logThis("getTryCountLeft called");
        if (mFahManager == null){
            serviceNotEnable("getTryCountLeft");
            return -1;
        }
        mTryCountLeft = mFahManager.getTryCountLeft();
        logThis("mTryCountLeft = " + mTryCountLeft);
        return mTryCountLeft;
    }
    public boolean canListen(boolean showError){
        logThis("canListen called");
        if (mFahManager == null){
            serviceNotEnable("canListen");
            return false;
        }
        canListenByUser();
        mCanListenBySystem = mFahManager.canListen(showError);
        logThis("canListenBySystem = " + mCanListenBySystem);
        logThis("can listen = " + String.valueOf(mCanListenByUser && mCanListenBySystem));
        return mCanListenByUser && mCanListenBySystem;
    }

    public boolean canListenByUser(){
        logThis("canListenByUser called");
        logThis("canListenByUser = " + mCanListenByUser);
        return mCanListenByUser;
    }

    public void setCanListenByUser(boolean canListen){
        logThis("setCanListenByUser called");
        logThis("setCanListenByUser = " + canListen);
        mCanListenByUser = canListen;
    }

    public long getTimeOutLeft(){
        logThis("getTimeOutLeft called");
        if (mFahManager == null){
            serviceNotEnable("getTimeOutLeft");
            return -1;
        }
        mTimeOutLeft = mFahManager.getTimeOutLeft();
        logThis("timeOutLeft = " + mTimeOutLeft + " millisecond");
        return mTimeOutLeft;
    }

    public boolean isHardwareEnable() {
        logThis("isHardwareEnable called");
        if (mFahManager == null){
            serviceNotEnable("isHardwareEnable");
            return false;
        }
        mIsHardwareEnable = mFahManager.isHardwareEnabled();
        logThis("mIsHardwareEnable = " + mIsHardwareEnable);
        return mIsHardwareEnable;
    }

    public boolean isFingerprintEnrolled(){
        if (mFahManager == null){
            serviceNotEnable("isFingerprintEnrolled");
            return false;
        }
        mIsFingerprintEnrolled = mFahManager.isFingerprintEnrolled();
        logThis("mIsFingerprintEnrolled = " + mIsFingerprintEnrolled);
        return mIsFingerprintEnrolled;
    }

    public void openSecuritySettings(){
        logThis("openSecuritySettings called");
        mContext.startActivity(new Intent(Settings.ACTION_SECURITY_SETTINGS));
    }

    public void showSecuritySettingsDialog(){
        if (mFahSecurityDialog == null){
            mFahSecurityDialog = new FahSecureSettingsDialog
                    .Builder(mContext, this)
                    .build();
        }
        mFahSecurityDialog.show();
    }

    private boolean isSdkVersionOk(){
        logThis("isSdkVersionOk called");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            logThis("sdkVersionOk");
            return true;
        }
        Log.d(TAG, "fingerprintAuthHelper cant work with sdk version < 23 (Android M)");
        return false;
    }

    private void logThis(String mess){
        if (mLoggingEnable){
            Log.d(TAG, mess);
        }
    }

    private void serviceNotEnable(String methodName){
        logThis("method '" + methodName + "' can't be finished, because of fingerprintService not enable");
    }

}
