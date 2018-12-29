package com.icecold.sleepbandtest.entity;

import com.google.gson.annotations.SerializedName;

/**
 *
 * Created by icecold_laptop_2 on 2018/8/20.
 */

public class PillowFileDownloadData {

    @SerializedName("periodId")
    private String periodid;
    private int start;
    private int end;
    private int date;


    public void setPeriodid(String periodid) {
        this.periodid = periodid;
    }
    public String getPeriodid() {
        return periodid;
    }


    public void setStart(int start) {
        this.start = start;
    }
    public int getStart() {
        return start;
    }


    public void setEnd(int end) {
        this.end = end;
    }
    public int getEnd() {
        return end;
    }


    public void setDate(int date) {
        this.date = date;
    }
    public int getDate() {
        return date;
    }
}
