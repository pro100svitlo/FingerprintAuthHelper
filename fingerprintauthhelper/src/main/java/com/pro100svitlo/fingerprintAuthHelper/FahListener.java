package com.pro100svitlo.fingerprintAuthHelper;

/**
 * Created by pro100svitlo on 8/26/16.
 */
public interface FahListener {
    void onFingerprintStatus(boolean authSuccessful, int errorType, CharSequence errorMess);
    void onFingerprintListening(boolean listening, long milliseconds);
}
