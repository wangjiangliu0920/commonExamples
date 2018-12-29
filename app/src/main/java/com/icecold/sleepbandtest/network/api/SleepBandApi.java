package com.icecold.sleepbandtest.network.api;

import io.reactivex.Observable;
import okhttp3.RequestBody;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.Streaming;

/**
 * @Description: 睡眠带子相关的api
 * @author: icecold_laptop_2
 * @date: 2018/9/4
 */

public interface SleepBandApi {

    @Streaming
    @GET("sleep_sensor/latest.txt")
    Observable<RequestBody> getRemoteVersion(@Query("model")String model, @Query("hw")String hw, @Query("fw")String fw);
}
