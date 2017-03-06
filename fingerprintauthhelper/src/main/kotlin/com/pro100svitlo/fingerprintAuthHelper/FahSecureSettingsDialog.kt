package com.pro100svitlo.fingerprintAuthHelper

import android.app.Activity
import android.content.Context
import android.support.v7.app.AlertDialog

/**
 * Created by pro100svitlo on 8/26/16.
 */
class FahSecureSettingsDialog private constructor(b: FahSecureSettingsDialog.Builder) {

    class Builder(c: Context, internal val mFAH: FingerprintAuthHelper) {

        internal var mContext: Context? = null
        internal var mTitle: String? = null
        internal var mMessage: String? = null
        internal var mPositive: String? = null
        internal var mNegative: String? = null

        init {
            if (c is Activity) {
                mContext = c
            } else {
                throw IllegalArgumentException("Context for FahSecureSettingsDialog must be " + "instance of Activity for correct styling")
            }
        }

        fun setTitle(title: String): Builder {
            mTitle = title
            return this
        }

        fun setTitle(resId: Int): Builder {
            mTitle = mContext!!.getString(resId)
            return this
        }

        fun setMessage(message: String): Builder {
            mMessage = message
            return this
        }

        fun setMessage(resId: Int): Builder {
            mMessage = mContext!!.getString(resId)
            return this
        }

        fun setPostisive(positive: String): Builder {
            mPositive = positive
            return this
        }

        fun setPostisive(resId: Int): Builder {
            mPositive = mContext!!.getString(resId)
            return this
        }

        fun setNegative(negative: String): Builder {
            mNegative = negative
            return this
        }

        fun setNegative(resId: Int): Builder {
            mNegative = mContext!!.getString(resId)
            return this
        }

        fun build(): FahSecureSettingsDialog {
            if (mTitle == null) {
                mTitle = mContext!!.getString(R.string.fah_dialog_openSecureSettings_title)
            }

            if (mMessage == null) {
                mMessage = mContext!!.getString(R.string.fah_dialog_openSecureSettings_message)
            }

            if (mPositive == null) {
                mPositive = mContext!!.getString(R.string.fah_dialog_openSecureSettings_pos)
            }

            if (mNegative == null) {
                mNegative = mContext!!.getString(R.string.fah_dialog_openSecureSettings_neg)
            }

            return FahSecureSettingsDialog(this)
        }
    }

    private val mContext: Context
    private val mFAH: FingerprintAuthHelper
    private var mDialog: AlertDialog? = null

    private val mTitle: String
    private val mMessage: String
    private val mPositive: String
    private val mNegative: String

    init {
        mContext = b.mContext!!
        mFAH = b.mFAH

        mTitle = b.mTitle!!
        mMessage = b.mMessage!!
        mPositive = b.mPositive!!
        mNegative = b.mNegative!!

        create()
    }

    fun show() {
        mDialog!!.show()
    }

    fun setMessage(mess: String) {
        mDialog!!.setMessage(mess)
    }

    fun setMessage(resId: Int) {
        mDialog!!.setMessage(mContext.getString(resId))
    }

    private fun create() {
        mDialog = AlertDialog.Builder(mContext)
                .setTitle(mTitle)
                .setMessage(mMessage)
                .setNegativeButton(mNegative, null)
                .setPositiveButton(mPositive) { dialogInterface, i -> mFAH.openSecuritySettings() }
                .create()
    }
}
