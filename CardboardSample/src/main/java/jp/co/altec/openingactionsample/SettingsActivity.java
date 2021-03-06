package jp.co.altec.openingactionsample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Switch;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.beardedhen.androidbootstrap.BootstrapEditText;
import com.google.vrtoolkit.cardboard.samples.treasurehunt.R;

public class SettingsActivity extends Activity {
    private boolean OnCheckObserver = false;
    UdpConnection udp;
    CheckBroadCast checkUDP;
    Handler mHandler = new Handler();
    Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            if (udp == null) return;
            udp.sendBroadcast();
            if(OnCheckObserver) {
                checkUDP.sendBroadcast();
            }
            mHandler.postDelayed(mRunnable, 300);
        }
    };

    Switch mObserverBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        BootstrapButton btn = (BootstrapButton)findViewById(R.id.button);
        mObserverBtn = (Switch)findViewById(R.id.switchObs);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (udp != null) {
                    udp.stopReceiver();
                    udp = null;
                }
                checkUDP = new CheckBroadCast(getApplicationContext(), "CheckIP");
                if (mObserverBtn.isChecked()) {
                    OnCheckObserver = true;
                    //checkUDP = new CheckBroadCast(getApplicationContext(), "CheckIP");
                    udp = new UdpConnection(getApplicationContext(),"GOAL");
                    udp.receiveBroadcast();
                }else {
                    udp = new UdpConnection(getApplicationContext(), ((BootstrapEditText) findViewById(R.id.editText)).getText().toString());
                    udp.receiveBroadcast();
                    //checkUDP = new CheckBroadCast(getApplicationContext(), "CheckIP");
                    checkUDP.receiveBroadcast();
                }

                Log.d("DEBUG", "/// DATA CONNECTION ///");
                mHandler.postDelayed(mRunnable, 300);

                if (mObserverBtn.isChecked()) {
                    Intent intent = new Intent(getApplicationContext(), ObserverActivity.class);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(getApplicationContext(), com.google.vrtoolkit.cardboard.samples.treasurehunt.MainActivity.class);
                    startActivity(intent);
                }
            }
        });
    }

}
