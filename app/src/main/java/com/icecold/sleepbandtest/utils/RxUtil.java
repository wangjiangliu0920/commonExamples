package com.icecold.sleepbandtest.utils;

import com.icecold.sleepbandtest.entity.DoubanResult;

import org.reactivestreams.Publisher;

import io.reactivex.Flowable;
import io.reactivex.FlowableTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 *
 * Created by icecold_laptop_2 on 2018/8/8.
 */

public class RxUtil {

    public static <T> FlowableTransformer<T, T> normalSchedulers(){
        return new FlowableTransformer<T, T>() {
            @Override
            public Publisher<T> apply(Flowable<T> observable) {
                return observable.subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread());
            }
        };
    }
    public static <T> Function<DoubanResult<T>,T> handleDoubanResult(){
        return new Function<DoubanResult<T>, T>() {
            @Override
            public T apply(DoubanResult<T> doubanResult) throws Exception {
                return doubanResult.getMovie();
            }
        };
    }
}
