package jp.co.altec.openingactionsample;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.google.vrtoolkit.cardboard.samples.treasurehunt.NetWorkMgr;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Map;

/**
 * Created by 2015295 on 2016/01/06.
 */
public class checkBroadCast {
    private final String TAG = "UDP-CONN";
    private WifiManager mWifiManager;
    private String mMyIpAddress;
    private String IP;
    CheckInfo mCheckInfo;
    NetWorkMgr mNetWorkMgr = NetWorkMgr.getInstance();
    private DatagramSocket mUdpSocket;
    private final int UDP_PORT = 11000;
    private boolean close = false;

    public checkBroadCast(Context context, String CheckIP) {
        mWifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        mNetWorkMgr.setCheckInfo(getMyIpAddress(),CheckIP);
        mCheckInfo = mNetWorkMgr.getCheckInfo();
    }

    public void receiveBroadcast() {
        new Thread() {
            @Override
            public void run(){
                String receiveData;
                try {
                    //受信用ソケット
                    if (mUdpSocket == null) {
                        mUdpSocket = new DatagramSocket(UDP_PORT);
                    }
                    Log.d(TAG, "receive socket open. port :" + mUdpSocket.getLocalPort());

                    //waiting = trueの間、ブロードキャストを受け取る
                    while(!close){
                        byte[] buf = new byte[1024];
                        DatagramPacket packet = new DatagramPacket(buf, buf.length);

                        //ゲスト端末からのブロードキャストを受け取る
                        //受け取るまでは待ち状態になる
                        Log.d(TAG, "waiting packet data ..........");
                        mUdpSocket.receive(packet);

                        int length = packet.getLength();
                        receiveData = new String(buf, 0, length);

                        // 送信元情報の取得


                        DeviceInfo info = mNetWorkMgr.parce(receiveData);
                        if (!info.getIpAddress().equals(getMyIpAddress())) {
                            DataControl.mDeviceInfos.put(info.getIpAddress(), info);
                        } else {
                            // test
                            //DataControl.mDeviceInfos.put(info.getIpAddress(), info);
                            Log.d(TAG, "my device info receive :: " + info);
                        }

                        Log.d(TAG, "receive socketAddress is " + packet.getSocketAddress().toString() + " packet data : " + receiveData);
                    }
                    mUdpSocket.close();
                } catch (SocketException e) {
                    close = true;
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    /**
     * 自分のIPアドレスを取得する。
     * @return IPアドレス
     */
    public String getMyIpAddress(){
        int ipAddress_int = 0;

        if (mWifiManager.getConnectionInfo() != null) {
            ipAddress_int = mWifiManager.getConnectionInfo().getIpAddress();
            mMyIpAddress = (ipAddress_int & 0xFF) + "." + (ipAddress_int >> 8 & 0xFF) + "." + (ipAddress_int >> 16 & 0xFF) + "." + (ipAddress_int >> 24 & 0xFF);
        } else {
            mMyIpAddress = null;
        }

        Log.d(TAG, "my ipAddress is " + mMyIpAddress);
        return mMyIpAddress;
    }

    InetAddress getBroadcastAddress(){
        DhcpInfo dhcpInfo = mWifiManager.getDhcpInfo();
        int broadcast = (dhcpInfo.ipAddress & dhcpInfo.netmask) | ~dhcpInfo.netmask;
        byte[] quads = new byte[4];
        for (int i = 0; i < 4; i++){
            quads[i] = (byte)((broadcast >> i * 8) & 0xFF);
        }
        try {
            return InetAddress.getByAddress(quads);
        }catch (IOException e){
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 同一Wi-fiに接続している全端末に対してブロードキャスト送信を行う
     */
    void sendBroadcast(final String IPAddr){
        new Thread() {
            @Override
            public void run() {
                try {
                    if (mUdpSocket == null) {
                        mUdpSocket = new DatagramSocket(UDP_PORT);
                    }
                    mUdpSocket.setBroadcast(true);
                    String toCheckIP = IPAddr + getMyIpAddress();
                    DatagramPacket packet = new DatagramPacket(toCheckIP.getBytes(), toCheckIP.getBytes().length, getBroadcastAddress(), UDP_PORT);
                    mUdpSocket.send(packet);
                } catch (SocketException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    void stopReceiver() {
        close = true;
    }
}


