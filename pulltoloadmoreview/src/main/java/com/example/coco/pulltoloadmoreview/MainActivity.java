package com.example.coco.pulltoloadmoreview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private PullUpToLoadMore mPull;
    private Button mBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mPull = (PullUpToLoadMore) findViewById(R.id.mPull);
        mBtn = (Button) findViewById(R.id.btn);
        mBtn.setOnClickListener(new View.OnClickListener() {//点击按钮返回顶部
            @Override
            public void onClick(View v) {
                mPull.scrollToTop();
            }
        });
    }
}
