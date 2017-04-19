# Documentation

1. [Init](#init)
2. [Main methods](#main-methods)
3. [Additional methods](#additional-methods)
4. [Callbacks](#callbacks)
5. [FahSecureSettingsDialog](#fahsecuresettingsdialog)

### Init

```sh
mFAH = new FingerprintAuthHelper.Builder(this, this) 
                    //necessarily, (Context, FahListener)
                .setKeyName("keyName")                   
                    //optional, used for KeyGenParameterSpec and SecretKey
                .setLoggingEnable(true)                  
                    //optional, false by default 
                .setTryTimeOut(2 * 60 * 1000)
                    //optional, 45000 milliseconds by default, in case you need custom timeout,
                    //must be >= 45000 milliseconds
                .build();
```

### Main methods
```sh
startListening()
    // method must be called in onResume or when you need to start listening
    // (for example inside onClick)
    // return true if device is listening
    // return false if device is not listening. Reason can be different. See logs for more details

stopListening()
    // method must be called in onPause or when you need to stop listening
    // return true if device is still listening
    // return false if device is not listening

onDestroy()
    // method must be called in onDestroy or when you leave activity/fragment/dialog
    // return true if onDestrow was successful
    // return false if device does not support Fingerprint technology
```

### Additional methods
```sh
onSaveInstanceState()
    // call this method to save data in case you need to support screen rotation
    // return true if onSaveInstanceState was successful
    // return false if device does not support Fingerprint technology

onRestoreInstanceState()
    // call this method to restore data in case you need to support screen rotation
    // return true if onRestoreInstanceState was successful
    // return false if device does not support Fingerprint technology

setCanListenByUser(boolean canListen)
    // in case you need to disable usage of fingerprint auth
    // (for example user disables it in your app settings)

canListenByUser()
    // by default true
    // return current boolean value

canListen()
    // in case you just need to know if device can listen
    // return true if device can listen
    // return false if device can not listen. Reason can be different. See logs for more details
    // (for example if setCanListenByUser(false), canListen() will return false)

isListening()
    // in case you just need to know if devise listening for fingerprint rigth now
    // return true if device is listening
    // return false if device is not listening. Reason can be different. See logs for more details

getTriesCountLeft()
    // in case you need to know how many tries are left
    // maxCount - default 5, every fingerprintError decrease counter.
    // when startListening, stopListening, authSuccessful - maxCount set to default
    // return int

cleanTimeOut()
    // in case you want to authenticate with other method after 5 unsuccessful tries fingerprintAuth,
    // and you need "drop" fingerprint timeout
    // if timeOut service is running and more than default timeOut (45 sec) have passed after
    // 5 scan fails, then method will stop timeoutservice;
    // If less then default timeOut have passed, than method will setUp leftTime as difference
    // between default and passed time.

getTimeOutLeft()
    // in case there were too many tries and timer is turned on 
    // and you need to know how many milliseconds are left
    // return long

isHardwareEnable()
    // method that checks if device supports fingerprint technology and
    // checks for fingerprint hardware
    // return true if everything is ok and hardware is enabled
    // return false if hardware is disabled or if device does not support Fingerprint technology

isFingerprintEnrolled()
    // method that checks if device has enrolled fingerprints
    // return true if at least one fingerprint enrolled
    // return false if no fingerprints enrolled or if device does not support Fingerprint technology

openSecuritySettings()
    // method, that opens android security settings activity, 
    // where user can change fingerprint settings
    
showSecuritySettingsDialog()
    // method, that opens dialog, with explanations why needs to set fingerprint and lock screen
    // also with propose go to settingsActivity
```

### Callbacks:
Your activity or fragment must implement FahListener;
```sh
@Override
public void onFingerprintStatus(boolean authSuccessful, int errorType, CharSequence errorMess) {
    // authSuccessful - boolean that shows auth status
    // errorType - if auth was failed, you can catch error type
    // errorMess - if auth was failed, errorMess will tell you (and user) the reason

    if (authSuccessful){
        // do some stuff here in case auth was successful
        // for example open another activity
    } else if (mFAH != null){
        // do some stuff here in case auth failed
        // there are three types of possible reasons: General, Auth, Help
        
        switch (errorType){
          //General errors
          case FahErrorType.General.PERMISSION_NEEDED:
              //in case permission is needed
              break;
          case FahErrorType.General.HARDWARE_DISABLED:
              // in case there is no hardware for fingerprint scaning or it is disabled
              break;
          case FahErrorType.General.LOCK_SCREEN_DISABLED:
              // in case the user did not set up lock screen (necessary for fingerprint auth!)
          case FahErrorType.General.NO_FINGERPRINTS:
              // in case the user has not set up any fingerprint yet (necessary for fingerprint auth!)
            
              // you can propose user to go to settings activity 
              // and set up lockScreen or Fingerprint
              // you also can use [FahSecureSettingsDialog](#FahSecureSettingsDialog)
              break;
              
          //Auth errors - errors of authentication
          case FahErrorType.Auth.AUTH_NOT_RECOGNIZED:
              // in case of wrong finger or finger was not recognized
              break;
          case FahErrorType.Auth.AUTH_UNAVAILABLE:
              // The hardware is unavailable. Try again later.
              break;
          case FahErrorType.Auth.AUTH_UNABLE_TO_PROCESS:
              // Error state returned when the sensor was unable to process the current image.
              break;
          case FahErrorType.Auth.AUTH_TIMEOUT:
              // Error state returned when the current request has been running too long. This is intended to
              // prevent programs from waiting for the fingerprint sensor indefinitely. The timeout is
              // platform and sensor-specific, but is generally on the order of 30 seconds.
              break;
          case FahErrorType.Auth.AUTH_NO_SPACE:
              // Error state returned for operations like enrollment; the operation cannot be completed
              // because there is not enough storage remaining to complete the operation.
              break;
          case FahErrorType.Auth.AUTH_CANCELED:
              // The operation was canceled because the fingerprint sensor is unavailable. For example,
              // this may happen when the user is switched, the device is locked or another pending operation
              // prevents or disables it.
              break;
          case FahErrorType.Auth.AUTH_TO_MANY_TRIES:
              // The operation was canceled because the API is locked out due to too many attempts.
              break;
              
          //Help errors - called when a recoverable error has been encountered during authentication. 
          // The errorMess is provided to give the user guidance for what went wrong
          case FahErrorType.Help.HELP_SCANNED_PARTIAL:
              // Only a partial fingerprint image was detected. During enrollment, the user should be
              // informed on what needs to happen to resolve this problem, e.g. "press firmly on sensor."
              break;
          case FahErrorType.Help.HELP_INSUFFICIENT:
              // The fingerprint image was too noisy to process due to a detected condition (i.e. dry skin) or
              // a possibly dirty sensor
              break;
          case FahErrorType.Help.HELP_SCANNER_DIRTY:
              // The fingerprint image was too noisy due to suspected or detected dirt on the sensor.
              // For example, it is reasonable return this after multiple
              // or actual detection of dirt on the sensor (stuck pixels, swaths, etc.).
              // The user is expected to take action to clean the sensor when this is returned.
              break;
          case FahErrorType.Help.HELP_MOVE_TO_SLOW:
              // The fingerprint image was unreadable due to lack of motion. This is most appropriate for
              // linear array sensors that require a swipe motion.
              break;
          case FahErrorType.Help.HELP_MOVE_TO_FAST:
              // The fingerprint image was incomplete due to quick motion. While mostly appropriate for
              // linear array sensors,  this could also happen if the finger was moved during acquisition.
              // The user should be asked to move the finger slower (linear) or leave the finger on the sensor
              // longer.
              break;
        }
    }
}

@Override
public void onFingerprintListening(boolean listening, long milliseconds) {
  // listening - status of fingerprint listen process
  // milliseconds - timeout value, will be > 0, if listening = false & errorType = AUTH_TO_MANY_TRIES
  
  if (listening){
      //add some code here
  } else {
      //add some code here
  }
  if (milliseconds > 0) {
      //if u need, u can show timeout for user
  }
}
```

### FahSecureSettingsDialog
If u need customize FahSecureSettingsDialog:
```sh
mSecureSettingsDialog = new FahSecureSettingsDialog
                              .Builder(this, mFAH) (context (instance of activity), fingerprintAuthHelper)
                              .setTitle(String)
                              .setTitle(stringResId)
                                  // default: 
                                  // "Attention"
                              .setMessage(String)
                              .setMessage(strintResId)
                                  // default: 
                                  // If You wish to use Fingerprint Authentication, please verify:\n
                                  // 1. that the lock screen is protected by a PIN, 
                                  //    pattern or password (fingerprints can only
                                  //    works if the lock screen has been secured).\n
                                  // 2. that at least one fingerprint has been registered on the device.\n\n
                                  // Wish to open Secure Settings to verify?
                              .setPositive(String)
                              .setPositive(stringResId)
                                  // default: 
                                  // Settings
                              .setNegative(String)
                              .setNegative(stringResId)
                                  // default: 
                                  // Dismiss
                              .build();
                              
mSecureSettingsDialog.show()
    // show dialog
    
mSecureSettingsDialog.setMessage(String)
mSecureSettingsDialog.setMessage(stringResId)
    // in case you need to change message after create dialog 
```

PositiveBtnClick will open Security Settings Activity:
```sh
mFAH.openSecuritySettings();
```

NegativeBtbClick will hide dialog
