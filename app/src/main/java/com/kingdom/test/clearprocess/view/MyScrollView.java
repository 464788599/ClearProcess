package com.kingdom.test.clearprocess.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

/**
 * Created by admin on 2016/9/21.
 */
public class MyScrollView extends ScrollView {
    private OnScrollListener onScrollListener;
    float downY=0;
    float upY=0;
    private float moveY;

    public MyScrollView(Context context) {
        super(context);
    }

    public MyScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setOnScrollListener(OnScrollListener onScrollListener) {
        this.onScrollListener = onScrollListener;
    }


    //滚动回调的接口
    public interface OnScrollListener{
        //回调的方法
         void onScroll(float downY,float upY,float moveY);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()){
            case MotionEvent.ACTION_DOWN:
                downY = ev.getY();
                break;
            case MotionEvent.ACTION_UP:
                upY = ev.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                moveY = ev.getY()-downY;
                break;
        }

        if (onScrollListener!=null){
            onScrollListener.onScroll(downY,upY,moveY);
        }
        return super.onTouchEvent(ev);
    }
}
