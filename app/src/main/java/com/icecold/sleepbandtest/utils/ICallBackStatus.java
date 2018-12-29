package com.icecold.sleepbandtest.utils;

/**
 * Created by icecold_laptop_2 on 2017/10/26.
 */

public class ICallBackStatus {

    public static final int BLE_SEARCHING = 0;
    public static final int CONNECTED_STATUS_OK = 1;
    public static final int DISCONNECT_STATUS = 2;
    public static final int STOP_SEARCH = 3;
    public static final int GET_BLE_ONE_DATA_OK = 4;
    public static final int SYNC_TIME_OK = 5;
    public static final int HAVE_PILLOW_DATA = 6;
    public static final int SYNC_TIME_FAIL = 7;
    public static final int SYNC_DATA_BLUETOOTH_ERROR = 8;
    public static final int SYNC_PILLOW_DATA_PROGRESS = 10;
    public static final int NO_PILLOW_DATA = 11;
    public static final int SYNCING_PILLOW_DATA = 12;
    public static final int CONNECTED_STATUS_FAIL = 13;
    public static final int GET_BLE_BATTERY_OK = 14;
    public static final int GET_BLE_BATTERY_FAIL = 15;
    public static final int GET_BLE_VERSION_OK = 16;
    public static final int GET_BLE_VERSION_FAIL = 17;
    public static final int START_SEARCH = 18;
    public static final int SEARCH_TIME_OUT = 19;
    public static final int REAL_TIME_DATA = 20;
    public static final int DEVICE_NOT_SUPPORTED_BLE = 21;
    public static final int SYNC_ALL_DATA_AND_UPLOAD_OK = 22;
    public static final int UNBIND_SERVICE_OK = 23;
    public static final int SYNC_DATA_NETWORK_REQUEST_FAIL = 24;
    public static final int GET_PILLOW_AUTH_SUCCESS = 29;
    public static final int GET_PILLOW_AUTH_FAIL = 30;
    public static final int BIND_SERVICE_OK = 39;

    public static final int DFU_DEVICE_NOT_CONNECTED = 31;//DFU设备未连接
    public static final int DFU_DEVICE_UPGRADE_OPERATION_STARTED = 32;//DFU设备升级操作已启动
    public static final int DFU_DEVICE_START_UPGRADE = 33;//DFU设备开始升级
    public static final int DFU_DEVICE_UPGRADE_SUCCESS = 34;//DFU设备升级完成
    public static final int DFU_DEVICE_UPGRADE_FAIL = 35;//DFU设备升级失败
    public static final int DFU_DEVICE_UPGRADING = 36;//DFU设备升级中

    public static final int DOWNLOAD_FILE_NETWORK_ERROR = 37;//下载文件网络错误
    public static final int DOWNLOAD_FILE_SUCCESS = 38;//下载文件成功
}
