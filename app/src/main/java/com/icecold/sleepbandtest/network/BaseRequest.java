package com.icecold.sleepbandtest.network;

import com.icecold.sleepbandtest.entity.PillowFileDownloadData;

import java.util.List;

/**
 *
 * Created by icecold_laptop_2 on 2018/8/20.
 */

public class BaseRequest {

    /**
     * code : 1
     * status : OK
     * message : SUCCESS
     * data : [{"periodId":"af030ca2593c4c72954a49c95bfab610.0","start":1534733251,"end":1534744485,"date":1534694400}]
     */

    private int code;
    private String status;
    private String message;
    private List<PillowFileDownloadData> data;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<PillowFileDownloadData> getData() {
        return data;
    }

    public void setData(List<PillowFileDownloadData> data) {
        this.data = data;
    }
}
