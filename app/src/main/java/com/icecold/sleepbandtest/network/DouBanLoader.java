package com.icecold.sleepbandtest.network;

import com.icecold.sleepbandtest.entity.Movie;
import com.icecold.sleepbandtest.network.api.IDouBanApi;
import com.icecold.sleepbandtest.utils.RxRetrofitClient;
import com.icecold.sleepbandtest.utils.RxUtil;

import java.util.List;

import io.reactivex.Flowable;

/**
 * @Description: 豆瓣相关的业务
 * @author: icecold_laptop_2
 * @date: 2018/9/4
 */

public class DouBanLoader {

    private final IDouBanApi douBanApi;

    public DouBanLoader() {
        douBanApi = RxRetrofitClient.getInstance().create(IDouBanApi.class);
    }
    public Flowable<List<Movie>> requestTop250Movies(int page, int count){
        return douBanApi.getTopMovies(page * 10,count)
                .map( RxUtil.<List<Movie>>handleDoubanResult())
                .compose(RxUtil.<List<Movie>>normalSchedulers());
    }
}
