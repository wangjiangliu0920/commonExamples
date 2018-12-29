package com.icecold.sleepbandtest.network;

import com.icecold.sleepbandtest.network.api.SleepBandApi;
import com.icecold.sleepbandtest.utils.RxRetrofitClient;

import io.reactivex.Observable;
import okhttp3.RequestBody;

/**
 * @Description: 带子相关的业务请求
 * @author: icecold_laptop_2
 * @date: 2018/9/4
 */

public class BandLoader {

    private final SleepBandApi sleepBandApi;

    public BandLoader() {
        sleepBandApi = RxRetrofitClient.getInstance().create(SleepBandApi.class);
    }

    public Observable<RequestBody> getVersion(String model, String hw, String fw){
        return sleepBandApi.getRemoteVersion(model,hw,fw);
    }
}
