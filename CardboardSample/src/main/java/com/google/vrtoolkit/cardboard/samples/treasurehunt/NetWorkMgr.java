package com.google.vrtoolkit.cardboard.samples.treasurehunt;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.util.concurrent.ConcurrentHashMap;

import jp.co.altec.openingactionsample.CheckInfo;
import jp.co.altec.openingactionsample.DataControl;
import jp.co.altec.openingactionsample.DeviceInfo;
import jp.co.altec.openingactionsample.Point;
import jp.co.altec.openingactionsample.UdpConnection;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
/**
 * Created by 2015295 on 2015/12/21.
 */
public class NetWorkMgr {
    String name,ipAddress, x, y, z;
    private DeviceInfo mDeviceInfo;
    private CheckInfo mCheckInfo;
    private Point mPoint;
    private static NetWorkMgr instance = new NetWorkMgr();

    private NetWorkMgr() {}

    public static NetWorkMgr getInstance() {
        return instance;
    }


    public void setDeviceInfo(String name, String ipAddress, Point point) {
        mDeviceInfo = new DeviceInfo(name, ipAddress, point);
    }
    public DeviceInfo getDeviceInfo() {
        return mDeviceInfo;
    }
    public String DeviceInfoFormat(){
        return mDeviceInfo.Format();
    }

    public DeviceInfo parce(String in){
        return mDeviceInfo.parse(in);
    }

    public void setCheckInfo(String KeyIP, String CheckIP){
        mCheckInfo = new CheckInfo(KeyIP , CheckIP);
    }
    public CheckInfo getCheckInfo(){
        return mCheckInfo;
    }

    public CheckInfo parse(String in){
        return mCheckInfo.parse(in);
    }
    public Point getMyPoint(){
        if (mPoint == null){
            return new Point("-1","-1","-1");
        }
        else{
            return mPoint;
        }
    }

}

