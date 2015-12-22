package com.google.vrtoolkit.cardboard.samples.treasurehunt;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.util.concurrent.ConcurrentHashMap;

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
    private WifiManager mWifiManager;
    private String mMyIpAddress, mOtherIpAddress;
    private final String TAG = "UDP-CONN";
    String name,ipAddress, x, y, z;
    Context mContext;
    Point point = new Point(x, y, z);
    private UdpConnection mUdpConnection;
    private DeviceInfo mDeviceInfo;
    private Point mPoint;
    private static NetWorkMgr instance = new NetWorkMgr();

    private NetWorkMgr() {}

    public static NetWorkMgr getInstance() {
        return instance;
    }

    //Hash Map
    ConcurrentHashMap<String, String> mHashMap = new ConcurrentHashMap<String, String>();
    public void setMyPoint(String x, String y, String z) {
        mPoint = new Point(x,y,z);
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

    public Point getMyPoint(){
        return mPoint;
    }
    public void setMyIpAddress(){

    }

        //To MainActivity
    /*

    //To SettingActivity
    public void setMyPoint(Point myPoint) {
        deviceInfo.setPoint(myPoint);
    }

    public Point getmPoint() {
        return deviceInfo.getPoint();
    }

    //To UdpConnection
    public String sendMyPoint() {
        return deviceInfo.Format();
    }

    public void setOtherPoint(String receiveData) {
        DeviceInfo info = DeviceInfo.parse(receiveData);
        if (!info.getIpAddress().equals(mUdpConnection.getMyIpAddress())) {
            mHashMap.put(info.getIpAddress(), info.Format());
        } else {
            // test
            mHashMap.put(info.getIpAddress(), info.Format());
            Log.d(TAG, "my device info receive :: " + info);
        }
    }

    public UdpConnection mUdpConnection(Context mContext, String name) {
        return new UdpConnection(mContext, name);
    }
    /*public String getOtherPoint(){
        return mHashMap.get(getOtherIpAddress());
    }
    public String getMyPoint() {
        return mHashMap.get(getMyIpAddress());
    }


    String getOtherIpAddress(){
        int ipAddress_int = 0;

    }
}
    String getMyIpAddress() {
        int ipAddress_int = 0;

        if (mWifiManager.getConnectionInfo() != null) {
            ipAddress_int = mWifiManager.getConnectionInfo().getIpAddress();
            mMyIpAddress = (ipAddress_int & 0xFF) + "." + (ipAddress_int >> 8 & 0xFF) + "." + (ipAddress_int >> 16 & 0xFF) + "." + (ipAddress_int >> 24 & 0xFF);
        } else {
            mMyIpAddress = null;
        }

        Log.d(TAG, "my ipAddress is " + mMyIpAddress);
        return mMyIpAddress;
    }*/
}
