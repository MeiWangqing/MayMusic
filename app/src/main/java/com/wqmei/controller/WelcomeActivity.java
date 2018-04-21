package com.wqmei.controller;

import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.wqmei.R;

import java.util.Timer;
import java.util.TimerTask;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        TextView textView = findViewById(R.id.welcome_text);
        textView.setTypeface(Typeface.createFromAsset(this.getAssets(),"font/fangzhengxiuke.ttf"));
        //一定时间跳转
        goMainPage();
    }

    /**
     * 跳转
     */
    private void goMainPage()
    {
        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask()
        {
            @Override
            public void run()
            {
                Intent intent = new Intent(WelcomeActivity.this,SearchActivity.class);
                startActivity(intent);
            }
        };
        //3.5秒后启动
        timer.schedule(timerTask,3500);
    }
}
