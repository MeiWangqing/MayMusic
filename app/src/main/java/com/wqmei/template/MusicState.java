package com.wqmei.template;

/**
 * author: wqmei
 * date: 2018/4/22 14:49
 * description:
 */
public enum MusicState
{
    //两种状态
    play,stop;
    public static MusicState state = MusicState.stop;

    public static void changeState()
    {
        if (state == play)
        {
            state = stop;
        } else if (state == stop)
        {
            state = play;
        }
    }
}
