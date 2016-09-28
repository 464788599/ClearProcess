package com.kingdom.test.clearprocess.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by D_Xcution on 2016/9/17.
 */
public class BatteryProgrssBar extends View {
    private int width;
    private int heigth;
    private int progress;
    private Bitmap mBitmap;
    private Canvas mCanvasBit;
    private Paint mPaintNormal;
    private Paint mPaintPoint;
    private Paint mPaintText;
    private Path mPath;
    public static final int REFRESH=0x55;
    private int count=0;
    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case REFRESH:
                    count+=10;//count的作用是为了让波浪线动起来
                    if (count>100) {
                        count = 0;
                    }
                    progress+=1;
                    if (progress>=200){
                        progress=0;
                    }
                    handler.sendEmptyMessageDelayed(REFRESH,100);
                    invalidate();
                    break;
            }
        }
    };
    public BatteryProgrssBar(Context context) {
        super(context);
    }

    public BatteryProgrssBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPaintNormal=new Paint();
        mPaintNormal.setColor(Color.CYAN);
        mPaintNormal.setStyle(Paint.Style.FILL);
        mPaintNormal.setAntiAlias(true);
        PorterDuffXfermode mode=new PorterDuffXfermode(PorterDuff.Mode.SRC_IN);
        mPaintNormal.setXfermode(mode);

        mPaintPoint=new Paint();
        mPaintPoint.setColor(Color.GRAY);
        mPaintPoint.setAntiAlias(true);
        mPaintPoint.setStyle(Paint.Style.FILL);
        mPaintPoint.setStrokeWidth(2);

        mPaintText=new Paint();
        mPaintText.setTextSize(50);
        mPaintText.setColor(Color.BLACK);
        mPaintText.setTextAlign(Paint.Align.CENTER);

        mPath=new Path();

        handler.sendEmptyMessage(REFRESH);
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        width=getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        heigth=getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        setMeasuredDimension(width,heigth);
        mBitmap=Bitmap.createBitmap(width,heigth, Bitmap.Config.ARGB_8888);
        mCanvasBit=new Canvas(mBitmap);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
//        canvas.drawColor(Color.CYAN);
        canvas.drawBitmap(mBitmap,0,0,null);
        mCanvasBit.drawCircle(360,500,100,mPaintPoint);
        //利用贝塞尔曲线绘制动态波浪
        mPath.reset();
        mPath.moveTo(500, 600-progress);
        mPath.lineTo(500,650);
        mPath.lineTo(count,650);
        mPath.lineTo(count, 600-progress);
        //循环绘制波浪线
        for (int i=0;i<10;i++) {
            //这两个绘制贝塞尔曲线会连接成为一个波浪线
            mPath.rQuadTo(20, 6, 50, 0);//rquadTo是按照该点（即count，500这个点）为原点进行绘制操作
            mPath.rQuadTo(20, -6, 50, 0);//上一个绘制完成后的终点，成为该rQuadTo的起点（即原点）重新开始绘制
        }

        mPath.close();
        mCanvasBit.drawPath(mPath,mPaintNormal);
        mCanvasBit.drawText(progress*100/200f+"%",360,500,mPaintText);
    }
}
