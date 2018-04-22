package com.wqmei.controller;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Button;
import android.widget.TextView;

import com.wqmei.R;
import com.wqmei.entity.Music;
import com.wqmei.template.MusicState;
import com.wqmei.util.CircleImageView;

import java.util.Timer;
import java.util.TimerTask;

public class MusicActivity extends AppCompatActivity
{
    private TextView musicDetailTitleText;
    private CircleImageView musicImg;
    private Music music = SearchActivity.currentMusic;
    private Button musicDetailPauseBtn;

    private Handler handler = new Handler();

    private Timer timer = null;
    private boolean rotationFlag = true;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);
        //初始化
        musicDetailTitleText = findViewById(R.id.music_detail_title_text);
        musicImg = findViewById(R.id.music_detail_img);
        musicDetailPauseBtn = findViewById(R.id.music_detail_pause_btn);

        initTitle();
        initImg();
        initButton();
        rotation();
    }

    /**
     * 旋转,在播放时执行
     */
    private void rotation()
    {
        if (MusicState.state == MusicState.play)
        {
            timer = new Timer();
            timer.schedule(new TimerTask()
            {
                @Override
                public void run()
                {
                    float rotation = musicImg.getRotation();
                    rotation = (rotation + 0.1f) % 360;
                    //rotation = (rotation + 0.6f) % 360;
                    float finalRotation = rotation;
                    handler.post(()->
                    {
                        musicImg.setRotation(finalRotation);
                    });
                }
            }, 0, 10);
        }
    }

    /**
     * 暂停旋转
     */
    private void cancelRotation()
    {
        timer.cancel();
    }

    /**
     * 初始化
     */
    private void initButton()
    {
        if (MusicState.state == MusicState.play)
        {
            musicDetailPauseBtn.setText("暂停");
        } else if (MusicState.state == MusicState.stop)
        {
            musicDetailPauseBtn.setText("播放");
        }

        musicDetailPauseBtn.setOnClickListener((v)->
        {
            Log.i("INFO", "切换状态");
            if (MusicState.state == MusicState.play)
            {
                //执行暂停
                if (SearchActivity.mediaPlayer != null && SearchActivity.mediaPlayer.isPlaying())
                {
                    SearchActivity.mediaPlayer.pause();
                    musicDetailPauseBtn.setText("播放");
                    MusicState.changeState();
                    cancelRotation();
                }
            } else if (MusicState.state == MusicState.stop)
            {
                //执行播放
                if (SearchActivity.mediaPlayer != null)
                {
                    SearchActivity.mediaPlayer.start();
                    musicDetailPauseBtn.setText("暂停");
                    MusicState.changeState();
                    rotation();
                }
            }
        });


    }

    /**
     * 返回键退出该activity
     *
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_BACK)
        {
            Log.i("INFO", "音乐详情页退出");
            Intent intent = new Intent();
            setResult(1,intent);
            this.finish();
        }
        return true;
    }

    /**
     * 获取图片
     */
    private void initImg()
    {
        musicImg.setImageBitmap(music.getBitmap());
    }

    /**
     * 初始化title
     */
    private void initTitle()
    {
        String spaceStr = "      ";
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < 5; i++)
        {
            stringBuffer.append(music.getName() + " - " + music.getSinger() + spaceStr);
        }
        musicDetailTitleText.setText(stringBuffer.toString());
    }


}
