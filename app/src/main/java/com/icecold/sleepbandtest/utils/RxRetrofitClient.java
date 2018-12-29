package com.icecold.sleepbandtest.utils;

import com.icecold.sleepbandtest.MyApplication;
import com.icecold.sleepbandtest.network.CacheNetworkInterceptor;
import com.icecold.sleepbandtest.network.api.IDouBanApi;

import java.io.File;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.ConnectionSpec;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 *
 * Created by icecold_laptop_2 on 2018/8/8.
 */

public class RxRetrofitClient {

    private static final long DEFAULT_CONNECT_TIMEOUT = 15;
    private static final long DEFAULT_READ_TIMEOUT = 15;
    private static final long DEFAULT_WRITE_TIMEOUT = 15;
    private static final long CACHE_MAX_SIZE = 10 * 1024 * 1024;
//    private static final String BASE_URL_DOUBAN = "https://api.douban.com/v2/movie/";
    private static final String BASE_URL_DOUBAN = Constant.BASE_URL;
    private IDouBanApi douBanApi;
    private Retrofit mRetrofit;

    public static RxRetrofitClient getInstance(){

        return RxRetrofitClientMode.instance;
    }

    private RxRetrofitClient() {
        initClient();
    }

    private static class RxRetrofitClientMode{

        private static RxRetrofitClient instance = new RxRetrofitClient();

    }

    private void initClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                //超时设置
                .connectTimeout(DEFAULT_CONNECT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(DEFAULT_READ_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(DEFAULT_WRITE_TIMEOUT, TimeUnit.SECONDS)
                //错误重试
                .retryOnConnectionFailure(true)
                //支持https
                .connectionSpecs(Arrays.asList(ConnectionSpec.CLEARTEXT, ConnectionSpec.MODERN_TLS));
        //添加各种拦截器
        addInterceptor(builder);

        //创建Retrofit实例
        mRetrofit = new Retrofit.Builder()
                .client(builder.build())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .baseUrl(BASE_URL_DOUBAN)
                .build();

    }

    private void addInterceptor(OkHttpClient.Builder builder) {
        //添加http log
        HttpLoggingInterceptor logger = new HttpLoggingInterceptor();
        logger.setLevel(HttpLoggingInterceptor.Level.BODY);
        builder.addInterceptor(logger);
        builder.addInterceptor(new CacheNetworkInterceptor());
        //设置缓存策略
        File cacheFile = new File(MyApplication.getInstance().getExternalCacheDir(),"myCache");
        Cache cache = new Cache(cacheFile,CACHE_MAX_SIZE);
        builder.cache(cache);
    }



    /**
     * 获取对应的service
     * @param service
     * @param <T>
     * @return
     */
    public <T> T create(Class<T> service){
        return mRetrofit.create(service);
    }
}
