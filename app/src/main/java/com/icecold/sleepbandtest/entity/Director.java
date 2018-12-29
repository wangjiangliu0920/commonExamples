package com.icecold.sleepbandtest.entity;

/**
 *
 * Created by icecold_laptop_2 on 2018/8/8.
 */

public class Director {
    /**
     * alt : https://movie.douban.com/celebrity/1047973/
     * avatars : {"small":"https://img3.doubanio.com/view/celebrity/s_ratio_celebrity/public/p230.webp","large":"https://img3.doubanio.com/view/celebrity/s_ratio_celebrity/public/p230.webp","medium":"https://img3.doubanio.com/view/celebrity/s_ratio_celebrity/public/p230.webp"}
     * name : 弗兰克·德拉邦特
     * id : 1047973
     */

    private String alt;
    private AvatarImage avatars;
    private String name;
    private String id;

    public String getAlt() {
        return alt;
    }

    public void setAlt(String alt) {
        this.alt = alt;
    }

    public AvatarImage getAvatars() {
        return avatars;
    }

    public void setAvatars(AvatarImage avatars) {
        this.avatars = avatars;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
