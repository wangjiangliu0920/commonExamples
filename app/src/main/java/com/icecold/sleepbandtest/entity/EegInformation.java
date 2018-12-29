package com.icecold.sleepbandtest.entity;

/**
 * @Description: 脑电原始信息的数据
 * @author: icecold_laptop_2
 * @date: 2018/12/3
 */

public class EegInformation {
    private int meditation;
    private int attention;
    private int timeStamp;

    public int getMeditation() {
        return meditation;
    }

    public void setMeditation(int meditation) {
        this.meditation = meditation;
    }

    public int getAttention() {
        return attention;
    }

    public void setAttention(int attention) {
        this.attention = attention;
    }

    public int getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(int timeStamp) {
        this.timeStamp = timeStamp;
    }

    @Override
    public String toString() {
        return "EegInformation{" +
                "meditation=" + meditation +
                ", attention=" + attention +
                ", timeStamp=" + timeStamp +
                '}';
    }
}
