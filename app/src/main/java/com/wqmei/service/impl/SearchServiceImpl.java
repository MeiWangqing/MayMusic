package com.wqmei.service.impl;

import android.content.Intent;
import android.util.Log;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.wqmei.controller.SearchActivity;
import com.wqmei.entity.Music;
import com.wqmei.service.SearchService;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * author: wqmei
 * date: 2018/4/22 9:36
 * description:
 */
public class SearchServiceImpl implements SearchService
{
    /**
     * 查找歌曲
     * @param keyword
     * @return
     */
    @Override
    public void searchSongsByKeyword(String keyword, RequestQueue requestQueue, SearchActivity searchActivity)
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
                        JSONArray jsonArray = jsonObject.getJSONObject("result").getJSONArray("songs");
                        //获取歌曲信息
                        getSongInfo(jsonArray, musicList);
                        System.out.println("获取到全部歌曲信息");
                        System.out.println("准备设置ListView");
                        searchActivity.setListView(musicList);
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
    }

    /**
     * 搜索歌词
     * @param requestQueue
     * @param music
     */
    @Override
    public void searchLyric(RequestQueue requestQueue, Music music)
    {
        Log.i("INFO","进入解析歌词");
        Integer songId = music.getId();
        String url = "http://music.163.com/api/song/media?id=" + songId;
        StringRequest stringRequest = new StringRequest(url,response ->
        {
            //解析
            JSONObject jsonObject = JSONObject.parseObject(response);
            String lyricStr = jsonObject.getString("lyric");
            Matcher matcher = Pattern.compile("\\[\\d{2}:\\d{2}.\\d{2}\\](.+?)\\\\n").matcher(lyricStr);
            while (matcher.find())
            {
                Log.i("INFO",matcher.group(1));
            }
        },error -> Log.e("ERROR", error.getMessage(),error));
        requestQueue.add(stringRequest);
    }


    /**
     * 获取歌曲信息
     * @param jsonArray
     * @param musicList
     * @return
     */
    private static List<Music> getSongInfo(JSONArray jsonArray, List<Music> musicList)
    {
        Log.i("INFO","进入解析函数");
        for (int i = 0; i < jsonArray.size(); i++)
        {
            Log.i("INFO","正在解析第"+i+"条记录...");
            Music music = new Music();
            JSONObject song = jsonArray.getJSONObject(i);
            //设置id
            music.setId(song.getInteger("id"));
            //设置歌曲名称
            music.setName(song.getString("name"));
            //设置图片url
            music.setImageUrl(getImageUrl(song));
            //获取歌手列表,遍历
            music.setSinger(getSingerNameFromJsonArray(song.getJSONArray("artists")));
            musicList.add(music);
        }
        Log.i("INFO","解析完成");
        return musicList;
    }

    /**
     * 获取图片url
     * @param song
     * @return
     */
    private static String getImageUrl(JSONObject song)
    {
        return song.getJSONObject("album").getString("picUrl");
    }


    /**
     * 获取歌手名
     * @param jsonArray
     * @return
     */
    private static String getSingerNameFromJsonArray(JSONArray jsonArray)
    {
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0,length = jsonArray.size(); i < length; i++)
        {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            String singerName = jsonObject.getString("name");
            stringBuffer.append(singerName).append("/");
        }
        //删除末尾的斜杠
        stringBuffer.deleteCharAt(stringBuffer.length() - 1);
        return stringBuffer.toString();
    }

}
