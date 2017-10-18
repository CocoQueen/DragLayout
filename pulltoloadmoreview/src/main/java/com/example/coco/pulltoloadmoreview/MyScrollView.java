package com.example.coco.pulltoloadmoreview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

/**
 * Created by coco on 2017/10/17.
 */

public class MyScrollView extends ScrollView {
    private static final String TAG = "MyScrollView";
    private ScrollListener listener;
    public void setScrollListener(ScrollListener listener){
        this.listener=listener;
    }

    public MyScrollView(Context context) {
        super(context);
    }

    public MyScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()){
            case MotionEvent.ACTION_MOVE:
                if (listener!=null){
                    int contentHeight = getChildAt(0).getHeight();
                    int scrollHeight = getHeight();
                    int scrollY = getScrollY();
                    listener.onScroll(scrollY);
                    if (scrollY+scrollHeight>=contentHeight||contentHeight<=scrollHeight){
                        listener.onScrollToBottom();
                    }else{
                        listener.notBottom();
                    }
                    if (scrollY==0){
                        listener.onScrollToTop();
                    }
                }

                break;
        }
        requestDisallowInterceptTouchEvent(false);//??
        return super.onTouchEvent(ev);
    }

    public interface ScrollListener{
        void onScrollToBottom();
        void onScrollToTop();
        void onScroll(int scrollY);
        void notBottom();

    }
}
