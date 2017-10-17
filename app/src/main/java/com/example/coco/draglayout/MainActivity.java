package com.example.coco.draglayout;

import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {
Fragment fragment1,fragment2;
    private MyDragLayout draglayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        //只支持两页的拖动
        fragment1 = new Fragment1();
        fragment2 = new Fragment2();

        getSupportFragmentManager().beginTransaction()
                .add(R.id.one, fragment1)
                .add(R.id.two, fragment2)
                .commit();


        draglayout = (MyDragLayout) findViewById(R.id.mDrag);
        draglayout.setNextPageListener(new MyDragLayout.showNextPageNotifier() {//手指滑动时的监听
            @Override
            public void onDragNext() {//滑动时延时0.2s
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        SystemClock.sleep(200);
                    }
                });
            }
        });
    }
}
