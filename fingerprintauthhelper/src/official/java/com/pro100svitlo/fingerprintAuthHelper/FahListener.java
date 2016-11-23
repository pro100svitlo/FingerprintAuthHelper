package com.pro100svitlo.fingerprintAuthHelper;

/**
 * Created by pro100svitlo on 11/23/16.
 */

public interface FahListener {
    void onFingerprintStatus(boolean authSuccessful, int errorType, CharSequence errorMess);
    void onFingerprintListening(boolean listening, long milliseconds);
}
