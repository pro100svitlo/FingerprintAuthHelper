# FingerprintAuthHelper
A small library that allows You to easily manage fingererprint authentication inside your Activity or Fragment on devices with fingerprint scanner and Android M and higher.
Min sdk version 14.

1. [Demo app](https://play.google.com/store/apps/details?id=com.pro100svitlo.fingerprintauthdemo)
2. [Usage](#usage)
3. [Documentation](Docs.md)
4. [Updates](#updates)
5. [Questions and help](#questions-and-help)
6. [License](#license)

![alt text](screenshots/sc_0.png "Touch sensor")
![alt text](screenshots/sc_1.png "Try in")


# Usage
##### Add the dependencies to your gradle file:
```sh
    dependencies {
        compile 'com.github.pro100svitlo:fingerprintAuthHelper:1.2.1'
    }
```

##### Inside your activity or fragment:

```sh

    private FingerprintAuthHelper mFAH;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
    ...
        mFAH = new FingerprintAuthHelper
                .Builder(this, this) //(Context inscance of Activity, FahListener)
                .build();

        if (mFAH.isHardwareEnable()){
            //do some stuff here
        } else {
            //otherwise do
        }
    }

    ...

   @Override
    protected void onResume() {
        super.onResume();
        mFAH.startListening();
    }

    ...

    @Override
    protected void onStop() {
        super.onStop();
        mFAH.stopListening();
    }

    ...

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mFAH.onDestroy();
    }

    @Override
    public void onFingerprintStatus(boolean authSuccessful, int errorType, CharSequence errorMess) {
        // authSuccessful - boolean that shows auth status
        // errorType - if auth was failed, you can catch error type
        // errorMess - if auth was failed, errorMess will tell you (and user) the reason

        if (authSuccessful){
            // do some stuff here in case auth was successful
        } else if (mFAH != null){
          // do some stuff here in case auth failed
            switch (errorType){
                case FahErrorType.General.LOCK_SCREEN_DISABLED:
                case FahErrorType.General.NO_FINGERPRINTS:
                    mFAH.showSecuritySettingsDialog();
                    break;
                case FahErrorType.Auth.AUTH_NOT_RECOGNIZED:
                    //do some stuff here
                    break;
                case FahErrorType.Auth.AUTH_TO_MANY_TRIES:
                    //do some stuff here
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
That's pretty much all what you need to start the work!
Full documentation and all options descriptions you can find [here](Docs.md).

---

### Updates
* v.1.2.1
    1. refactoring
* v.1.2.0
    1. stability improvements
* v.1.1.7
    1. new function cleanTimeOut() - in case you authinticate with other method. More details in [docs](Docs.md).
    2. remove possibility to set max try count (becouse Fingerprint API update :)
    3. now on Kotlin :)
    4. refactoring
* v.1.1.5
    1. refactoring
    2. small bugfix
* v.1.1.4
    1. fix crashes on Sumsung devices
    2. add method isFingerprintEnrolled() to FingerprintAuthHelper
    3. small bugfix
* v.1.1.3
    1. init context must be instance of Activity
    2. add method showSecuritySettingsDialog() to FingerprintAuthHelper
    3. add help error
* v.1.1.2
    1. add possibility to set max count of tries

### Questions and help
If you have some problems with using this library or something doesn't work correctly - just write me an email and describe your question or problem. I will try to do my best to help you and fix the problem if it exists. Here is my email: pro100svitlo@gmail.com. Or you also can leave an [issue here](https://github.com/pro100svitlo/FingerprintAuthHelper/issues).

### License
The MIT License (MIT)

Copyright (c) 2016 FingerprintAuthHelper

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.