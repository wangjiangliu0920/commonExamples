package com.icecold.sleepbandtest.entity;

/**
 *
 * Created by icecold_laptop_2 on 2018/8/8.
 */

public class AvatarImage {
    /**
     * small : https://img3.doubanio.com/view/celebrity/s_ratio_celebrity/public/p230.webp
     * large : https://img3.doubanio.com/view/celebrity/s_ratio_celebrity/public/p230.webp
     * medium : https://img3.doubanio.com/view/celebrity/s_ratio_celebrity/public/p230.webp
     */

    private String small;
    private String large;
    private String medium;

    public String getSmall() {
        return small;
    }

    public void setSmall(String small) {
        this.small = small;
    }

    public String getLarge() {
        return large;
    }

    public void setLarge(String large) {
        this.large = large;
    }

    public String getMedium() {
        return medium;
    }

    public void setMedium(String medium) {
        this.medium = medium;
    }
}
