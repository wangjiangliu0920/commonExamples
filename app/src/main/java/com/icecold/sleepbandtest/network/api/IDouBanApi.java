package com.icecold.sleepbandtest.network.api;

import com.icecold.sleepbandtest.entity.DoubanResult;
import com.icecold.sleepbandtest.entity.Movie;

import java.util.List;

import io.reactivex.Flowable;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 *
 * Created by icecold_laptop_2 on 2018/8/8.
 */

public interface IDouBanApi {

    String BASE_URL_DOUBAN = "https://api.douban.com/v2/movie/";

    @GET("top250")
    Flowable<DoubanResult<List<Movie>>> getTopMovies(@Query("start") int start, @Query("count") int count);

}
