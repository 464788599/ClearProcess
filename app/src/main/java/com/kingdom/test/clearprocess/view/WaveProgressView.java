package com.kingdom.test.clearprocess.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;

import com.kingdom.test.clearprocess.R;


/**
 * Created by D_Xcution on 2016/9/17.
 */
public class WaveProgressView extends View {

    private final int progressColor;
    private  final int radiusColor;
    private  final int textColor;
    private float progress;
    private  final float maxProgress;
    private  final int textSize;
    private int radius;
    public static final int REFRESH=0x55;

    private  Paint textPaint;
    private  Canvas bitmapCanvas;
    private  Paint pathPaint;
    private  Bitmap bitmap;

    private int width;
    private int height;
    private int minPadding;
    private Paint circlePaint;
    private Path path;
    private int count;
    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case REFRESH:
                    count+=10;//count的作用是为了让波浪线动起来
                    if (count>600) {
                        count = 0;
                    }
            handler.sendEmptyMessageDelayed(REFRESH,100);
            invalidate();
            break;
        }
    }
};


    public WaveProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.WaveProgressView);
        radius = (int) a.getDimension(R.styleable.WaveProgressView_radius, 35);
        textColor = a.getColor(R.styleable.WaveProgressView_progress_text_color, 0xFFFFFFFF);
        textSize = a.getDimensionPixelSize(R.styleable.WaveProgressView_progress_text_size, 35);
        progressColor = a.getColor(R.styleable.WaveProgressView_progress_color, 0xFFFFFFFF);
        radiusColor = a.getColor(R.styleable.WaveProgressView_radius_color, 0xFFFFFFFF);
        progress = a.getFloat(R.styleable.WaveProgressView_progress, 0);
        maxProgress = a.getFloat(R.styleable.WaveProgressView_maxProgress, 100);
        a.recycle();

        handler.sendEmptyMessage(REFRESH);


        }

@Override
protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //计算宽和高
        int exceptW = getPaddingLeft() + getPaddingRight() + 2 * radius;
        int exceptH = getPaddingTop() + getPaddingBottom() + 2 * radius;
        int width = resolveSize(exceptW, widthMeasureSpec);
        int height = resolveSize(exceptH, heightMeasureSpec);
        int min = Math.min(width, height);

        this.width = this.height = min;

        //计算半径,减去padding的最小值
        int minLR = Math.min(getPaddingLeft(), getPaddingRight());
        int minTB = Math.min(getPaddingTop(), getPaddingBottom());
        minPadding = Math.min(minLR, minTB);
        radius = (min - minPadding * 2) / 2;

        //设置控件的宽和高
        setMeasuredDimension(min, min);
        }

@Override
protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //设置绘制Path的画笔
        pathPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pathPaint.setColor(progressColor);
        pathPaint.setDither(true);
        pathPaint.setAntiAlias(true);
        //绘制path路径的画笔设置为PorterDuff.Mode.SRC_IN模式，这个模式只显示重叠的部分
        pathPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

        //设置画园的画笔
        circlePaint = new Paint();
        circlePaint.setColor(radiusColor);
        circlePaint.setAntiAlias(true);
        circlePaint.setStyle(Paint.Style.FILL);



        //设置绘制文字的画笔
        textPaint = new Paint();
        textPaint.setColor(textColor);
        textPaint.setTextSize(textSize);


        //将所有的绘制 绘制到一个透明的bitmap上，然后将这个bitmap绘制到canvas上
        if (bitmap == null) {
        bitmap = Bitmap.createBitmap(this.width, this.height, Bitmap.Config.ARGB_8888);
        bitmapCanvas = new Canvas(bitmap);
        }

        path = new Path();


        //为了方便计算和绘制，将坐标系平移padding的距离
        bitmapCanvas.save();
        //移动坐标系
        bitmapCanvas.translate(minPadding, minPadding);
        // .... some thing
        bitmapCanvas.restore();

        //绘制园
        bitmapCanvas.drawCircle(radius, radius, radius, circlePaint);

        //绘制PATH
        //重置绘制路线
        path.reset();
        float percent=progress * 1.0f / maxProgress;//0~1
        float y = (1 - percent) * radius * 2;//2*radius~0
        //移动到右上边
        path.moveTo(radius * 2, y);
        //移动到最右下方
        path.lineTo(radius * 2, radius * 2);
        //移动到最左下边
        path.lineTo(0, radius * 2);
        //移动到左上边
        // path.lineTo(0, y);
        //实现左右波动,根据progress来平移
        path.lineTo(-count, y);
        if (progress != 0.0f) {
        //根据直径计算绘制贝赛尔曲线的次数
        int count = radius * 4 / 150;
        //控制-控制点y的坐标
            float point = (1 - percent) * 60;
            for (int i = 0; i < count; i++) {
                path.rQuadTo(60, -point, 150, 0);
                path.rQuadTo(60, point, 150, 0);
            }
        }
        //闭合
        path.close();
        bitmapCanvas.drawPath(path, pathPaint);

        //绘制文字
        String text = progress + "%";
        float textW = textPaint.measureText(text);
        Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
        float baseLine = radius - (fontMetrics.ascent + fontMetrics.descent) / 2;
        bitmapCanvas.drawText(text, radius - textW / 2, baseLine, textPaint);

        //bitmap绘制到canvas上
        canvas.drawBitmap(bitmap, 0, 0, null);



    }

    public void setProgress(int progress){
        if (maxProgress<progress||progress<0){
            return;
        }
        this.progress=progress;
        postInvalidate();
    }
}
