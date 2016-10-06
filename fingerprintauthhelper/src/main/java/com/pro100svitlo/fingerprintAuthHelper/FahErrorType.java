package com.pro100svitlo.fingerprintAuthHelper;

/**
 * Created by pro100svitlo on 10/4/16.
 */
public class FahErrorType {

    public static int HELP_ERROR_BASE = 100;
    public static int AUTH_ERROR_BASE = 200;

    public interface General{
        int PERMISSION_NEEDED = -100;
        int LOCK_SCREEN_DISABLED = -101;
        int NO_FINGERPRINTS = -102;
        int HARDWARE_DISABLED = -104;
    }

    public interface Help{
        int HELP_SCENED_PARTIAL = 101;
        int HELP_SCANNER_DIRTY = 102;
        int HELP_MOVE_TO_SLOW = 103;
        int HELP_MOVE_TO_FAST = 104;
    }

    public interface Auth{
        int AUTH_NOT_RECOGNIZED = 208;
        int AUTH_UNAVAILABLE = 201;
        int AUTH_UNABLE_TO_PROCESS = 202;
        int AUTH_TIMEOUT = 203;
        int AUTH_NO_SPACE = 204;
        int AUTH_CANCELED = 205;
        int AUTH_TO_MANY_TRIES = 207;
    }

    public static String getErrorNameByCode(int fahError){
        switch (fahError){
            case General.PERMISSION_NEEDED:
                return "PERMISSION_NEEDED";
            case General.LOCK_SCREEN_DISABLED:
                return "LOCK_SCREEN_DISABLED";
            case General.NO_FINGERPRINTS:
                return "NO_FINGERPRINTS";
            case General.HARDWARE_DISABLED:
                return "HARDWARE_DISABLED";

            case Help.HELP_SCENED_PARTIAL:
                return "HELP_SCENED_PARTIAL";
            case Help.HELP_SCANNER_DIRTY:
                return "HELP_SCANNER_DIRTY";
            case Help.HELP_MOVE_TO_SLOW:
                return "HELP_MOVE_TO_SLOW";
            case Help.HELP_MOVE_TO_FAST:
                return "HELP_MOVE_TO_FAST";

            case Auth.AUTH_NOT_RECOGNIZED:
                return "AUTH_NOT_RECOGNIZED";
            case Auth.AUTH_UNAVAILABLE:
                return "AUTH_UNAVAILABLE";
            case Auth.AUTH_UNABLE_TO_PROCESS:
                return "AUTH_UNABLE_TO_PROCESS";
            case Auth.AUTH_TIMEOUT:
                return "AUTH_TIMEOUT";
            case Auth.AUTH_NO_SPACE:
                return "AUTH_NO_SPACE";
            case Auth.AUTH_CANCELED:
                return "AUTH_CANCELED";
            case Auth.AUTH_TO_MANY_TRIES:
                return "AUTH_TO_MANY_TRIES";

            default:
                return "UNKNOWN ERROR";
        }
    }

}
