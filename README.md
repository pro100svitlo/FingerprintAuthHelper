# FingerprintAuthDemo
A small library that allow You easy manage fingererprint authentication on devices with fingerprint scanner and Android M and upper

1. [Demo](https://play.google.com/store/apps/details?id=com.pro100svitlo.nfccardread)
2. [Usage](#usage)
3. [Callbacks](#callbacks)
4. [Updates](#updates)
5. [Used In](#used-in)
6. [Questions and help](#questions-and-help)
7. [License](#license)

# Usage
##### Add the dependencies to your gradle file:
```sh
    dependencies {
        compile 'com.github.pro100svitlo:fingerprintAuthHelper:1.1.2'
    }
```

##### Inside your activity or fragment:

```sh

    private FingerprintAuthHelper mFAH;
    private FahSecureSettingsDialog mSecureSettingsDialog;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    ...
        mFAH = new FingerprintAuthHelper.Builder(this, this)
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
    protected void onPause() {
        super.onPause();
        mFAH.stopListening();
    }
 
    ...
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mFAH.onDestroy();
    }
```
That's pretty much all what you need to start the work!

---

### Callbacks:

Your activity or fragment must implement FahListener;
 
```sh
    @Override
    public void onFingerprintStatus(boolean authSuccessful, int errorType, CharSequence errorMess) {
        if (authSuccessful){
            // do some stuff here in case auth was successfull
        } else if (mFAH != null){
          // do some stuff here in case auth failed
            switch (errorType){
                case FahErrorType.General.LOCK_SCREEN_DISABLED:
                case FahErrorType.General.NO_FINGERPRINTS:
                    if (mSecureSettingsDialog == null){
                        mSecureSettingsDialog = new FahSecureSettingsDialog.Builder(this, mFAH).build();
                    }
                    mSecureSettingsDialog.show();
                    break;
                case FahErrorType.Auth.AUTH_NOT_RECOGNIZED:
                    //do some stuff here
                    break;
            }
        }
    }

    @Override
    public void onFingerprintListening(boolean listening, long milliseconds) {
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

### Updates
* v.1.1.2
    1. add possibility to set max try count

### Questions and help
If you have some problems with using this library or something doesn't work correctly - just write me an email and describe your question or problem. I will try to do my best to help you and fix the problem if it is. Here is my email: pro100svitlo@gmail.com

### Used in
If you use this library, please, let me know (pro100svitlo@gmail.com)
Thanks!

1. [Bank of Georgia (TskApp)]


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
