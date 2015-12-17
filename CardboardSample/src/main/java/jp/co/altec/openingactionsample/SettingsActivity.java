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
    int count = 0;
    Handler mHandler = new Handler();
    Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            if (udp == null) return;
            if (count/4 > 0) {
 //               udp.mDeviceInfo.setPoint(new Point(String.valueOf(count), "0", String.valueOf(++count)));
                  udp.mDeviceInfo.setPoint(udp.mDeviceInfo.getPoint());
                  System.out.println("UDP_POINT = "+ udp.mDeviceInfo.getPoint());
            }
            udp.sendBroadcast();
            mHandler.postDelayed(mRunnable,300);
            count++;
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
                udp = new UdpConnection(getApplicationContext(), ((BootstrapEditText)findViewById(R.id.editText)).getText().toString());
                udp.receiveBroadcast();

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
