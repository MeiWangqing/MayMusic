package com.wqmei.util;

import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.wqmei.controller.SearchActivity;
import com.wqmei.entity.Music;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * author: wqmei
 * date: 2018/4/21 15:13
 * description:
 */
public class SongUtil
{
    /**
     * 获取音乐列表
     * @param keyword
     * @return
     */
    public static List<Music> getMusicList(String keyword)
    {
        List<Music> musicList = new ArrayList<>(20);

        RequestQueue requestQueue = Volley.newRequestQueue(SearchActivity.context);
        //组合请求
        StringRequest stringRequest = null;
        try
        {
            stringRequest = new StringRequest(Request.Method.POST,"http://music.163.com/api/search/pc?" +
                    "?limit=10&offset=0&type=1&s="+ URLEncoder.encode(keyword,"utf-8"),
                    response ->
                    {
                        System.out.println("---------接收到响应-------------");
                        System.out.println(response.length());
                        System.out.println(response);
                        //解析响应
                        JSONObject jsonObject = JSON.parseObject(response);
                        //获取歌曲列表
                        JSONArray jsonArray = ((JSONObject) jsonObject.get("result")).getJSONArray("songs");
                        //获取歌曲信息
                        getSongInfo(jsonArray, musicList);
                    }, error -> Log.e("TAG", error.getMessage(), error));
        } catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
        requestQueue.add(stringRequest);
        return musicList;
    }

    /**
     * 获取歌曲信息
     * @param jsonArray
     * @param musicList
     * @return
     */
    private static List<Music> getSongInfo(JSONArray jsonArray,List<Music> musicList)
    {
        System.out.println("进入解析函数");
        for (int i = 0; i < jsonArray.size(); i++)
        {
            Music music = new Music();
            JSONObject song = (JSONObject) jsonArray.get(i);
            //设置id
            music.setId(song.getInteger("id"));
            //设置歌曲名称
            music.setName(song.getString("name"));
            //获取歌手列表,遍历
            music.setSinger(getSingerNameFromJsonArray(song.getJSONArray("artists")));
            musicList.add(music);
        }
        return musicList;
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
            JSONObject jsonObject = (JSONObject) jsonArray.get(i);
            String singerName = jsonObject.getString("name");
            stringBuffer.append(singerName).append("/");
        }
        //删除末尾的斜杠
        stringBuffer.deleteCharAt(stringBuffer.length() - 1);
        return stringBuffer.toString();
    }
}