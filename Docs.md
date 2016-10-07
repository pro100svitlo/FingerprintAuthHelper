#Documentation

1. [Init](#init)
2. [Main methods](#main-methods)
3. [Additional methods](#additional-methods)
4. [Errors](#errors)

###Init

```sh
mFAH = new FingerprintAuthHelper.Builder(this, this) 
                    //necessarily, (Context, FahListener)
                .setKeyName("keyName")                   
                    //optional, used for KeyGenParameterSpec and SecretKey
                .setLoggingEnable(true)                  
                    //optional, false by default 
                .setTryTimeOut(2 * 60 * 1000)
                    //optional, 60000 milliseconds by default, in case you need custom timeout,
                    //must be >= 60000 milliseconds
                .setMaxTryCount(3)
                    //optional, 5 by default, must be: 0 < MaxTryCount <= 5
                .build();
```

###Main methods
```sh
startListening() 
    // method must be called in onResume or when you need to start listening 
    //(for example inside onClick)
    // return true if device listening 
    // return false if device not listening. Reason can be different. See logs for more details

stopListening() 
    // method must be called in onPause or when you need to stop listening
    // return true if device still listening 
    // return false if device not listening

onDestroy() 
    // method must be called in onDestroy or when you leave activity/fragment/dialog
    // return true if onDestrow was successful
    // return false if devise doesn't support Fingerprint technology
```

###Additional methods
```sh
onSaveInstanceState()
    // call this method to save data in case you need to support screen rotation
    // return true if onSaveInstanceState was successful
    // return false if devise doesn't support Fingerprint technology

onRestoreInstanceState()
    // call this method to restore data in case you need to support screen rotation
    // return true if onRestoreInstanceState was successful
    // return false if devise doesn't support Fingerprint technology

setCanListenByUser(boolean canListen)
    // in case you need to disable usage fingerprint auth 
    // (for example user disable it in your app settings)

canListenByUser()
    // by default true
    // return current boolean value

canListen()
    // in case you just need to know if device can listen for fingerprints
    // return true if device can listen
    // return false if device can't listen. Reason can be different. See logs for more details
    // (for example if setCanListenByUser(false), canListen() will return false)

isListening()
    // in case you just need to know if devise listening for fingerprint rigth now
    // return true if device listening 
    // return false if device not listening. Reason can be different. See logs for more details

getTryCountLeft()
    // in case you need to know how many tries left
    // return int

getTimeOutLeft()
    // in case there are was to meny tries and timer is turned on 
    // and you need to know how many milliseconds are left
    // return long


isHardwareEnable()
    // method that check if device support fingerprint technology and
    // check for fingerprint hardware
    // return true if everything is ok and hardware is enable
    // return false if hardware disable or if devise doesn't support Fingerprint technology

openSecuritySettings()
    // method, that open android security settings activity, 
    // where user can change fingerprint settings
    // for example if there is no saved fingerprints, you can propose user to 
    // go to settingsActivity and set it up
```

###Errors
