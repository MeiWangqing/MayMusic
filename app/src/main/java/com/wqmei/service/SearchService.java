package com.wqmei.service;

import com.android.volley.RequestQueue;
import com.wqmei.controller.SearchActivity;
import com.wqmei.entity.Music;

/**
 * author: wqmei
 * date: 2018/4/22 9:35
 * description:
 */
public interface SearchService
{
    void searchSongsByKeyword(String keyword, RequestQueue requestQueue, SearchActivity searchActivity);

    void searchLyric(RequestQueue requestQueue, Music music);
}
