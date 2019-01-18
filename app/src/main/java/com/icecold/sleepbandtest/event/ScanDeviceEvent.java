package com.icecold.sleepbandtest.event;

import com.polidea.rxandroidble2.RxBleDevice;

/**
 * @Description: 蓝牙搜索设备事件
 * @author: icecold_laptop_2
 * @date: 2019/1/14
 */

public class ScanDeviceEvent {
    private boolean isScanTimeout;
    private boolean scanSuccess;
    private RxBleDevice rxBleDevice;

    public boolean isScanTimeout() {
        return isScanTimeout;
    }

    public ScanDeviceEvent setScanTimeout(boolean scanTimeout) {
        isScanTimeout = scanTimeout;
        return this;
    }

    public boolean isScanSuccess() {
        return scanSuccess;
    }

    public ScanDeviceEvent setScanSuccess(boolean scanSuccess) {
        this.scanSuccess = scanSuccess;
        return this;
    }

    public RxBleDevice getRxBleDevice() {
        return rxBleDevice;
    }

    public ScanDeviceEvent setRxBleDevice(RxBleDevice rxBleDevice) {
        this.rxBleDevice = rxBleDevice;
        return this;
    }
}
