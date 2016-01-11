package jp.co.altec.openingactionsample;

import java.security.Key;

/**
 * Created by 2015295 on 2016/01/06.
 */
public class CheckInfo {
    private String KeyIP = "0.0.0.0";

    public CheckInfo(String mKeyIP) {
        KeyIP = mKeyIP;
    }

    public void setKeyIP(String mKeyIP) {
        this.KeyIP = mKeyIP;
    }

    public String getKeyIP() {
        return KeyIP;
    }

    public String Check(String in) {
        String data = in;
        String WinIP = null;
//        if (!data.equals("0.0.0.0")) {
//            WinIP = data;
//            return WinIP;
//        }
        return data;
    }
}
