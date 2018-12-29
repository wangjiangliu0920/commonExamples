package com.icecold.sleepbandtest.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 *
 * Created by icecold_laptop_2 on 2018/8/8.
 */

public class NetworkUtils {
    /**
     * 检测网络是否可用
     * @param context 上下文
     * @return true 可用 false 不可用
     */
    public static boolean isNetworkAvaliable(Context context){
        if (context != null) {
            ConnectivityManager manager = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            if (manager == null) {
                return false;
            }
            NetworkInfo networkInfo = manager.getActiveNetworkInfo();
            return !(networkInfo == null || !networkInfo.isAvailable());
        }
        return false;
    }
}
