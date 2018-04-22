package com.wqmei.entity;


public class Music
{
    private Integer id;

    private String name;

    private String singer;

    private String lyrics;

    private String imageUrl;

    public Music()
    {
    }

    public Music(Integer id, String name, String singer, String lyrics, String imageUrl)
    {
        this.id = id;
        this.name = name;
        this.singer = singer;
        this.lyrics = lyrics;
        this.imageUrl = imageUrl;
    }

    @Override
    public String toString()
    {
        return "Music{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", singer='" + singer + '\'' +
                ", lyrics='" + lyrics + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                '}';
    }

    public String getImageUrl()
    {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl)
    {
        this.imageUrl = imageUrl;
    }

    public Integer getId()
    {
        return id;
    }

    public void setId(Integer id)
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
