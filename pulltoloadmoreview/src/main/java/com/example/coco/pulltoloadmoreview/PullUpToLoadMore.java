package com.example.coco.pulltoloadmoreview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Scroller;

/**
 * Created by coco on 2017/10/17.
 */

public class PullUpToLoadMore extends ViewGroup {
    public static String TAG = PullUpToLoadMore.class.getName();
    MyScrollView scrollViewTop, scrollViewBottom;
    VelocityTracker tracker = VelocityTracker.obtain();//??
    Scroller scroller = new Scroller(getContext());

    int curPosition = 0;
    int positionY;
    int lastY;
    public int scaledTouchSlop;
    int speed = 200;
    boolean isIntercept;
    public boolean bottomIsTop = false;
    public boolean topIsBottom = false;

    public PullUpToLoadMore(Context context) {
        super(context);
        init();
    }

    private void init() {
        post(new Runnable() {
            @Override
            public void run() {
                scrollViewTop = (MyScrollView) getChildAt(0);
                scrollViewBottom = (MyScrollView) getChildAt(1);
                scrollViewTop.setScrollListener(new MyScrollView.ScrollListener() {
                    @Override
                    public void onScrollToBottom() {
                        topIsBottom = true;
                    }

                    @Override
                    public void onScrollToTop() {

                    }

                    @Override
                    public void onScroll(int scrollY) {

                    }

                    @Override
                    public void notBottom() {
                        topIsBottom = false;
                    }
                });
                scrollViewBottom.setScrollListener(new MyScrollView.ScrollListener() {
                    @Override
                    public void onScrollToBottom() {

                    }

                    @Override
                    public void onScrollToTop() {

                    }

                    @Override
                    public void onScroll(int scrollY) {
                        if (scrollY == 0) {
                            bottomIsTop = true;
                        } else {
                            bottomIsTop = false;
                        }
                    }

                    @Override
                    public void notBottom() {

                    }
                });
                positionY=scrollViewTop.getBottom();
               scaledTouchSlop= ViewConfiguration.get(getContext()).getScaledTouchSlop();
            }
        });

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        this.requestDisallowInterceptTouchEvent(false);
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int  y = (int) ev.getY();
        switch (ev.getAction()){
            case MotionEvent.ACTION_DOWN:
                lastY=y;
                break;
            case MotionEvent.ACTION_MOVE:
                if (topIsBottom){
                    int dy = lastY - y;
                    if (dy>0&&curPosition==0){
                        if (dy>=scaledTouchSlop){
                            isIntercept=true;
                            lastY=y;
                        }
                    }
                }
                if (bottomIsTop){
                    int dy = lastY - y;
                    if (dy<0&&curPosition==1){
                        if (Math.abs(dy)>=scaledTouchSlop){
                            isIntercept=true;
                        }
                    }
                }
                break;
        }


        return isIntercept;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int y = (int) event.getY();
        tracker.addMovement(event);//??
        switch (event.getAction()){
            case MotionEvent.ACTION_MOVE:
                int dy = lastY - y;
                if (getScrollY()+dy<0){
                    dy=getScrollY()+dy+Math.abs(getScrollY()+dy);
                }
                if (getScrollY()+dy+getHeight()>scrollViewBottom.getBottom()){
                    dy=dy-(getScrollY()+dy-(scrollViewBottom.getBottom()-getHeight()));
                }
                scrollBy(0,dy);
                break;
            case MotionEvent.ACTION_UP:
                isIntercept=false;
                tracker.computeCurrentVelocity(1000);//??
                float yVelocity = tracker.getYVelocity();
                if (curPosition==0){
                    if (yVelocity<0&&yVelocity<-speed){
                        smoothScroll(positionY);
                        curPosition=1;
                    }else{
                        smoothScroll(0);
                    }
                }else{
                    if (yVelocity>0&&yVelocity>speed){
                        smoothScroll(0);
                        curPosition=0;
                    }else {
                        smoothScroll(positionY);
                    }
                }
                break;
        }
        lastY=y;


        return true;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        measureChildren(widthMeasureSpec,heightMeasureSpec);
    }

    private void smoothScroll(int positionY) {
        int dy = positionY - getScrollY();
        scroller.startScroll(getScrollX(),getScrollY(),0,dy);
        invalidate();
    }

    public PullUpToLoadMore(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PullUpToLoadMore(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int childCount = getChildCount();
        int childTop=0;
        for (int i = 0; i < childCount; i++) {
            View view = getChildAt(i);
            view.layout(l,childTop,r,childTop+view.getMeasuredHeight());
            childTop+=view.getMeasuredHeight();
        }
    }
    //滚动到顶部
    public void scrollToTop(){
        smoothScroll(0);
        curPosition=0;
        scrollViewTop.smoothScrollTo(0,0);
    }

    @Override
    public void computeScroll() {
        if (scroller.computeScrollOffset()) {
            scrollTo(scroller.getCurrX(), scroller.getCurrY());
            postInvalidate();
        }
    }
}
