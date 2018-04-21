package com.wqmei.util;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * author: 45295
 * date: 2018/4/21 19:49
 * description:
 */
public class HeaderStringRequest extends StringRequest
{
    public HeaderStringRequest(int method, String url, Response.Listener<String> listener, Response.ErrorListener errorListener)
    {
        super(method, url, listener, errorListener);
    }

    public HeaderStringRequest(String url, Response.Listener<String> listener, Response.ErrorListener errorListener)
    {
        super(url, listener, errorListener);
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError
    {
        Map<String, String> map = new HashMap<>();
        map.put("referer", "http://music.163.com");
        map.put("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/66.0.3359.117 Safari/537.36");
        return super.getHeaders();
    }
}
