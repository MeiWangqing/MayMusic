package com.wqmei.controller;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.wqmei.R;
import com.wqmei.adapter.MusicAdapter;
import com.wqmei.entity.Music;
import com.wqmei.service.SearchService;
import com.wqmei.service.impl.SearchServiceImpl;
import com.wqmei.template.MusicState;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity implements MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener
{
    public static Context context;//上下文
    public static MediaPlayer mediaPlayer = null;//音乐播放
    public static Music currentMusic = null;//当前音乐

    private Button searchButton;//搜索按钮
    private EditText searchText;//搜索的字符串
    private ListView listView;//歌曲列表
    private InputMethodManager inputMethodManager;//输入法
    private RequestQueue requestQueue;//请求队列
    private Button pauseBtn;//暂停按钮
    private ImageView songImgBottom;//底部歌曲图片
    private TextView songInfoTextBottom;//底部歌曲信息
    private Handler handler = new Handler();
    private SearchService searchService = new SearchServiceImpl();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        /*http://music.163.com/song/media/outer/url?id=ID数字.mp3*/
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        context = getApplicationContext();
        requestQueue = Volley.newRequestQueue(SearchActivity.this);//请求队列
        //歌曲信息
        songImgBottom = findViewById(R.id.song_img_bottom);//底部图片
        songInfoTextBottom = findViewById(R.id.song_info_bottom);//底部文字
        System.out.println("是否获得焦点: " + songInfoTextBottom.isFocusable());
        listView = findViewById(R.id.song_list);//歌曲列表
        pauseBtn = findViewById(R.id.pause_btn);//暂停按钮
        searchButton = findViewById(R.id.search_btn);//搜索按钮
        searchText = findViewById(R.id.search_text);//搜索文本
        //获取输入法
        inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);


        //绑定各种事件的函数
        initPauseBtn();
        initSearchModule();
        initListView();
        initBottomGoToNextPage();
    }

    /**
     * 绑定跳转
     */
    private void initBottomGoToNextPage()
    {
        songImgBottom.setOnClickListener((v)->
        {
            if (currentMusic != null)
            {
                Intent intent = new Intent(SearchActivity.this,MusicActivity.class);
                startActivityForResult(intent, 1);
            }
        });
        songInfoTextBottom.setOnClickListener((v)->
        {
            if (currentMusic != null)
            {
                Intent intent = new Intent(SearchActivity.this, MusicActivity.class);
                startActivityForResult(intent, 1);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 1)
        {
            //更改按钮
            if (MusicState.state == MusicState.stop)
            {
                pauseBtn.setText("播放");
            }else
            {
                pauseBtn.setText("暂停");
            }

        }
    }

    /**
     * 初始化ListView,设定item点击事件
     */
    private void initListView()
    {
        /**
         * 设定点击播放音乐
         */
        listView.setOnItemClickListener((parent, view, position, id) ->
        {
            new Thread(() ->
            {
                Music music = (Music) parent.getItemAtPosition(position);
                if (music != null && music.getId() != null)
                {
                    currentMusic = music;
                    if (mediaPlayer != null)
                    {
                        mediaPlayer.stop();
                        mediaPlayer.reset();
                    } else
                    {
                        mediaPlayer = new MediaPlayer();
                        mediaPlayer.setAudioStreamType(AudioManager.STREAM_SYSTEM);
                        mediaPlayer.setOnBufferingUpdateListener(SearchActivity.this);
                        mediaPlayer.setOnPreparedListener(SearchActivity.this);
                    }
                    Log.i("INFO", "现在播放的歌曲信息: " + music.toString());
                    handler.post(() ->
                    {
                        try
                        {
                            mediaPlayer.setDataSource("http://music.163.com/song/media/outer/url?id=" + music.getId() + ".mp3");
                            mediaPlayer.prepare();
                            mediaPlayer.start();
                            showToast(music);
                            resetPause();
                            setBottomInfo(music.getImageUrl(), music.getName() + " - " + music.getSinger());

                        } catch (IOException e)
                        {
                            e.printStackTrace();
                        }
                    });
                }
            }).start();

        });
    }


    private void showToast(Music music)
    {
        //弹出
        Toast.makeText(SearchActivity.this, "开始播放： " + music.getName(), Toast.LENGTH_SHORT).show();
    }

    /**
     * 初始化暂停按钮
     */
    private void initPauseBtn()
    {
        //暂停播放的转化
        pauseBtn.setOnClickListener((v) ->
        {
            Log.i("INFO", "搜索页面点击暂停按钮,此时状态为: "+MusicState.state);
            //执行播放
            if (MusicState.state == MusicState.stop)
            {
                if (mediaPlayer != null)
                {
                    mediaPlayer.start();
                    pauseBtn.setText("暂停");
                    //切换状态
                    MusicState.changeState();
                }
            } else if (MusicState.state == MusicState.play)
            {
                //执行暂停
                if (mediaPlayer != null && mediaPlayer.isPlaying())
                {
                    mediaPlayer.pause();
                    pauseBtn.setText("播放");
                    MusicState.changeState();
                }
            }
        });
    }

    /**
     *切歌,重置按钮状态
     */
    private void resetPause()
    {
        MusicState.state = MusicState.play;
        pauseBtn.setText("暂停");
    }

    /**
     * 设置底部的图片和跑马灯
     *
     * @param imageUrl
     * @param songInfo
     */
    private void setBottomInfo(String imageUrl, String songInfo)
    {
        String spaceStr = "        ";
        //设置文字
        songInfoTextBottom.setText(songInfo);

        new Thread(() ->
        {
            HttpURLConnection connection = null;
            InputStream inputStream = null;
            Bitmap bitmap = null;
            try
            {
                URL url = new URL(imageUrl);
                //获取http连接
                connection = (HttpURLConnection) url.openConnection();
                //设置超时
                connection.setConnectTimeout(6000);
                //设置可以读取流
                connection.setDoInput(true);
                connection.setUseCaches(false);
                Log.i("songInfo", imageUrl);
                //connection.connect();
                //获取数据流
                inputStream = connection.getInputStream();
                //读取文件
                bitmap = BitmapFactory.decodeStream(inputStream);
                //设置属性
                currentMusic.setBitmap(bitmap);
            } catch (MalformedURLException e)
            {
                e.printStackTrace();
            } catch (IOException e)
            {
                e.printStackTrace();
            } finally
            {
                if (connection != null)
                {
                    connection.disconnect();
                }
                if (inputStream != null)
                {
                    try
                    {
                        inputStream.close();
                    } catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
            //发送请求给主线程
            Bitmap finalBitmap = bitmap;
            handler.post(() ->
            {
                System.out.println("开始设置图片");
                //设置图片
                songImgBottom.setImageBitmap(finalBitmap);
            });
        }).start();


    }

    /**
     * 初始化按钮和搜索模块
     */
    private void initSearchModule()
    {
        //绑定点击
        searchButton.setOnClickListener((v) ->
        {
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
                //显示加载
                loadingListView(listView);
                //搜索
                searchSongs(searchStr);
            }

        });
        //绑定回车搜索
        searchText.setOnEditorActionListener((v, actionId, event) ->
        {
            if (actionId == EditorInfo.IME_ACTION_SEARCH)
            {
                Log.i("INFO", "绑定通过回车执行搜索");
                searchButton.performClick();
            }
            return false;
        });
    }


    /**
     * 设置歌曲列表
     *
     * @param songList
     */
    public void setListView(List<Music> songList)
    {
        //添加适配器
        MusicAdapter musicAdapter = new MusicAdapter(SearchActivity.this, R.layout.list_view, songList);
        listView.setAdapter(musicAdapter);
        Log.i("INFO", "设置适配器");
    }

    /**
     * 搜索歌曲
     *
     * @param keyword
     */
    private void searchSongs(String keyword)
    {
        //查找歌曲
        searchService.searchSongsByKeyword(keyword, requestQueue, this);
    }

    /**
     * 显示等待字样
     *
     * @param listView
     */
    private void loadingListView(ListView listView)
    {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < 10; i++)
        {
            list.add("正在搜索中,请稍后......");
        }
        ListAdapter adapter = new ArrayAdapter<String>(SearchActivity.this, android.R.layout.simple_list_item_1, list);
        listView.setAdapter(adapter);
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
