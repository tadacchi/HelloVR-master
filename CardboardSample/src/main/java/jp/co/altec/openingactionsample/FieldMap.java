package jp.co.altec.openingactionsample;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.google.vrtoolkit.cardboard.samples.treasurehunt.NetWorkMgr;

import java.util.Map;

/**
 * Created by tokue on 2015/11/29.
 */
public class FieldMap extends SurfaceView implements SurfaceHolder.Callback, Runnable {

    private SurfaceHolder mSurfaceHolder;
    private Thread mThread;
    NetWorkMgr mNetWorkMgr = NetWorkMgr.getInstance();
    private int mScreen_width, mScreen_height;
    CheckBroadCast checkUDP;
    private String GOAL = "GOAL";
    private String KeyIP;
    static final long FPS = 20;
    static final long FRAME_TIME = 1000 / FPS;
    static final int BALL_R = 10;
    int cx = BALL_R, cy = BALL_R;
    int checkx = 359, checky = 602;
    boolean CheckTouchX = false;
    boolean CheckTouchY = false;
    public FieldMap(Context context) {
        super(context);
        checkUDP = new CheckBroadCast(context,KeyIP);
        mSurfaceHolder = getHolder();
        mSurfaceHolder.addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mThread = new Thread(this);
        mThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        mScreen_width = width;
        mScreen_height = height;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mThread = null;
    }

    @Override
    public void run() {

        Canvas canvas = null;
        Paint plpaint = new Paint();
        Paint bgPaint = new Paint();
        Paint txtPaint = new Paint();
        Paint trpaint = new Paint();
        // Background
        bgPaint.setStyle(Paint.Style.FILL);
        bgPaint.setColor(Color.DKGRAY);

        // Player
        plpaint.setStyle(Paint.Style.FILL);
        plpaint.setColor(Color.GREEN);
        //Treasure
        trpaint.setStyle(Paint.Style.FILL);
        trpaint.setColor(Color.YELLOW);

        txtPaint.setColor(Color.WHITE);
        txtPaint.setTextSize(18);

        long loopCount = 0;
        long waitTime = 0;
        long startTime = System.currentTimeMillis();
        while(mThread != null) {
            try{
                loopCount++;
                canvas = mSurfaceHolder.lockCanvas();
                canvas.drawRect( 0, 0, mScreen_width, mScreen_height, bgPaint);
                // Player情報描画
                for(Map.Entry<String, DeviceInfo> e : DataControl.mDeviceInfos.entrySet()) {
                    System.out.println(e.getKey() + " : " + e.getValue());
                    KeyIP = e.getKey();
                    cx = (int) (mScreen_width/2 + Float.valueOf(e.getValue().getPoint().x));
                    cy = (int) (mScreen_height/2 + Float.valueOf(e.getValue().getPoint().z));
                    canvas.drawCircle(cx, cy, BALL_R, plpaint);
                    canvas.drawCircle(checkx, checky, BALL_R, trpaint);
                    canvas.drawText(GOAL, checkx, checky + BALL_R + 5, txtPaint);
                    canvas.drawText(e.getValue().getName(), cx, cy + BALL_R + 5, txtPaint);
                    CheckInfo checkInfo = mNetWorkMgr.getCheckInfo();
                    if(checkInfo == null){
                        mNetWorkMgr.setCheckInfo("0.0.0.0");
                    }
                    if(checkx-5 < cx&&cx < checkx +5){
                        CheckTouchX = true;
                    }
                    if(checky-5 < cy&&cy < checky +5){
                        CheckTouchY = true;
                    }
                    if (CheckTouchX && CheckTouchY) {
                        checkInfo.setKeyIP(e.getKey());
                    } else {
                        checkInfo.setKeyIP("0.0.0.0");
                    }
                }
                mSurfaceHolder.unlockCanvasAndPost(canvas);
                waitTime = (loopCount * FRAME_TIME) - (System.currentTimeMillis() - startTime);
                if( waitTime > 0 ) {
                    Thread.sleep(waitTime);
                }
            } catch(Exception e) {
            }
        }
    }

}
