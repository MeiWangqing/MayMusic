package com.wqmei.service;

import android.os.Handler;

import com.android.volley.RequestQueue;
import com.wqmei.controller.SearchActivity;
import com.wqmei.entity.Music;

import java.util.List;

/**
 * author: wqmei
 * date: 2018/4/22 9:35
 * description:
 */
public interface SongService
{
    List<Music> getSongList(String keyword, RequestQueue requestQueue, SearchActivity searchActivity);
}
