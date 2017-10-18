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
    MyScrollView scrollViewTop, scrollViewBottom;
    //主要用跟踪触摸屏事件（flinging事件和其他gestures手势事件）的速率
    VelocityTracker tracker = VelocityTracker.obtain();
    Scroller scroller = new Scroller(getContext());

    int curPosition = 0;//当前位置
    int positionY;
    int lastY;//手指滑动后停止的位置
    public int scaledTouchSlop;//最小滑动距离
    int speed = 200;//滑动速度
    boolean isIntercept;//判断是否拦截
    public boolean bottomIsTop = false;
    public boolean topIsBottom = false;

    public PullUpToLoadMore(Context context) {
        super(context);
        init();
    }

    //初始化操作
    private void init() {
        post(new Runnable() {
            @Override
            public void run() {
                scrollViewTop = (MyScrollView) getChildAt(0);
                scrollViewBottom = (MyScrollView) getChildAt(1);

                scrollViewTop.setScrollListener(new MyScrollView.ScrollListener() {//顶部scroll view的滑动监听
                    @Override
                    public void onScrollToBottom() {//滑动到底部
                        topIsBottom = true;
                    }

                    @Override
                    public void onScrollToTop() {//滑动到顶部

                    }

                    @Override
                    public void onScroll(int scrollY) {

                    }

                    @Override
                    public void notBottom() {//不是底部
                        topIsBottom = false;
                    }
                });
                scrollViewBottom.setScrollListener(new MyScrollView.ScrollListener() {//底部scrollview的监听（参照顶部scrollview）
                    @Override
                    public void onScrollToBottom() {

                    }

                    @Override
                    public void onScrollToTop() {

                    }

                    @Override
                    public void onScroll(int scrollY) {//滑动时触发
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
                positionY = scrollViewTop.getBottom();
                scaledTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
            }
        });

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        //防止子view禁止父view拦截事件
        this.requestDisallowInterceptTouchEvent(false);
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int y = (int) ev.getY();
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN://记录手指按下的点
                lastY = y;
                break;
            case MotionEvent.ACTION_MOVE:
                //判断是否滑动到了底部
                if (topIsBottom) {
                    int dy = lastY - y;
                    //判断是否向上滑动 && 位置停留在下标为0的位置（即：title、content页）
                    if (dy > 0 && curPosition == 0) {
                        if (dy >= scaledTouchSlop) {
                            isIntercept = true;//拦截
                            lastY = y;
                        }
                    }
                }
                if (bottomIsTop) {
                    int dy = lastY - y;
                    //向下滑动&&位置为下标为1的位置（即：图文详情页）
                    if (dy < 0 && curPosition == 1) {
                        if (Math.abs(dy) >= scaledTouchSlop) {
                            isIntercept = true;
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
        tracker.addMovement(event);//在触屏速率中添加要监听的事件
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                int dy = lastY - y;
                if (getScrollY() + dy < 0) {//向上滑动
                    dy = getScrollY() + dy + Math.abs(getScrollY() + dy);
                }
                if (getScrollY() + dy + getHeight() > scrollViewBottom.getBottom()) {
                    dy = dy - (getScrollY() + dy - (scrollViewBottom.getBottom() - getHeight()));
                }
                scrollBy(0, dy);//手指滑动了的距离
                break;
            case MotionEvent.ACTION_UP://手指抬起的事件
                isIntercept = false;
                tracker.computeCurrentVelocity(1000);//设置当前速率为1000？（应该是这个意思吧）
                float yVelocity = tracker.getYVelocity();
                if (curPosition == 0) {
                    if (yVelocity < 0 && yVelocity < -speed) {
                        smoothScroll(positionY);
                        curPosition = 1;
                    } else {
                        smoothScroll(0);
                    }
                } else {
                    if (yVelocity > 0 && yVelocity > speed) {
                        smoothScroll(0);
                        curPosition = 0;
                    } else {
                        smoothScroll(positionY);
                    }
                }
                break;
        }
        lastY = y;


        return true;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        measureChildren(widthMeasureSpec, heightMeasureSpec);
    }

    //通过scroller实现弹性滑动
    private void smoothScroll(int positionY) {
        int dy = positionY - getScrollY();
        scroller.startScroll(getScrollX(), getScrollY(), 0, dy);
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
        int childTop = 0;
        for (int i = 0; i < childCount; i++) {
            View view = getChildAt(i);
            view.layout(l, childTop, r, childTop + view.getMeasuredHeight());
            childTop += view.getMeasuredHeight();
        }
    }

    //滚动到顶部（一键返回顶部）
    public void scrollToTop() {
        smoothScroll(0);
        curPosition = 0;
        scrollViewTop.smoothScrollTo(0, 0);
    }

    /**
     * 为了易于控制滑屏控制，Android框架提供了 computeScroll()方法去控制这个流程。
     * 在绘制View时，会在draw()过程调用该 方法。因此， 再配合使用Scroller实例，
     * 我们就可以获得当前应该的偏移坐标，手动使View/ViewGroup偏移至该处。
     */
    @Override
    public void computeScroll() {
        if (scroller.computeScrollOffset()) {
            scrollTo(scroller.getCurrX(), scroller.getCurrY());
            postInvalidate();
        }
    }
}
