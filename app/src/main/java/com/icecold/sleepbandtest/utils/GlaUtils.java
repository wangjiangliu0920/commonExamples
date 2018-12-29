package com.icecold.sleepbandtest.utils;




public class GlaUtils {

    public final static String PILLOW_SERV_UUID_BATT = "0000180f-0000-1000-8000-00805f9b34fb";
    public final static String PILLOW_CHAR_UUID_BLVL = "00002a19-0000-1000-8000-00805f9b34fb";
    public final static String PILLOW_SERV_UUID_INFO = "0000180a-0000-1000-8000-00805f9b34fb";
    public final static String PILLOW_CHAR_UUID_HVER = "00002a26-0000-1000-8000-00805f9b34fb";
    public final static String PEGASI_BRAINWAVE_SERVICE_UUID = "6e400001-b5a3-f393-e0a9-e50e24dcca9e";
    public final static String PEGASI_BRAINWAVE_TX_SERVICE_CHARACTERISTIC_UUID = "6e400003-b5a3-f393-e0a9-e50e24dcca9e";
    public final static String PILLOW_CHAR_UUID_DATA_ALWAYS = "6e400004-b5a3-f393-e0a9-e50e24dcca9e";
    public final static String PILLOW_BRAINWAVE_CHARACTERISTIC_WRITE = "6e400002-b5a3-f393-e0a9-e50e24dcca9e";
    public final static String PILLOW_CHAR_UUID_READ = "6e400005-b5a3-f393-e0a9-e50e24dcca9e";

    public final static String BAND_PEGASI_SERVICE_UUID = "8EEF0000-B45D-0CCD-B111-DBDBD494FB6E";
    public final static String BAND_PEGASI_SYNC_TIME_CHARACTERISTIC_UUID = "8EEF0001-B45D-0CCD-B111-DBDBD494FB6E";
    public final static String BAND_PEGASI_LOG_DATA_CHARACTERISTIC_UUID = "8EEF0002-B45D-0CCD-B111-DBDBD494FB6E";
    public final static String BAND_PEGASI_LIVE_MODE_CHARACTERISTIC_UUID = "8EEF0003-B45D-0CCD-B111-DBDBD494FB6E";
    public final static String BAND_PEGASI_CHANGER_STATUS_CHARACTERISTIC_UUID = "8EEF0004-B45D-0CCD-B111-DBDBD494FB6E";
    public final static String BAND_NORDIC_UART_SERVICE_UUID = "6E400001-B5A3-F393-E0A9-E50E24DCCA9E";
    public final static String BAND_DEVICE_INFO_SERVICE_UUID = "0000180a-0000-1000-8000-00805f9b34fb";
    public final static String BAND_BATTERY_SERVICE_UUID = "0000180f-0000-1000-8000-00805f9b34fb";

    public final static String PILLOW_DFU_COMMAND = "@DU";
    public final static int SEARCH_DFU_TIMEOUT = 10 * 1000;
    public final static String NULL_VALUE = "null";

    /**
     * 表示使能notify读取所有的数据
     */
    public static final int ENABLE_SYNC_DATA = 2;
    /**
     * 使能notify读取协议版本号
     */
    public static final int ENABLE_READ_PROTOCOL_VERSION = 1;
    /**
     * 协议版本号一
     */
    public static final int PROTOCOL_VERSION_IS_ONE = 1;
    /**
     * 协议版本号零
     */
    public static final int PROTOCOL_VERSION_IS_ZERO = 0;
    /**
     * 包含pk值
     */
    public static final int CONTAIN_PK_VALUE = 4;
    /**
     * 不包含pk值
     */
    public static final int NOT_CONTAIN_PK_VALUE = 5;

}
