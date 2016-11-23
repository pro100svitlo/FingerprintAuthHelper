package com.pro100svitlo.fingerprintAuthHelper

/**
 * Created by pro100svitlo on 10/4/16.
 */
object FahErrorType {

    var HELP_ERROR_BASE = 100
    var AUTH_ERROR_BASE = 200

    interface General {
        companion object {
            val PERMISSION_NEEDED = -100
            val LOCK_SCREEN_DISABLED = -101
            val NO_FINGERPRINTS = -102
            val HARDWARE_DISABLED = -104
        }
    }

    interface Help {
        companion object {
            val HELP_SCANNED_PARTIAL = 101
            val HELP_INSUFFICIENT = 102
            val HELP_SCANNER_DIRTY = 103
            val HELP_MOVE_TO_SLOW = 104
            val HELP_MOVE_TO_FAST = 105
        }
    }

    interface Auth {
        companion object {
            val AUTH_NOT_RECOGNIZED = 208
            val AUTH_UNAVAILABLE = 201
            val AUTH_UNABLE_TO_PROCESS = 202
            val AUTH_TIMEOUT = 203
            val AUTH_NO_SPACE = 204
            val AUTH_CANCELED = 205
            val AUTH_TO_MANY_TRIES = 207
        }
    }

    fun getErrorNameByCode(fahError: Int): String {
        when (fahError) {
            General.PERMISSION_NEEDED -> return "PERMISSION_NEEDED"
            General.LOCK_SCREEN_DISABLED -> return "LOCK_SCREEN_DISABLED"
            General.NO_FINGERPRINTS -> return "NO_FINGERPRINTS"
            General.HARDWARE_DISABLED -> return "HARDWARE_DISABLED"

            Help.HELP_SCANNED_PARTIAL -> return "HELP_SCANNED_PARTIAL"
            Help.HELP_INSUFFICIENT -> return "HELP_INSUFFICIENT"
            Help.HELP_SCANNER_DIRTY -> return "HELP_SCANNER_DIRTY"
            Help.HELP_MOVE_TO_SLOW -> return "HELP_MOVE_TO_SLOW"
            Help.HELP_MOVE_TO_FAST -> return "HELP_MOVE_TO_FAST"

            Auth.AUTH_NOT_RECOGNIZED -> return "AUTH_NOT_RECOGNIZED"
            Auth.AUTH_UNAVAILABLE -> return "AUTH_UNAVAILABLE"
            Auth.AUTH_UNABLE_TO_PROCESS -> return "AUTH_UNABLE_TO_PROCESS"
            Auth.AUTH_TIMEOUT -> return "AUTH_TIMEOUT"
            Auth.AUTH_NO_SPACE -> return "AUTH_NO_SPACE"
            Auth.AUTH_CANCELED -> return "AUTH_CANCELED"
            Auth.AUTH_TO_MANY_TRIES -> return "AUTH_TO_MANY_TRIES"

            else -> return "UNKNOWN ERROR"
        }
    }

}
