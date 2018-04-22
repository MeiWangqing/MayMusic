package com.wqmei.entity;


import android.graphics.Bitmap;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Music
{
    private Integer id;

    private String name;

    private String singer;

    private String lyrics;

    private String imageUrl;

    private Bitmap bitmap;
}
