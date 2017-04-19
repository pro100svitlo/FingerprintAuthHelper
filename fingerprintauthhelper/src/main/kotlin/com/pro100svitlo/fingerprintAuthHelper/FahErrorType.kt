package com.pro100svitlo.fingerprintAuthHelper

/**
 * Created by pro100svitlo on 10/4/16.
 */
object FahErrorType {

    const val HELP_ERROR_BASE = 100
    const val AUTH_ERROR_BASE = 200

    object General {
        const val PERMISSION_NEEDED = -100
        const val LOCK_SCREEN_DISABLED = -101
        const val NO_FINGERPRINTS = -102
        const val HARDWARE_DISABLED = -104
    }

    object Help {
        const val HELP_SCANNED_PARTIAL = 101
        const val HELP_INSUFFICIENT = 102
        const val HELP_SCANNER_DIRTY = 103
        const val HELP_MOVE_TO_SLOW = 104
        const val HELP_MOVE_TO_FAST = 105
    }

    object Auth {
        const val AUTH_NOT_RECOGNIZED = 208
        const val AUTH_UNAVAILABLE = 201
        const val AUTH_UNABLE_TO_PROCESS = 202
        const val AUTH_TIMEOUT = 203
        const val AUTH_NO_SPACE = 204
        const val AUTH_CANCELED = 205
        const val AUTH_TOO_MANY_TRIES = 207
        @Deprecated("Use AUTH_TOO_MANY_TRIES instead.", ReplaceWith("FahErrorType.Auth.AUTH_TOO_MANY_TRIES"))
        const val AUTH_TO_MANY_TRIES = 207
    }

    fun getErrorNameByCode(fahError: Int): String {
        return when (fahError) {
            General.PERMISSION_NEEDED -> "PERMISSION_NEEDED"
            General.LOCK_SCREEN_DISABLED -> "LOCK_SCREEN_DISABLED"
            General.NO_FINGERPRINTS -> "NO_FINGERPRINTS"
            General.HARDWARE_DISABLED -> "HARDWARE_DISABLED"

            Help.HELP_SCANNED_PARTIAL -> "HELP_SCANNED_PARTIAL"
            Help.HELP_INSUFFICIENT -> "HELP_INSUFFICIENT"
            Help.HELP_SCANNER_DIRTY -> "HELP_SCANNER_DIRTY"
            Help.HELP_MOVE_TO_SLOW -> "HELP_MOVE_TO_SLOW"
            Help.HELP_MOVE_TO_FAST -> "HELP_MOVE_TO_FAST"

            Auth.AUTH_NOT_RECOGNIZED -> "AUTH_NOT_RECOGNIZED"
            Auth.AUTH_UNAVAILABLE -> "AUTH_UNAVAILABLE"
            Auth.AUTH_UNABLE_TO_PROCESS -> "AUTH_UNABLE_TO_PROCESS"
            Auth.AUTH_TIMEOUT -> "AUTH_TIMEOUT"
            Auth.AUTH_NO_SPACE -> "AUTH_NO_SPACE"
            Auth.AUTH_CANCELED -> "AUTH_CANCELED"
            Auth.AUTH_TOO_MANY_TRIES -> "AUTH_TOO_MANY_TRIES"

            else -> "UNKNOWN ERROR"
        }
    }

}
