package com.pro100svitlo.fingerprintauthdemo;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.pro100svitlo.fingerprintAuthHelper.FahErrorType;
import com.pro100svitlo.fingerprintAuthHelper.FahListener;
import com.pro100svitlo.fingerprintAuthHelper.FingerprintAuthHelper;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements FahListener {

    private final int TIME_OUT = 500;

    private FingerprintAuthHelper mFAH;

    private ImageView mFingerprintIcon;
    private TextView mFingerprintText;

    private String mFingerprintRetryStr;
    private int mFpColorError;
    private int mFpColorNormal;
    private int mFpColorSuccess;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mFingerprintIcon = (ImageView) findViewById(R.id.iv_fingerprint);
        mFingerprintText = (TextView) findViewById(R.id.tv_fingerprintText);


        mFAH = new FingerprintAuthHelper.Builder(this, this)
                .setTryTimeOut(2 *45 * 1000)
                .setKeyName(MainActivity.class.getSimpleName())
                .setLoggingEnable(true)
                .build();
        boolean isHardwareEnable = mFAH.isHardwareEnable();

        //in case if user want to disable usage fingerprint u can turn if off
//        mFAH.setCanListenByUser(false);

        if (isHardwareEnable && mFAH.canListenByUser()){
            mFpColorError = ContextCompat.getColor(this, android.R.color.holo_red_dark);
            mFpColorNormal = ContextCompat.getColor(this, R.color.colorPrimary);
            mFpColorSuccess = ContextCompat.getColor(this, android.R.color.holo_green_dark);
            mFingerprintRetryStr = getString(R.string.fingerprintTryIn);
        } else {
            mFingerprintText.setText(getString(R.string.notSupport));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFAH.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mFAH.stopListening();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mFAH.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mFAH.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mFAH.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onFingerprintStatus(boolean authSuccessful, int errorType, CharSequence errorMess) {
        if (authSuccessful){
            DrawableCompat.setTint(mFingerprintIcon.getDrawable(), mFpColorSuccess);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    goToSecondActivity();
                }
            }, TIME_OUT);
        } else if (mFAH != null){
            Toast.makeText(this, errorMess, Toast.LENGTH_SHORT).show();
            switch (errorType){
                case FahErrorType.General.HARDWARE_DISABLED:
                case FahErrorType.General.NO_FINGERPRINTS:
                    mFAH.showSecuritySettingsDialog();
                    break;
                case FahErrorType.Auth.AUTH_NOT_RECOGNIZED:
                    DrawableCompat.setTint(mFingerprintIcon.getDrawable(), mFpColorError);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            DrawableCompat.setTint(mFingerprintIcon.getDrawable(), mFpColorNormal);
                        }
                    }, TIME_OUT);
                    break;
            }
        }
    }

    @Override
    public void onFingerprintListening(boolean listening, long milliseconds) {
        if (listening){
            setFingerprintListening();
        } else {
            setFingerprintNotListening();
        }
        if (milliseconds > 0) {
            mFingerprintText.setTextColor(mFpColorError);
            mFingerprintText.setText(getPrettyTime(mFingerprintRetryStr ,milliseconds));
        }
    }

    private void goToSecondActivity() {
        startActivity(new Intent(MainActivity.this, SecondActivity.class));
    }

    private void setFingerprintListening(){
        DrawableCompat.setTint(mFingerprintIcon.getDrawable(), mFpColorNormal);
        mFingerprintText.setTextColor(mFpColorNormal);
        mFingerprintText.setText(getString(R.string.touch_sensor));
    }

    private void setFingerprintNotListening(){
        mFingerprintText.setTextColor(mFpColorError);
        DrawableCompat.setTint(mFingerprintIcon.getDrawable(), mFpColorError);
    }

    private String getPrettyTime(String coreStr, long millis){
        return String.format(coreStr,
                TimeUnit.MILLISECONDS.toMinutes(millis),
                TimeUnit.MILLISECONDS.toSeconds(millis) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))
        );
    }

    public void onEnterOtherMethodClick(View view) {
        mFAH.cleanTimeOut();
        goToSecondActivity();
    }
}
