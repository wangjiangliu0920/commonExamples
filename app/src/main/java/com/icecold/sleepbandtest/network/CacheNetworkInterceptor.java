package com.icecold.sleepbandtest.network;

import android.text.TextUtils;

import com.vise.log.ViseLog;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 *
 * Created by icecold_laptop_2 on 2018/8/8.
 */

public class CacheNetworkInterceptor implements Interceptor {
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Response response = chain.proceed(request);

        String cacheControl = request.cacheControl().toString();
        ViseLog.i("cacheData = "+cacheControl);
        if (TextUtils.isEmpty(cacheControl)) {
            cacheControl = "public,max-age = 60";
        }
        return response.newBuilder()
                .header("Cache-Control",cacheControl)
                .removeHeader("Pragma")
                .build();
//        if (NetworkUtils.isNetworkAvaliable(MyApplication.getInstance())) {
//            //网络存在，设置缓存时间为0
//            int maxAge = 60 * 5;
//            response.newBuilder()
//                    .header("Cache-Control","public,max-age="+maxAge)
//                    .removeHeader("Pragma")//消除头信息，因为如果服务器不支持，会返回一些干扰信息，不清除下面无法生效
//                    .build();
//        }else {
//            //没有网络的情况下，设置超时时间是1天
//            int maxStale = 60 * 60 * 24;
//            response.newBuilder()
//                    .header("Cache-Control","public,only-if-cache,max-stale="+maxStale)
//                    .removeHeader("Pragma")
//                    .build();
//        }
//        return response;
    }
}
