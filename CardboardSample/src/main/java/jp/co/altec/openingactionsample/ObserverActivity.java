package jp.co.altec.openingactionsample;

import android.app.Activity;
import android.os.Bundle;

public class ObserverActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new FieldMap(this));
    }

}
