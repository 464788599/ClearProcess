package com.kingdom.test.clearprocess.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;

import com.kingdom.test.clearprocess.R;

/**
 * Created by D_Xcution on 2016/9/17.
 */
public class CircularProgrssBar extends View {
    private final String CircularProgrssBarName;
    private final float CircularProgrssBarNameSize;
    private final float memorySize;
    private final float percentageSize;
    private final float progressBarsize;
    private final int progressBarColor;
    int w, h;
    private final int PEN_W = 10;
    int n=0;
    private String a;
    private String max;
    private int x;

    public CircularProgrssBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
        ViewTreeObserver vto = getViewTreeObserver();
        vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            //返回true之后，onDraw方法才会调用
            @Override
            public boolean onPreDraw() {
                // TODO Auto-generated method stub
                w = getWidth();
                h = getHeight();
                Log.i("FSLog", "w = " + w + " h = " + h);
                return true;
            }
        });
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CircularProgrssBar);
         CircularProgrssBarName = a.getString(R.styleable.CircularProgrssBar_CircularProgrssBarName);
          CircularProgrssBarNameSize = a.getDimension(R.styleable.CircularProgrssBar_CircularProgrssBarNameSize,35);
          memorySize = a.getDimension(R.styleable.CircularProgrssBar_memorySize,35);
          percentageSize = a.getDimension(R.styleable.CircularProgrssBar_percentageSize,35);
          progressBarsize = a.getDimension(R.styleable.CircularProgrssBar_progressBarsize,35);
         progressBarColor= a.getColor(R.styleable.CircularProgrssBar_progressBarColor,0xFFFFFFFF);
    }
@Override
protected void onDraw(Canvas canvas) {
    // TODO Auto-generated method stub
    super.onDraw(canvas);

    Log.i("FSLog", "onDraw");

    //设置背景颜色，该值可以由使用者控制
//		canvas.drawColor(Color.CYAN);

    //创建画笔对象
    Paint paint = new Paint();
    //消除锯齿
    paint.setAntiAlias(true);
    //设置画笔颜色
    paint.setColor(Color.GRAY);
    //设置画笔封闭空间不需要填充
    paint.setStyle(Paint.Style.STROKE);
    //设置笔触的宽度
    paint.setStrokeWidth(progressBarsize);

    //注意，半径需要减去笔触宽度的一半，否则就画出界了
    //画圆
    canvas.drawCircle(w / 2, h / 2, (w > h ? h : w) / 2 - progressBarsize / 2, paint);

    //画弧
    //oval表示弧形所处的圆形或者椭圆形所处的矩形范围
    //一个矩形决定了一个圆形或者椭圆形
    //设置画笔颜色
    paint.setColor(progressBarColor);
    RectF oval = new RectF(0 + progressBarsize / 2, 0 + progressBarsize / 2, w - progressBarsize / 2, h - progressBarsize / 2);
    canvas.drawArc(oval, 90, n * 360 / 100, false, paint);

    //画文字
    paint.setColor(progressBarColor);
    //绘制时，需要填充封闭区域
    paint.setStyle(Paint.Style.FILL);
    paint.setStrokeWidth(1);


    //计算需要绘制的该字符串的大小
    paint.setTextSize(memorySize);
    float tw = paint.measureText(a + "/"+max);
    canvas.drawText(a + "/"+max, (w - tw) / 2, (h + 10+memorySize) / 2, paint);


    paint.setTextSize(CircularProgrssBarNameSize);
    float tw2 = paint.measureText(CircularProgrssBarName);
    canvas.drawText(CircularProgrssBarName, (w - tw2) / 2, (h + 130+CircularProgrssBarNameSize) / 2, paint);

    paint.setTextSize(percentageSize);
    float tw3 = paint.measureText(n+"%");
    canvas.drawText(n+"%", (w - tw3) / 2, (h -percentageSize+40) / 2, paint);
}
    public void setProgress(int n){
        if (100<n||n<0){
            return;
        }
        this.n=n;
        postInvalidate();
    }
    public void setUseRAM(String a){
        this.a=a;
        postInvalidate();
    }
    public void setMaxRAM(String max){
        this.max=max;
        postInvalidate();
    }
}
