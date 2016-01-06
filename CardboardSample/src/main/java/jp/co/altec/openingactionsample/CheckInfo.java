package jp.co.altec.openingactionsample;

import java.security.Key;

/**
 * Created by 2015295 on 2016/01/06.
 */
public class CheckInfo {
    private String KeyIP = "0.0.0";
    private String CheckIP = "0.0.0";

    public CheckInfo(String mKeyIP, String mCheckIP){
        KeyIP = mKeyIP;
        CheckIP = mCheckIP;
    }


    public CheckInfo parse(String in){
        String[] data = in.split(":");
        return new CheckInfo(data[0],data[1]);
    }
}
