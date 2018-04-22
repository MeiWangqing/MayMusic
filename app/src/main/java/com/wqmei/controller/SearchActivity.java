package com.wqmei.controller;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
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
import com.wqmei.service.SongService;
import com.wqmei.service.impl.SongServiceImpl;
import com.wqmei.util.SongUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchActivity extends AppCompatActivity implements MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener
{

    public static Context context;//上下文
    private MediaPlayer mediaPlayer;//音乐播放
    /**
     * 搜索按钮
     */
    private Button searchButton;//搜索按钮
    private EditText searchText;//搜索的字符串
    private ListView listView;//歌曲列表
    private InputMethodManager inputMethodManager;//输入法
    private RequestQueue requestQueue;//请求队列
    private Button pauseBtn;
    private ImageView songImgBottom;
    private TextView textView;
    private Handler handler = new Handler();
    private SongService songService = new SongServiceImpl();

    Thread olderThread = null;
    ThreadLocal<Thread> threadLocal = new ThreadLocal<>();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        /*http://music.163.com/song/media/outer/url?id=ID数字.mp3*/
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        //请求队列
        requestQueue = Volley.newRequestQueue(SearchActivity.this);
        //歌曲信息
        songImgBottom = findViewById(R.id.song_img_bottom);
        textView = findViewById(R.id.song_info_bottom);
        System.out.println("是否获得焦点: "+textView.isFocusable());
        //歌曲列表
        listView = findViewById(R.id.song_list);
        //初始化暂停按钮
        pauseBtn = findViewById(R.id.pause_btn);
        searchButton = findViewById(R.id.search_btn);//搜索按钮
        searchText = findViewById(R.id.search_text);//搜索文本
        //获取输入法
        inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        initPauseBtn();
        initSearchModule();
    }

    private void initPauseBtn()
    {
        pauseBtn.setOnClickListener((v) ->
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
     * 设置底部的图片和跑马灯
     * @param imageUrl
     * @param songInfo
     */
    private void setBottomInfo(String imageUrl,String songInfo)
    {
        String spaceStr = "        ";
        //设置文字
        textView.setText(songInfo+spaceStr+songInfo+spaceStr+songInfo+spaceStr);

        new Thread(()->
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

            } catch (MalformedURLException e)
            {
                e.printStackTrace();
            } catch (IOException e)
            {
                e.printStackTrace();
            }finally
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
            handler.post(()->
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
                Log.i("---", "通过回车执行搜索");
                searchButton.performClick();
            }
            return false;
        });
    }


    /**
     * 设置歌曲列表
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

        System.out.println("查找开始");
        //查找歌曲
        List<Music> songList = this.getMusicList(keyword);
        System.out.println("查找结束 " + songList.size());
        System.out.println("======是否设置点击属性======");
        System.out.println(listView.getOnItemClickListener());
        /**
         * 设定点击播放音乐
         */
        listView.setOnItemClickListener((parent, view, position, id) ->
        {
            //使用ui线程进行调用
            //Looper.prepare();
            Music music = (Music) parent.getItemAtPosition(position);
            if (music != null && music.getId() != null)
            {
                Toast.makeText(SearchActivity.this, "开始播放： " + music.getName(), Toast.LENGTH_SHORT).show();
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
                System.out.println(music.toString());
                try
                {
                    mediaPlayer.setDataSource("http://music.163.com/song/media/outer/url?id=" + music.getId() + ".mp3");
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                    setBottomInfo(music.getImageUrl(),music.getName()+" - "+music.getSinger());
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
            }

        });
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


    /**
     * 查找歌曲
     *
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
            stringRequest = new StringRequest(Request.Method.POST, "http://music.163.com/api/search/pc?" +
                    "?limit=10&offset=0&type=1&s=" + URLEncoder.encode(keyword, "utf-8"),
                    response ->
                    {
                        System.out.println("---------接收到响应-------------");
                        System.out.println("响应长度" + response.length());
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
                    map.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/66.0.3359.117 Safari/537.36");
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
