@file:Suppress("unused")

package com.pro100svitlo.fingerprintAuthHelper

import android.app.Activity
import android.content.Context
import android.support.annotation.StringRes
import android.support.v7.app.AlertDialog

/**
 * Created by pro100svitlo on 8/26/16.
 */
class FahSecureSettingsDialog private constructor(b: FahSecureSettingsDialog.Builder) {

    class Builder(c: Context, internal val fah: FingerprintAuthHelper) {

        internal var context: Context
        internal var title: String? = null
        internal var message: String? = null
        internal var positive: String? = null
        internal var negative: String? = null

        init {
            if (c is Activity) {
                context = c
            } else {
                throw IllegalArgumentException("Context for FahSecureSettingsDialog must be " + "instance of Activity for correct styling")
            }
        }

        fun setTitle(title: String): Builder {
            this.title = title
            return this
        }

        fun setTitle(@StringRes resId: Int): Builder {
            title = context.getString(resId)
            return this
        }

        fun setMessage(message: String): Builder {
            this.message = message
            return this
        }

        fun setMessage(@StringRes resId: Int): Builder {
            message = context.getString(resId)
            return this
        }

        fun setPositive(positive: String): Builder {
            this.positive = positive
            return this
        }

        @Deprecated("Use setPositive(positive: String) instead.", ReplaceWith("setPositive(positive)"))
        fun setPostisive(positive: String): Builder = setPositive(positive)

        fun setPositive(resId: Int): Builder {
            positive = context.getString(resId)
            return this
        }

        @Deprecated("Use setPositive(resId: Int) instead.", ReplaceWith("setPositive(resId)"))
        fun setPostisive(@StringRes resId: Int): Builder = setPositive(resId)

        fun setNegative(negative: String): Builder {
            this.negative = negative
            return this
        }

        fun setNegative(@StringRes resId: Int): Builder {
            negative = context.getString(resId)
            return this
        }

        fun build() = FahSecureSettingsDialog(this)
    }

    private val context = b.context
    private val fah = b.fah
    private val dialog: AlertDialog by lazy { create() }

    private val title = b.title ?: context.getString(R.string.fah_dialog_openSecureSettings_title)
    private val message = b.message ?: context.getString(R.string.fah_dialog_openSecureSettings_message)
    private val positive = b.positive ?: context.getString(R.string.fah_dialog_openSecureSettings_pos)
    private val negative = b.negative ?: context.getString(R.string.fah_dialog_openSecureSettings_neg)

    fun show() {
        dialog.show()
    }

    fun setMessage(mess: String) {
        dialog.setMessage(mess)
    }

    fun setMessage(resId: Int) {
        dialog.setMessage(context.getString(resId))
    }

    private fun create(): AlertDialog {
        return AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setNegativeButton(negative, null)
                .setPositiveButton(positive) { _, _ -> fah.openSecuritySettings() }
                .create()
    }
}
