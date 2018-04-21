package com.wqmei.entity;


public class Music
{
    private int id;

    private String name;

    private String singer;

    private String lyrics;

    public Music()
    {
    }

    public Music(int id, String name, String singer, String lyrics)
    {
        this.id = id;
        this.name = name;
        this.singer = singer;
        this.lyrics = lyrics;
    }

    @Override
    public String toString()
    {
        return "Music{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", singer='" + singer + '\'' +
                ", lyrics='" + lyrics + '\'' +
                '}';
    }

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getSinger()
    {
        return singer;
    }

    public void setSinger(String singer)
    {
        this.singer = singer;
    }

    public String getLyrics()
    {
        return lyrics;
    }

    public void setLyrics(String lyrics)
    {
        this.lyrics = lyrics;
    }
}
