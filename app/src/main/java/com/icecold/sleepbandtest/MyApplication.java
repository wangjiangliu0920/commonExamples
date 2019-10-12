package com.icecold.sleepbandtest;

import android.app.Application;
import android.content.Context;

import com.icecold.sleepbandtest.common.BluetoothDeviceManager;
import com.icecold.sleepbandtest.db.DbCore;
import com.icecold.sleepbandtest.utils.Constant;
import com.polidea.rxandroidble2.RxBleClient;
import com.polidea.rxandroidble2.internal.RxBleLog;
import com.vise.xsnow.http.ViseHttp;

import java.util.concurrent.TimeUnit;

import okhttp3.logging.HttpLoggingInterceptor;

/**
 *
 */

public class MyApplication extends Application {

    private static MyApplication instance;
    private RxBleClient rxBleClient;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        //初始化另一个库的蓝牙
        rxBleClient = RxBleClient.create(this);
        RxBleClient.setLogLevel(RxBleLog.DEBUG);
        //初始化蓝牙
        BluetoothDeviceManager.getInstance().init(this);
        //初始化数据库
        DbCore.init(this);
        //打开调试
        DbCore.enableQueryBuilderLog();

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
        /*
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
        */
    }
    public static synchronized MyApplication getInstance(){
        return instance;
    }
    public static RxBleClient getRxBleClient(Context context){
        MyApplication applicationContext = (MyApplication) context.getApplicationContext();
        return applicationContext.rxBleClient;
    }
}
