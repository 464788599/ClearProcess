package com.kingdom.test.clearprocess.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewTreeObserver;

/**
 * Created by admin on 2016/10/8.
 */

public class Circular extends View {
    private final Paint paint;
    private int w;
    private int h;

    public Circular(Context context, AttributeSet attrs) {
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
                return true;
            }
        });

        //创建画笔对象
        paint = new Paint();
        //消除锯齿
        paint.setAntiAlias(true);
        //设置画笔颜色
        paint.setColor(Color.rgb(100,133,177));
        //设置画笔封闭空间不需要填充
        paint.setStyle(Paint.Style.STROKE);
        //设置笔触的宽度
        paint.setStrokeWidth(5);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //注意，半径需要减去笔触宽度的一半，否则就画出界了
        //画圆
        canvas.drawCircle(w / 2, h / 2, (w > h ? h : w) / 2 - 5 / 2, paint);
    }
}
