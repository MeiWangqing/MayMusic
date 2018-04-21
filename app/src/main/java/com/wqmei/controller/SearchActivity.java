package com.wqmei.controller;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.wqmei.R;
import com.wqmei.adapter.MusicAdapter;
import com.wqmei.entity.Music;
import com.wqmei.util.SongUtil;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchActivity extends AppCompatActivity implements MediaPlayer.OnBufferingUpdateListener,MediaPlayer.OnCompletionListener,MediaPlayer.OnPreparedListener{

    public static Context context;//上下文
    private MediaPlayer mediaPlayer;//音乐播放
    private Button searchButton;//搜索按钮
    private EditText searchText;//搜索的字符串
    private ListView listView;//歌曲列表
    private InputMethodManager inputMethodManager;//输入法
    private RequestQueue requestQueue;//请求队列
    private Button pauseBtn;

    Thread olderThread = null;
    ThreadLocal<Thread> threadLocal = new ThreadLocal<>();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        /*http://music.163.com/song/media/outer/url?id=ID数字.mp3*/
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        requestQueue = Volley.newRequestQueue(SearchActivity.this);

        //获取输入法
        inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        initListView();
        initPauseBtn();
        initSearchModule();
    }

    private void initPauseBtn()
    {
        //初始化
        pauseBtn = findViewById(R.id.pause_btn);
        pauseBtn.setOnClickListener((v)->
        {
            if ("暂停".equals(pauseBtn.getText()))
            {
                if (mediaPlayer != null && mediaPlayer.isPlaying())
                {
                    mediaPlayer.pause();
                    pauseBtn.setText("播放");
                }
            } else if ("播放".equals(pauseBtn.getText()))
            {
                if (mediaPlayer != null && !mediaPlayer.isPlaying())
                {
                    mediaPlayer.start();
                    pauseBtn.setText("暂停");
                }
            }

        });
    }

    /**
     * 初始化按钮和搜索模块
     */
    private void initSearchModule()
    {
        //初始化，绑定匿名函数
        searchButton = findViewById(R.id.search_btn);
        searchText = findViewById(R.id.search_text);
        searchButton.setOnClickListener((v)->{
            String searchStr = String.valueOf(searchText.getText());
            //不为空触发搜索
            if (!"null".equals(searchStr) && searchStr.length() > 0)
            {
                //收起输入法
                if (inputMethodManager != null && inputMethodManager.isActive())
                {
                    inputMethodManager.hideSoftInputFromWindow(searchText.getWindowToken(), 0);
                    //inputMethodManager.toggleSoftInput(0,InputMethodManager.HIDE_NOT_ALWAYS);
                }
                //搜索
                searchSongs(searchStr);
            }

        });
    }

    /**
     * 初始化ListView
     */
    private void initListView()
    {
        listView = findViewById(R.id.song_list);
    }


    private void setListView(List<Music> songList)
    {
        //添加适配器
        MusicAdapter musicAdapter = new MusicAdapter(SearchActivity.this,R.layout.list_view, songList);

        listView.setAdapter(musicAdapter);
        System.out.println("设置适配器");
    }

    /**
     * 搜索歌曲
     * @param keyword
     */
    private void searchSongs(String keyword)
    {

        System.out.println("查找开始");
        //查找歌曲
        List<Music> songList = this.getMusicList(keyword);
        System.out.println("查找结束 "+songList.size());

        /**
         * 设定点击播放音乐
         */
        listView.setOnItemClickListener((parent, view, position, id) ->
        {
            /*new Thread(()->
            {
                synchronized (SearchActivity.this)
                {
                    threadLocal.set(Thread.currentThread());
                    if (olderThread != null)
                    {
                        olderThread.interrupt();
                    }
                    olderThread = Thread.currentThread();
                }*/

                //使用ui线程进行调用
                //Looper.prepare();
                Music music = (Music) parent.getItemAtPosition(position);
                Toast.makeText(SearchActivity.this, "开始播放： "+music.getName(), Toast.LENGTH_SHORT).show();
                if (mediaPlayer != null)
                {
                    mediaPlayer.stop();
                    mediaPlayer.reset();
                }else
                {
                    mediaPlayer = new MediaPlayer();
                    mediaPlayer.setAudioStreamType(AudioManager.STREAM_SYSTEM);
                    mediaPlayer.setOnBufferingUpdateListener(SearchActivity.this);
                    mediaPlayer.setOnPreparedListener(SearchActivity.this);
                }
                System.out.println(music.toString());
                try
                {
                    mediaPlayer.setDataSource("http://music.163.com/song/media/outer/url?id="+music.getId()+".mp3");
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                } catch (IOException e)
                {
                    e.printStackTrace();
                }

                //Looper.loop();
            //}).start();

        });
    }

    /**
     * 清空ListView
     * @param listView
     */
    private void clearListView(ListView listView)
    {
        List<Music> musicList = new ArrayList<>();
        MusicAdapter musicAdapter = new MusicAdapter(SearchActivity.this,listView.getId(),musicList);
        listView.setAdapter(musicAdapter);
    }


    /**
     * 查找歌曲
     * @param keyword
     * @return
     */
    private List<Music> getMusicList(String keyword)
    {
        List<Music> musicList = new ArrayList<>(20);
        //组合请求
        StringRequest stringRequest = null;
        try
        {
            stringRequest = new StringRequest(Request.Method.POST,"http://music.163.com/api/search/pc?" +
                    "?limit=10&offset=0&type=1&s="+ URLEncoder.encode(keyword,"utf-8"),
                    response ->
                    {
                        System.out.println("---------接收到响应-------------");
                        System.out.println("响应长度"+response.length());
                        //System.out.println(response);
                        //解析响应
                        JSONObject jsonObject = JSONObject.parseObject(response);
                        //获取歌曲列表
                        JSONArray jsonArray = ((JSONObject) jsonObject.get("result")).getJSONArray("songs");
                        //获取歌曲信息
                        SongUtil.getSongInfo(jsonArray, musicList);
                        System.out.println("获取到全部歌曲信息");

                        System.out.println("准备设置ListView");
                        setListView(musicList);
                    }, error -> Log.e("TAG", error.getMessage(), error))
            {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError
                {
                    Map<String, String> map = new HashMap<>();
                    map.put("referer", "http://music.163.com");
                    map.put("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/66.0.3359.117 Safari/537.36");
                    return map;
                }
            };
            requestQueue.add(stringRequest);
        } catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
        System.out.println("即将返回外部函数");
        return musicList;
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
