package com.icecold.sleepbandtest.common;

import android.content.Context;

import com.icecold.sleepbandtest.MyApplication;
import com.polidea.rxandroidble2.RxBleDevice;

/**
 * @Description: RxAndroidBle管理多个蓝牙设备
 * @author: icecold_laptop_2
 * @date: 2019/1/11
 */

public class BleClientManager {

    private static BleClientManager bleManagerInstance;
    private RxBleDevice rxBleDevice;

    private BleClientManager() {

    }

    public BleClientManager getInstance(){
        if (bleManagerInstance == null) {
            synchronized (BleClientManager.class){
                if (bleManagerInstance == null){
                    bleManagerInstance = new BleClientManager();
                }
            }
        }
        return bleManagerInstance;
    }

    public RxBleDevice getBleDevice(Context context,String macAddress){
        rxBleDevice = MyApplication.getRxBleClient(context).getBleDevice(macAddress);
        return rxBleDevice;
    }
}
