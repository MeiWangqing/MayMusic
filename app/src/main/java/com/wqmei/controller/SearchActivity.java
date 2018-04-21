package com.wqmei.controller;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.wqmei.R;
import com.wqmei.adapter.MusicAdapter;
import com.wqmei.entity.Music;
import com.wqmei.util.SongUtil;


import java.io.IOException;
import java.util.List;

public class SearchActivity extends AppCompatActivity implements MediaPlayer.OnBufferingUpdateListener,MediaPlayer.OnCompletionListener,MediaPlayer.OnPreparedListener{

    //上下文
    public static Context context;

    private MediaPlayer mediaPlayer = new MediaPlayer();

    //歌曲列表
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /*http://music.163.com/song/media/outer/url?id=ID数字.mp3*/
        context = getApplicationContext();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        listView = findViewById(R.id.song_list);
        /*listView.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_expandable_list_item_1, new ArrayList<String>()
        {{
            add("123");
            add("hello");
            add("world");
        }}));*/
        List<Music> songList = SongUtil.getMusicList("绿茶");
        System.out.println(songList.size());
        //System.out.println(songList.get(0).toString());
        MusicAdapter musicAdapter = new MusicAdapter(SearchActivity.this,R.layout.list_view, songList);
        listView.setAdapter(musicAdapter);


        mediaPlayer.setAudioStreamType(AudioManager.STREAM_SYSTEM);
        mediaPlayer.setOnBufferingUpdateListener(SearchActivity.this);
        mediaPlayer.setOnPreparedListener(SearchActivity.this);

        listView.setOnItemClickListener((parent, view, position, id) ->
        {
            Toast.makeText(SearchActivity.this, "点击", Toast.LENGTH_SHORT).show();
            Music music = (Music) parent.getItemAtPosition(position);
            System.out.println(music.toString());
            mediaPlayer.reset();
            try
            {
                mediaPlayer.setDataSource("http://music.163.com/song/media/outer/url?id="+music.getId()+".mp3");
                mediaPlayer.prepare();
                mediaPlayer.start();
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        });

    }

    public static void main(String[] args)
    {

    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent)
    {

    }

    @Override
    public void onCompletion(MediaPlayer mp)
    {

    }

    @Override
    public void onPrepared(MediaPlayer mp)
    {
        mp.start();
    }
}
