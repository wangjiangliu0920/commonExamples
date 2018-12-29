package com.icecold.sleepbandtest.callBack;

import com.vise.baseble.ViseBle;
import com.vise.baseble.callback.scan.IScanCallback;
import com.vise.baseble.callback.scan.ScanCallback;
import com.vise.baseble.model.BluetoothLeDevice;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 * Created by icecold_laptop_2 on 2018/7/23.
 */

public class SingleMacFilterScanCallback extends ScanCallback {

    private AtomicBoolean hasFound = new AtomicBoolean(false);
    private String deviceMac;//指定设备Mac地址

    public SingleMacFilterScanCallback(IScanCallback scanCallback) {
        super(scanCallback);
    }


    public ScanCallback setDeviceMac(String deviceMac) {
        this.deviceMac = deviceMac;
        return this;
    }

    @Override
    public BluetoothLeDevice onFilter(BluetoothLeDevice bluetoothLeDevice) {
        BluetoothLeDevice tempDevice = null;
        if (!hasFound.get()) {
            if (bluetoothLeDevice != null && bluetoothLeDevice.getAddress() != null && deviceMac != null
                    && deviceMac.equalsIgnoreCase(bluetoothLeDevice.getAddress().trim())) {
                hasFound.set(true);
                isScanning = false;
                ViseBle.getInstance().stopScan(SingleMacFilterScanCallback.this);
                tempDevice = bluetoothLeDevice;
                bluetoothLeDeviceStore.addDevice(bluetoothLeDevice);
                scanCallback.onScanFinish(bluetoothLeDeviceStore);
            }
        }
        return tempDevice;
    }
}
