package com.icecold.sleepbandtest.network;

import android.util.Log;

import com.icecold.sleepbandtest.MyApplication;
import com.icecold.sleepbandtest.utils.NetworkUtils;

import java.io.IOException;

import okhttp3.CacheControl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 *
 * Created by icecold_laptop_2 on 2018/8/10.
 */

public class CacheInterceptor implements Interceptor {
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        //网络不可用的话,全部用缓存
        if (!NetworkUtils.isNetworkAvaliable(MyApplication.getInstance())) {
            request = request.newBuilder()
                    .cacheControl(CacheControl.FORCE_CACHE)
                    .build();
            Log.i( CacheNetworkInterceptor.class.getSimpleName(),"no network ");
        }

        return chain.proceed(request);
    }
}
