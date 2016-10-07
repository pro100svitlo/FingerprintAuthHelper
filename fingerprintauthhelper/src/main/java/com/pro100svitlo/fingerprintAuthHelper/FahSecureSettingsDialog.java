package com.pro100svitlo.fingerprintAuthHelper;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

/**
 * Created by pro100svitlo on 8/26/16.
 */
public class FahSecureSettingsDialog {

    public static class Builder{

        private Context mContext;
        private FingerprintAuthHelper mFAH;
        private String mTitle;
        private String mMessage;
        private String mPositive;
        private String mNegative;

        public Builder(Context c, FingerprintAuthHelper helper){
            if (c instanceof Activity){
                mContext = c;
            } else {
                throw new IllegalArgumentException("Context for FahSecureSettingsDialog must be " +
                        "instance of Activity for correct styling");
            }
            mFAH = helper;
        }

        public Builder setTitle(String title){
            mTitle = title;
            return this;
        }

        public Builder setTitle(int resId){
            mTitle = mContext.getString(resId);
            return this;
        }

        public Builder setMessage(String message){
            mMessage = message;
            return this;
        }

        public Builder setMessage(int resId){
            mMessage = mContext.getString(resId);
            return this;
        }

        public Builder setPostisive(String positive){
            mPositive = positive;
            return this;
        }

        public Builder setPostisive(int resId){
            mPositive = mContext.getString(resId);
            return this;
        }

        public Builder setNegative(String negative){
            mNegative = negative;
            return this;
        }

        public Builder setNegative(int resId){
            mNegative = mContext.getString(resId);
            return this;
        }

        public FahSecureSettingsDialog build(){
            if (mTitle == null){
                mTitle = mContext.getString(R.string.fah_dialog_openSecureSettings_title);
            }

            if (mMessage == null){
                mMessage = mContext.getString(R.string.fah_dialog_openSecureSettings_message);
            }

            if (mPositive == null){
                mPositive = mContext.getString(R.string.fah_dialog_openSecureSettings_pos);
            }

            if (mNegative == null){
                mNegative = mContext.getString(R.string.fah_dialog_openSecureSettings_neg);
            }

            return new FahSecureSettingsDialog(this);
        }
    }

    private final Context mContext;
    private final FingerprintAuthHelper mFAH;
    private AlertDialog mDialog;

    private final String mTitle;
    private final String mMessage;
    private final String mPositive;
    private final String mNegative;

    private FahSecureSettingsDialog(Builder b){
        mContext = b.mContext;
        mFAH = b.mFAH;

        mTitle = b.mTitle;
        mMessage = b.mMessage;
        mPositive = b.mPositive;
        mNegative = b.mNegative;

        create();
    }

    public void show(){
        mDialog.show();
    }

    public void setMessage(String mess){
        mDialog.setMessage(mess);
    }

    public void setMessage(int resId){
        mDialog.setMessage(mContext.getString(resId));
    }

    private void create(){
        mDialog = new AlertDialog.Builder(mContext)
                .setTitle(mTitle)
                .setMessage(mMessage)
                .setNegativeButton(mNegative, null)
                .setPositiveButton(mPositive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mFAH.openSecuritySettings();
                    }
                })
                .create();
    }
}
