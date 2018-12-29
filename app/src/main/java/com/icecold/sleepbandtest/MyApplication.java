package com.icecold.sleepbandtest;

import android.app.Application;
import android.os.Bundle;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.icecold.sleepbandtest.common.BluetoothDeviceManager;
import com.icecold.sleepbandtest.utils.Constant;
import com.vise.xsnow.http.ViseHttp;

import java.util.concurrent.TimeUnit;

import okhttp3.logging.HttpLoggingInterceptor;

/**
 *
 */

public class MyApplication extends Application {

    private static MyApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        //初始化蓝牙
        BluetoothDeviceManager.getInstance().init(this);

        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        ViseHttp.init(this);
        ViseHttp.CONFIG()
                .baseUrl(Constant.BASE_URL)
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .connectTimeout(15, TimeUnit.SECONDS)
                .retryCount(2)
                .retryDelayMillis(1000)
                .interceptor(loggingInterceptor);
        RequestQueue mQueue = Volley.newRequestQueue(this);
        Bundle bundle = new Bundle();
        StringRequest stringRequest = new StringRequest(Request.Method.GET, "", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        stringRequest.setShouldCache(true);
        mQueue.add(stringRequest);
    }
    public static synchronized MyApplication getInstance(){
        return instance;
    }
}
