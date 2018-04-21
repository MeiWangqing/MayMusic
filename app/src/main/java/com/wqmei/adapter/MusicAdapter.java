package com.wqmei.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.wqmei.R;
import com.wqmei.entity.Music;

import java.util.List;

/**
 * author: wqmei
 * date: 2018/4/21 15:11
 * description:适配器
 */
public class MusicAdapter extends ArrayAdapter<Music>
{
    private int resourceId;

    /**
     *
     * @param context  上下文
     * @param resource 子布局id
     * @param objects  数据
     */
    public MusicAdapter(@NonNull Context context, int resource, @NonNull List<Music> objects)
    {
        super(context, resource, objects);
        resourceId = resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent)
    {
        Music music = getItem(position);
        //为子项加载布局
        View view = LayoutInflater.from(getContext()).inflate(resourceId, null);
        //歌曲名
        TextView songNameView = view.findViewById(R.id.song_name);
        songNameView.setText(music.getName());
        //歌手名
        TextView singerNameView = view.findViewById(R.id.singer_name);
        singerNameView.setText(music.getSinger());
        return view;
    }
}
