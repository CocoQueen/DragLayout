package com.example.coco.draglayout;

import android.content.Context;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by coco on 2017/10/17.
 * <p>
 * 这是一个viewgroup容器，实现上下两个frame layout的拖动切换
 */

public class MyDragLayout extends ViewGroup {

    //拖拽工具类
    private ViewDragHelper helper;
    private GestureDetectorCompat compat;
    private View frameView1, frameView2;//上下两个framelayout
    private int viewHeight;
    private static final int VEL_THRESHOLD = 100;//滑动速度的阈值，超过这个绝对值认为是上下
    private static final int DISTANCE_THRESHOLD = 101;//滑动速度不够时，通过这个阈值来判断应该粘到底部还是顶部
    private int downTop1;//手指按下时，frameview1的gettop值
    private showNextPageNotifier nextPageListener;//手指松开是否加载下一页的监听

    public MyDragLayout(Context context) {
        this(context, null);
    }

    public MyDragLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyDragLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        helper = ViewDragHelper.create(this, 10f, new ViewDragHelper.Callback() {//拖拽效果的主要逻辑
            @Override
            public boolean tryCaptureView(View child, int pointerId) {//返回true进行两个子view的跟踪
                return true;
            }

            @Override
            public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
                int childIndex = 1;
                if (changedView == frameView2) {
                    childIndex = 2;
                }
                //一个view的位置改变，另一个view的位置要跟进
                onViewPosChanged(childIndex, top);
            }

            @Override
            public int getViewVerticalDragRange(View child) {
                //这个用来控制拖拽过程中松手后，自动滑行的速度，？？？随意的数字？？？感觉没啥用
                return 1;
            }

            @Override
            public void onViewReleased(View releasedChild, float xvel, float yvel) {
                //滑动松开后，需要向上或者向下粘到特定的位置
                animTopOrBottom(releasedChild, yvel);
            }

            @Override
            public int clampViewPositionVertical(View child, int top, int dy) {
                int finalTop = top;
                //上面的view
                if (child == frameView1) {
                    if (top > 0) {
                        //不让第一个往下拖，因为顶部会白板
                        finalTop = 0;
                    }
                }
                //下面的view
                else if (child == frameView2) {
                    if (top < 0) {
                        //不让第二个往上拖。因为会白板
                        finalTop = 0;
                    }
                }
                // finalTop代表的是理论上应该拖动到的位置。此处计算拖动的距离除以一个参数(3)，是让滑动的速度变慢。数值越大，滑动的越慢
                return child.getTop() + (finalTop - child.getTop()) / 3;
            }
        });
        helper.setEdgeTrackingEnabled(ViewDragHelper.EDGE_BOTTOM);
        compat = new GestureDetectorCompat(context, new YScrollDetector());
    }

    @Override
    protected void onFinishInflate() {
        frameView1 = getChildAt(0);
        frameView2 = getChildAt(1);
    }

    @Override
    public void computeScroll() {
        if (helper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    private void animTopOrBottom(View releasedChild, float yvel) {
        int finalTop = 0;//默认粘到最顶端
        if (releasedChild == frameView1) {
            //拖动第一个view松手
            if (yvel < -VEL_THRESHOLD || (downTop1 == 0 && frameView1.getTop() < -DISTANCE_THRESHOLD)) {
                //向上速度足够大（即超过某个阈值），就滑动到顶端
                finalTop = -viewHeight;

                //初始化下一页
                if (null != nextPageListener) {
                    nextPageListener.onDragNext();
                }
            }
        } else {
            //拖动第二个view松手
            if (VEL_THRESHOLD < yvel || (downTop1 == -viewHeight && releasedChild.getTop() > DISTANCE_THRESHOLD)) {
                //保持原地不动
                finalTop = viewHeight;
            }
        }
        if (helper.smoothSlideViewTo(releasedChild, 0, finalTop)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    /**
     * 滑动时view位置改变的处理逻辑
     * @param childIndex
     * 滑动显示的view
     * @param top
     * 滑动view的top位置
     */
    private void onViewPosChanged(int childIndex, int top) {
        if (childIndex == 1) {
            int offsetTopBottom = viewHeight + frameView1.getTop() - frameView2.getTop();
            frameView2.offsetTopAndBottom(offsetTopBottom);
        } else if (childIndex == 2) {
            frameView1.offsetTopAndBottom(frameView2.getTop() - viewHeight - frameView1.getTop());
        }
        invalidate();
    }

    /**
     * touch事件的拦截与处理都交给拖拽工具类来处理
     * @param ev
     * @return
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (frameView1.getBottom() > 0 && frameView1.getTop() < 0) {
            //view粘到顶部或底部，正在动画中的时候，不处理touch事件
            return false;
        }
        boolean yScroll = compat.onTouchEvent(ev);
        boolean shouldIntercept = helper.shouldInterceptTouchEvent(ev);
        int action = ev.getActionMasked();
        if (action == MotionEvent.ACTION_DOWN) {

            //手指按下时，启用拖拽工具（可能会导致异常）
            helper.processTouchEvent(ev);
            downTop1 = frameView1.getTop();
        }
        return shouldIntercept && yScroll;

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // 统一交给拖拽工具类处理，由DragHelperCallback实现拖动效果
        try {
            helper.processTouchEvent(event);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        // 只在初始化的时候调用
        // 一些参数作为全局变量保存起来
        frameView1.layout(l, 0, r, b - t);
        frameView2.layout(l, 0, r, b - t);

        viewHeight = frameView1.getMeasuredHeight();
        frameView2.offsetTopAndBottom(viewHeight);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        measureChildren(widthMeasureSpec, heightMeasureSpec);

        int maxWidth = MeasureSpec.getSize(widthMeasureSpec);
        int maxHeight = MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(resolveSizeAndState(maxWidth, widthMeasureSpec, 0), resolveSizeAndState(maxHeight, heightMeasureSpec, 0));

    }

    public interface showNextPageNotifier {
        void onDragNext();
    }

    public void setNextPageListener(showNextPageNotifier nextPageListener) {
        this.nextPageListener = nextPageListener;
    }

    private class YScrollDetector extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            float y = Math.abs(distanceY);
            float x = Math.abs(distanceX);
            boolean b = y > x;
            return b;
        }
    }
}
