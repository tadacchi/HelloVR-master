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
    UdpConnection udp;
    Handler mHandler = new Handler();
    Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            if (udp == null) return;
            udp.sendBroadcast();
            mHandler.postDelayed(mRunnable,300);
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
                if (mObserverBtn.isChecked()) {
                    udp = new UdpConnection(getApplicationContext(),"GOAL");
                    udp.receiveBroadcast();
                }else {
                    udp = new UdpConnection(getApplicationContext(), ((BootstrapEditText) findViewById(R.id.editText)).getText().toString());
                    udp.receiveBroadcast();
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
