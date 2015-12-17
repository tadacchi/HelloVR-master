package jp.co.altec.openingactionsample;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.Map;

/**
 * Created by tokue on 2015/11/29.
 */
public class FieldMap extends SurfaceView implements SurfaceHolder.Callback, Runnable {

    private SurfaceHolder mSurfaceHolder;
    private Thread mThread;
    private int mScreen_width, mScreen_height;

    static final long FPS = 20;
    static final long FRAME_TIME = 1000 / FPS;
    static final int BALL_R = 10;
    int cx = BALL_R, cy = BALL_R;

    public FieldMap(Context context) {
        super(context);

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
        Paint paint = new Paint();
        Paint bgPaint = new Paint();
        Paint txtPaint = new Paint();

        // Background
        bgPaint.setStyle(Paint.Style.FILL);
        bgPaint.setColor(Color.DKGRAY);

        // Player
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.RED);

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
                    cx = (int) (mScreen_width/2 + Float.valueOf(e.getValue().getPoint().x));
                    cy = (int) (mScreen_height/2 + Float.valueOf(e.getValue().getPoint().z));
                    System.out.println("GetPoint!! x = " + Float.valueOf(e.getValue().getPoint().x) + "y = " + Float.valueOf(e.getValue().getPoint().z));
                    System.out.println("Observe! x = " + cx + "y = " + cy);
                    canvas.drawCircle(cx, cy, BALL_R, paint);
                    canvas.drawText(e.getValue().getName(), cx, cy + BALL_R + 5, txtPaint);
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
