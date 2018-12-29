package com.icecold.sleepbandtest;

import android.Manifest;
import android.bluetooth.BluetoothGattDescriptor;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Base64;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.icecold.sleepbandtest.common.BluetoothDeviceManager;
import com.icecold.sleepbandtest.common.ToastUtil;
import com.icecold.sleepbandtest.event.CallbackDataEvent;
import com.icecold.sleepbandtest.event.ConnectEvent;
import com.icecold.sleepbandtest.event.NotifyDataEvent;
import com.icecold.sleepbandtest.network.BandLoader;
import com.icecold.sleepbandtest.network.BaseRequest;
import com.icecold.sleepbandtest.utils.Constant;
import com.icecold.sleepbandtest.utils.FileUtil;
import com.icecold.sleepbandtest.utils.GlaUtils;
import com.icecold.sleepbandtest.utils.ParcelableUtil;
import com.icecold.sleepbandtest.utils.SPUtils;
import com.icecold.sleepbandtest.utils.Utils;
import com.icecold.sleepbandtest.widget.UncertainDialog;
import com.vise.baseble.ViseBle;
import com.vise.baseble.common.PropertyType;
import com.vise.baseble.core.BluetoothGattChannel;
import com.vise.baseble.model.BluetoothLeDevice;
import com.vise.baseble.utils.BleUtil;
import com.vise.baseble.utils.HexUtil;
import com.vise.log.ViseLog;
import com.vise.log.inner.LogcatTree;
import com.vise.xsnow.event.BusManager;
import com.vise.xsnow.event.Subscribe;
import com.vise.xsnow.http.ViseHttp;
import com.vise.xsnow.http.callback.ACallback;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class MainActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 3;
    private static final int ENABLE_BLE_CODE = 4;
    private static final int SKIP_BYTE = 4;//需要跳过的字节数字
    private static final byte CYCLE_TIME = 0x0f;//循环的次数
    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;
    public static final String TAG = "MainActivity";
    @BindView(R.id.tv_message)
    TextView tvMessage;
    @BindView(R.id.btn_connect)
    Button connectDevice;
    @BindView(R.id.btn_disconnect)
    Button disconnectDevice;
    @BindView(R.id.btn_sync_time)
    Button syncTimeDevice;
    @BindView(R.id.btn_read_time)
    Button readTimeDevice;
    @BindView(R.id.btn_sync_data)
    Button syncAllData;
    @BindView(R.id.btn_open_time)
    Button openTime;
    @BindView(R.id.btn_off_time)
    Button offTime;
    @BindView(R.id.btn_skip_next)
    Button skipNext;
    @BindView(R.id.btn_read_battery)
    Button readBattery;
    @BindView(R.id.btn_firmware_update)
    Button btnFirmwareUpdate;
    private BluetoothLeDevice bluetoothLeDevice;
    private ArrayList<String> totalSleepData = new ArrayList<>();
    private int sendReadDataItem;
    private Runnable writeFileRun = new Runnable() {

        @Override
        public void run() {
            //写入数据到sd卡中
            FileUtil.writeFileToSDCard(totalSleepData, FileUtil.DOWNLOAD_FOLDER, FileUtil.DOWNLOAD_FILE_NAME, true, false);
            //写入数据完成清空所有的临时数据
            totalSleepData.clear();
            String zipPath = Environment.getExternalStorageDirectory()
                    + File.separator + FileUtil.DOWNLOAD_FOLDER + File.separator + FileUtil.DOWNLOAD_ZIP;
            File file = new File(zipPath);
            postFile(file);
//            ViseHttp.getOkHttpClient().newCall()
        }
    };
    private BandLoader bandLoader;

    private void postFile(File file) {
        MultipartBody.Builder requestBoby = new MultipartBody.Builder().setType(MultipartBody.FORM);
        if (file != null) {
            RequestBody body = RequestBody.create(MediaType.parse("application/x-zip-compressed"), file);
            String fileName = file.getName();
            requestBoby.addFormDataPart("file", fileName, body);
        }
        requestBoby.addFormDataPart("userid", "909ea610531f4ac88c07161369367513");
        requestBoby.addFormDataPart("ver", "1");
//        Request request = new Request.Builder().url(Constant.BASE_URL+"pillow/uploadPillowData").post(requestBoby.build()).tag("zhidao").build();
//        ViseHttp.getOkHttpClient().newCall(request).enqueue(new Callback() {
//            @Override
//            public void onFailure(Call call, IOException e) {
//                ViseLog.i("上传出错 error = "+ e.getMessage());
//                Toast.makeText(MainActivity.this,"上传出错",Toast.LENGTH_SHORT).show();
//                call.cancel();
//            }
//
//            @Override
//            public void onResponse(Call call, Response response) throws IOException {
//                ViseLog.i("上传成功");
//            }
//        });
        ViseHttp.POST("pillow/uploadPillowData")
                .setRequestBody(requestBoby.build())
                .tag("zhidao")
                .request(new ACallback<BaseRequest>() {

                    @Override
                    public void onSuccess(BaseRequest data) {
                        ViseLog.i("请求成功 data = " + data.toString());
                    }

                    @Override
                    public void onFail(int errCode, String errMsg) {
                        ViseLog.i("请求失败 errorCode = " + errCode + "errorMsg = " + errMsg);
                    }
                });
    }

    private Handler myHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        myHandler = new Handler();
        bandLoader = new BandLoader();
        ViseLog.getLogConfig().configAllowLog(true);//配置日志信息
        ViseLog.plant(new LogcatTree());//添加logcat打印信息
        BusManager.getBus().register(this);

        initViewAndEvent();
        checkBluetoothPermission();
        Log.i(TAG, "onCreate: 栈 id = "+getTaskId());
        ViseLog.i("栈 id = " + getTaskId());

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "取消扫描", Toast.LENGTH_LONG).show();
            } else {
                String address = result.getContents();
                refreshLogTextView("正在连接中稍等...\r\n");
                String realAddress = reversalMacAddress(address);
                BluetoothDeviceManager.getInstance().connectByMac(realAddress);
            }
        }
        if (requestCode == RESULT_CANCELED) {
            finish();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE) {
            syncDeviceAllData();
        }
    }

    @Override
    protected void onDestroy() {
        ViseLog.i("销毁界面");
        //移除所有的日志树
//        ViseLog.uprootAll();
        Log.i(TAG, "销毁界面 ");
        ViseHttp.cancelTag("zhidao");
        if (bluetoothLeDevice != null) {
            BluetoothDeviceManager.getInstance().setmDeviceReconnected(bluetoothLeDevice, false);
        }
//        退出APP的时候需要清除所有的资源并且断开所有的连接
        ViseBle.getInstance().clear();
        BusManager.getBus().unregister(this);
        super.onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
//        super.onConfigurationChanged(newConfig);
    }

    @OnClick({R.id.btn_connect, R.id.btn_disconnect, R.id.btn_sync_time, R.id.btn_read_time, R.id.btn_sync_data,
            R.id.btn_open_time, R.id.btn_off_time, R.id.btn_skip_next, R.id.btn_read_battery,R.id.btn_firmware_update})
    public void allClickEvent(View view) {
        switch (view.getId()) {
            case R.id.btn_connect:
//                Intent intent3 = new Intent(this,OftenActivity.class);
//                intent3.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                startActivity(intent3);
//                showUncertainDialog();
                IntentIntegrator integrator = new IntentIntegrator(this);
                integrator.setOrientationLocked(false);
                integrator.initiateScan();
                break;
            case R.id.btn_disconnect:
                ToastUtil.show(this,"断开连接");
                ToastUtil.showAtCenter(this,"断开连接");
//                ToastUtil.makeText(this,"断开连接",Toast.LENGTH_SHORT);
//                if (bluetoothLeDevice != null) {
//                    BluetoothDeviceManager.getInstance().disconnect(bluetoothLeDevice);
//                }
                break;
            case R.id.btn_read_battery:
                //读取电量
                if (bluetoothLeDevice != null) {
//                    BluetoothDeviceManager.getInstance().bindChannel(bluetoothLeDevice,PropertyType.PROPERTY_READ,
//                            UUID.fromString(GlaUtils.PILLOW_SERV_UUID_BATT),UUID.fromString(GlaUtils.PILLOW_CHAR_UUID_BLVL),
//                            null);
//                    BluetoothDeviceManager.getInstance().read(bluetoothLeDevice);
                    byte[] deleteUserData = new byte[]{0x44};
                    BluetoothDeviceManager.getInstance().bindChannel(bluetoothLeDevice, PropertyType.PROPERTY_WRITE,
                            UUID.fromString(GlaUtils.BAND_PEGASI_SERVICE_UUID),
                            UUID.fromString(GlaUtils.BAND_PEGASI_LOG_DATA_CHARACTERISTIC_UUID),
                            null);
                    BluetoothDeviceManager.getInstance().write(bluetoothLeDevice, deleteUserData);
                }
                break;
            case R.id.btn_sync_time:
                //写数据设备
                if (bluetoothLeDevice != null) {
                    long currentTimeMillis = System.currentTimeMillis() / 1000;
                    byte[] data = new byte[]{(byte) (currentTimeMillis & 0xff),
                            (byte) ((currentTimeMillis >> 8) & 0xff),
                            (byte) ((currentTimeMillis >> 16) & 0xff),
                            (byte) ((currentTimeMillis >> 24) & 0xff),
                            (byte) 0, (byte) 0, (byte) 0, (byte) 0};
                    String hexStr = HexUtil.encodeHexStr(data);
                    Log.d("mainActivity", "当前时间的十六进制 = " + hexStr);
                    refreshLogTextView("写入的时间戳 time = " + currentTimeMillis + "\r\n");
                    BluetoothDeviceManager.getInstance().bindChannel(bluetoothLeDevice, PropertyType.PROPERTY_WRITE,
                            UUID.fromString(GlaUtils.BAND_PEGASI_SERVICE_UUID), UUID.fromString(GlaUtils.BAND_PEGASI_SYNC_TIME_CHARACTERISTIC_UUID),
                            null);
                    BluetoothDeviceManager.getInstance().write(bluetoothLeDevice, data);
                }
                break;
            case R.id.btn_read_time:
                //读取时间
                if (bluetoothLeDevice != null) {
                    BluetoothDeviceManager.getInstance().bindChannel(bluetoothLeDevice, PropertyType.PROPERTY_READ,
                            UUID.fromString(GlaUtils.BAND_PEGASI_SERVICE_UUID), UUID.fromString(GlaUtils.BAND_PEGASI_SYNC_TIME_CHARACTERISTIC_UUID),
                            null);
                    BluetoothDeviceManager.getInstance().read(bluetoothLeDevice);
                }
                break;
            case R.id.btn_sync_data:
                if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    //请求权限
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
                    //判断是否需要 向用户解释，为什么要申请该权限
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.READ_CONTACTS)) {
                        Toast.makeText(this, "shouldShowRequestPermissionRationale", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    //同步所有数据
                    syncDeviceAllData();
                }
                break;
            case R.id.btn_open_time:

                Intent intent2 = new Intent(this, OftenActivity.class);
                startActivity(intent2);

                break;
            case R.id.btn_off_time:
                if (bluetoothLeDevice != null) {
                    //关闭时时模式
                    byte[] offLive = new byte[]{0x4c, 0x44};
                    BluetoothDeviceManager.getInstance().bindChannel(bluetoothLeDevice, PropertyType.PROPERTY_WRITE,
                            UUID.fromString(GlaUtils.BAND_PEGASI_SERVICE_UUID),
                            UUID.fromString(GlaUtils.BAND_PEGASI_LIVE_MODE_CHARACTERISTIC_UUID),
                            null);
                    BluetoothDeviceManager.getInstance().write(bluetoothLeDevice, offLive);
                }
                break;
            case R.id.btn_skip_next:
                Intent intent = new Intent(this, EegActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                break;
            case R.id.btn_firmware_update:
                bandLoader.getVersion("PGY8S01","1.0.0","1.0.0")
                        .subscribeOn(Schedulers.io())
                        .unsubscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<RequestBody>() {
                            @Override
                            public void accept(RequestBody requestBody) throws Exception {
                                ViseLog.i("请求得到的响应体 = "+requestBody.toString());
                            }
                        });

//                ViseHttp.DOWNLOAD("sleep_sensor/latest.txt?model=PGY8S01&hw=1.0.0&fw=1.0.0")
//                        .setDirName("fire")
//                        .setFileName("temp.txt")
//                        .request(new ACallback<DownProgress>() {
//                            @Override
//                            public void onSuccess(DownProgress data) {
//                                if (data.isDownComplete()) {
//                                    String filePath = FileUtil.getDiskCachePath(MainActivity.this) + File.separator
//                                            + "fire" + File.separator + "temp.txt";
//                                    if (FileUtil.isFileExists(filePath)) {
//                                        //读取文件的内容
//                                        String string = FileIOUtils.readFile2String(filePath);
//                                        ViseLog.i("读到文件的内容 = "+string);
//                                    }
//                                }
//                                ViseLog.i("下载的data = "+data.toString());
//                            }
//
//                            @Override
//                            public void onFail(int errCode, String errMsg) {
//                                ViseLog.i("下载出错 code = "+errCode+" message = "+errMsg);
//                            }
//                        });
                break;
            default:
                break;
        }
    }

    private void showUncertainDialog() {
        UncertainDialog uncertainDialog = UncertainDialog.getInstance();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(uncertainDialog,"dialog");
        transaction.commitAllowingStateLoss();
    }

    private void syncDeviceAllData() {
        initDataWithSync();
        if (bluetoothLeDevice != null) {
            //首先需要初始化
            BluetoothDeviceManager.getInstance().initEnableChannel(bluetoothLeDevice);

            BluetoothDeviceManager.getInstance().bindChannel(bluetoothLeDevice, PropertyType.PROPERTY_NOTIFY,
                    UUID.fromString(GlaUtils.BAND_PEGASI_SERVICE_UUID), UUID.fromString(GlaUtils.BAND_PEGASI_LOG_DATA_CHARACTERISTIC_UUID),
                    null);
            BluetoothDeviceManager.getInstance().registerNotify(bluetoothLeDevice, false);
        }
    }

    private void initDataWithSync() {
        //初始化文件
        String pathString = Environment.getExternalStorageDirectory()
                + File.separator + FileUtil.DOWNLOAD_FOLDER + File.separator + FileUtil.DOWNLOAD_FILE_NAME;
        String zipPath = Environment.getExternalStorageDirectory()
                + File.separator + FileUtil.DOWNLOAD_FOLDER + File.separator + FileUtil.DOWNLOAD_ZIP;
        FileUtil.writeFileEmpty(pathString);
        FileUtil.deleteFile(new File(zipPath));
        //初始化数据
        if (totalSleepData != null) {
            totalSleepData.clear();
        }
        numberPager = 0;
        sendReadDataItem = 0;
    }

    @Subscribe
    public void showScanConnectDevice(ConnectEvent event) {
        if (event != null) {
            if (event.isSuccess()) {
                //显示成功
                if (event.getDeviceMirror() != null) {
                    ViseLog.d("mainActivity 连接成功了");
                    bluetoothLeDevice = event.getDeviceMirror().getBluetoothLeDevice();
                    refreshLogTextView("连接成功了\r\n");
                    //设置重连接
                    if (bluetoothLeDevice != null) {
                        //保存蓝牙对象
                        byte[] bluetoothByte = ParcelableUtil.marshall(bluetoothLeDevice);
                        ViseLog.d("连接后得到的蓝牙对象 = " + bluetoothLeDevice.toString());
                        SPUtils.getInstance(Constant.SHARED_PREFERENCE_NAME).put(Constant.BLUETOOTH_DEVICE_KEY, Base64.encodeToString(bluetoothByte, 0));

                        BluetoothDeviceManager.getInstance().setmDeviceReconnected(bluetoothLeDevice, true);
                    }
                }
            } else {
                //默认是失败的
                if (event.isDisconnected()) {
                    //主动断开连接
                    refreshLogTextView("设备断开连接,可能超时或者远离了\r\n");
                } else {
                    //不是主动断开连接的,都是被动执行的
                    refreshLogTextView("连接时错误\r\n");
                }
            }
        }
    }

    @Subscribe
    public void bleCallBackEvent(CallbackDataEvent event) {
        if (event != null) {
            if (event.isSuccess()) {
                //成功收到数据回调
                BluetoothGattChannel bluetoothGattChannel = event.getBluetoothGattChannel();
                if (bluetoothGattChannel != null) {
                    //写入时间成功
                    if (bluetoothGattChannel.getPropertyType() == PropertyType.PROPERTY_WRITE &&
                            bluetoothGattChannel.getCharacteristicUUID().
                                    compareTo(UUID.fromString(GlaUtils.BAND_PEGASI_SYNC_TIME_CHARACTERISTIC_UUID)) == 0) {
                        refreshLogTextView("写入时间成功了\r\n");
                    }
                    //接收到时间返回
                    if (bluetoothGattChannel.getPropertyType() == PropertyType.PROPERTY_READ &&
                            bluetoothGattChannel.getCharacteristicUUID().
                                    compareTo(UUID.fromString(GlaUtils.BAND_PEGASI_SYNC_TIME_CHARACTERISTIC_UUID)) == 0) {
                        long time = HexUtil.byteToLong(event.getData(), 0, 8, false);
                        refreshLogTextView("接收到写入的时间值 time = " + time + "\r\n");
                    }
                    //读取电量成功
                    if (bluetoothGattChannel.getPropertyType() == PropertyType.PROPERTY_READ &&
                            bluetoothGattChannel.getCharacteristicUUID().
                                    compareTo(UUID.fromString(GlaUtils.PILLOW_CHAR_UUID_BLVL)) == 0) {
                        byte[] data = event.getData();
                        int battery = data[0] & 0xff;
                        refreshLogTextView("接收到设备当前的电量 battery = " + battery + "\r\n");
                    }
                    //使能log特征成功
                    if (bluetoothGattChannel.getPropertyType() == PropertyType.PROPERTY_NOTIFY &&
                            bluetoothGattChannel.getCharacteristicUUID().
                                    compareTo(UUID.fromString(GlaUtils.BAND_PEGASI_LOG_DATA_CHARACTERISTIC_UUID)) == 0) {
                        if (Arrays.equals(event.getData(), BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)) {
                            refreshLogTextView("使能log特征成功了\r\n");
                            byte[] queryLatestTime = new byte[]{0x51};
                            if (bluetoothLeDevice != null) {
                                BluetoothDeviceManager.getInstance().bindChannel(bluetoothLeDevice, PropertyType.PROPERTY_WRITE,
                                        UUID.fromString(GlaUtils.BAND_PEGASI_SERVICE_UUID),
                                        UUID.fromString(GlaUtils.BAND_PEGASI_LOG_DATA_CHARACTERISTIC_UUID),
                                        null);
                                BluetoothDeviceManager.getInstance().write(bluetoothLeDevice, queryLatestTime);
                            }
                        }
                    }
                    //使能时时模式特征成功
                    /*
                    if (bluetoothGattChannel.getPropertyType() == PropertyType.PROPERTY_NOTIFY &&
                            bluetoothGattChannel.getCharacteristicUUID().
                                    compareTo(UUID.fromString(GlaUtils.BAND_PEGASI_LIVE_MODE_CHARACTERISTIC_UUID)) == 0){
                        if (Arrays.equals(event.getData(), BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)) {
                            refreshLogTextView("使能时时模式成功了,发送时时命令\r\n");
                            //发送时时的命令
                            byte[] liveMode = new byte[]{0x4c,0x45};
                            if (bluetoothLeDevice != null) {
                                BluetoothDeviceManager.getInstance().bindChannel(bluetoothLeDevice,PropertyType.PROPERTY_WRITE,
                                        UUID.fromString(GlaUtils.BAND_PEGASI_SERVICE_UUID),
                                        UUID.fromString(GlaUtils.BAND_PEGASI_LIVE_MODE_CHARACTERISTIC_UUID),
                                        null);
                                BluetoothDeviceManager.getInstance().write(bluetoothLeDevice,liveMode);
                            }
                        }
                    }
                    */
                    /*
                    //发送关闭时时指令成功了，需要去掉使能
                    if (bluetoothGattChannel.getPropertyType() == PropertyType.PROPERTY_WRITE &&
                            bluetoothGattChannel.getCharacteristicUUID().
                                    compareTo(UUID.fromString(GlaUtils.BAND_PEGASI_LIVE_MODE_CHARACTERISTIC_UUID)) == 0){
                        byte[] data = event.getData();
                        //表示是关闭时时的指令
                        if (data[1] == 0x44){
                            //首先需要初始化
                            BluetoothDeviceManager.getInstance().initEnableChannel(bluetoothLeDevice);
                            //取消使能
                            BluetoothDeviceManager.getInstance().bindChannel(bluetoothLeDevice,PropertyType.PROPERTY_NOTIFY,
                                    UUID.fromString(GlaUtils.BAND_PEGASI_SERVICE_UUID),
                                    UUID.fromString(GlaUtils.BAND_PEGASI_LIVE_MODE_CHARACTERISTIC_UUID),
                                    null);
                            BluetoothDeviceManager.getInstance().unregisterNotify(bluetoothLeDevice,false);
                        }
                    }
                    */
                }
            } else {
                //接收数据出现错误
                refreshLogTextView("接收数据出错了\r\n");
            }
        }
    }

    byte[] latestWriteTime;//低位在前
    int numberPager;

    //notify回来的数据在这里接收
    @Subscribe
    public void bleNotifyCallBackEvent(NotifyDataEvent event) {
        if (event != null) {
            if (event.getBluetoothGattChannel() != null) {
                byte[] data = event.getData();
                if (event.getBluetoothGattChannel().getCharacteristicUUID()
                        .compareTo(UUID.fromString(GlaUtils.BAND_PEGASI_LOG_DATA_CHARACTERISTIC_UUID)) == 0) {
                    //得到读取最后写入的时间戳
                    if (data[0] == 0x51) {
                        if (data[1] == 0x01) {
                            latestWriteTime = HexUtil.subBytes(data, 2, 4);
//                            long startTime = System.currentTimeMillis() / 1000 - 2 * 86400;
                            long startTime = 0;
                            byte[] beginTime = LongToBytes(startTime);
                            //发送读取页数
                            byte[] readPageCommand = new byte[]{0x52, latestWriteTime[0], latestWriteTime[1], latestWriteTime[2],
                                    latestWriteTime[3], 0, 0, 0, 0,
                                    beginTime[0], beginTime[1], beginTime[2], beginTime[3],
                                    beginTime[4], beginTime[5], beginTime[6], beginTime[7]};
                            if (bluetoothLeDevice != null) {
                                BluetoothDeviceManager.getInstance().bindChannel(bluetoothLeDevice, PropertyType.PROPERTY_WRITE,
                                        UUID.fromString(GlaUtils.BAND_PEGASI_SERVICE_UUID),
                                        UUID.fromString(GlaUtils.BAND_PEGASI_LOG_DATA_CHARACTERISTIC_UUID),
                                        null);
                                BluetoothDeviceManager.getInstance().write(bluetoothLeDevice, readPageCommand);
                            }
                        } else {
                            refreshLogTextView("发送读取写入的最后时间戳出错\r\n");
                        }
                    }
                    //得到读取页数的返回
                    if (data[0] == 0x52) {
                        if (data[1] == 0x01) {
                            byte[] number = HexUtil.subBytes(data, 2, 2);
                            numberPager = Utils.getInstance().byteToInt(number);
                            ViseLog.i("得到设备上数据的总页数 pager = " + numberPager);
                            //发送第一条数据
                            if (numberPager > 3 && bluetoothLeDevice != null) {
                                byte[] readFirstData = new byte[]{0x53, 0x41, 0, 0};
                                BluetoothDeviceManager.getInstance().bindChannel(bluetoothLeDevice, PropertyType.PROPERTY_WRITE,
                                        UUID.fromString(GlaUtils.BAND_PEGASI_SERVICE_UUID),
                                        UUID.fromString(GlaUtils.BAND_PEGASI_LOG_DATA_CHARACTERISTIC_UUID),
                                        null);
                                BluetoothDeviceManager.getInstance().write(bluetoothLeDevice, readFirstData);
                            } else {
                                //取消使能
                                BluetoothDeviceManager.getInstance().bindChannel(bluetoothLeDevice, PropertyType.PROPERTY_NOTIFY,
                                        UUID.fromString(GlaUtils.BAND_PEGASI_SERVICE_UUID),
                                        UUID.fromString(GlaUtils.BAND_PEGASI_LOG_DATA_CHARACTERISTIC_UUID),
                                        null);
                                BluetoothDeviceManager.getInstance().unregisterNotify(bluetoothLeDevice, false);
                            }
                        } else {
                            refreshLogTextView("发送读取设备页数出错\r\n");
                        }
                    }
                    //得到睡眠数据
                    if (data[0] <= CYCLE_TIME) {
                        byte location = data[0];

                        //拷贝睡眠内容,并把内容先填充在一个list中，后面一次性写入文件
                        byte[] sleepData = new byte[data.length - SKIP_BYTE];
                        System.arraycopy(data, SKIP_BYTE, sleepData, 0, data.length - SKIP_BYTE);
                        String hexStr = HexUtil.encodeHexStr(sleepData);
                        ViseLog.i("整晚的睡眠数据:" + hexStr);
                        totalSleepData.add(hexStr);

                        if (location == CYCLE_TIME) {
                            myHandler.removeCallbacksAndMessages(null);
                            //发送下一条指令
                            sendReadDataItem = sendReadDataItem + 1;
                            if (sendReadDataItem <= numberPager) {
                                byte[] leaveData = new byte[]{(byte) 0x53, 0x41, (byte) ((sendReadDataItem) & 0xff), (byte) ((sendReadDataItem >> 8) & 0xff)};
                                ViseLog.d("写入的页数:" + sendReadDataItem);
                                if (bluetoothLeDevice != null) {
                                    BluetoothDeviceManager.getInstance().bindChannel(bluetoothLeDevice, PropertyType.PROPERTY_WRITE,
                                            UUID.fromString(GlaUtils.BAND_PEGASI_SERVICE_UUID),
                                            UUID.fromString(GlaUtils.BAND_PEGASI_LOG_DATA_CHARACTERISTIC_UUID),
                                            null);
                                    BluetoothDeviceManager.getInstance().write(bluetoothLeDevice, leaveData);
                                }
//                                myHandler.postDelayed(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        byte[] leaveData = new byte[]{(byte) 0x53, (byte) ((sendReadDataItem >> 8) & 0xff), (byte) ((sendReadDataItem) & 0xff)};
//                                        ViseLog.e("超时重新发送没有回调错误的页数");
//                                        ViseLog.e("写入的页数:" + sendReadDataItem);
//                                        if (bluetoothLeDevice != null) {
//                                            BluetoothDeviceManager.getInstance().bindChannel(bluetoothLeDevice, PropertyType.PROPERTY_WRITE,
//                                                    UUID.fromString(GlaUtils.BAND_PEGASI_SERVICE_UUID),
//                                                    UUID.fromString(GlaUtils.BAND_PEGASI_LOG_DATA_CHARACTERISTIC_UUID),
//                                                    null);
//                                            BluetoothDeviceManager.getInstance().write(bluetoothLeDevice, leaveData);
//                                        }
//                                    }
//                                }, 600);
                            } else {
                                //所有都读取完成了,把数据存入sd卡中的一个临时文件中
                                if (totalSleepData != null && totalSleepData.size() > 0) {
                                    Thread mThread = new Thread(writeFileRun);
                                    mThread.start();
                                }
                                ViseLog.i("写入数据完成,禁止notify以及把数据写入文件");
                                //首先需要初始化
                                BluetoothDeviceManager.getInstance().initEnableChannel(bluetoothLeDevice);
                                //disenable之前的notify
                                if (bluetoothLeDevice != null) {
                                    BluetoothDeviceManager.getInstance().bindChannel(bluetoothLeDevice, PropertyType.PROPERTY_NOTIFY,
                                            UUID.fromString(GlaUtils.BAND_PEGASI_SERVICE_UUID),
                                            UUID.fromString(GlaUtils.BAND_PEGASI_LOG_DATA_CHARACTERISTIC_UUID),
                                            null);
                                    BluetoothDeviceManager.getInstance().unregisterNotify(bluetoothLeDevice, false);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * @param values 需要转化的long值
     * @return 返回的byte是低位在前高位在后的
     */
    private byte[] LongToBytes(long values) {

        byte[] buffer = new byte[8];
        for (int i = 0; i < 8; i++) {
            int offset = i * 8;
            buffer[i] = (byte) ((values >> offset) & 0xff);
        }
        return buffer;
    }

    private void initViewAndEvent() {
        tvMessage.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    view.getParent().requestDisallowInterceptTouchEvent(true);
                }
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    view.getParent().requestDisallowInterceptTouchEvent(true);
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    view.getParent().requestDisallowInterceptTouchEvent(false);
                }
                return false;
            }
        });
        tvMessage.setMovementMethod(ScrollingMovementMethod.getInstance());
    }

    private void checkBluetoothPermission() {
        //判断是不是6.0以上的设备
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //判断是否有权限
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                //请求权限
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
                //判断是否需要 向用户解释，为什么要申请该权限
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.READ_CONTACTS)) {
                    Toast.makeText(this, "shouldShowRequestPermissionRationale", Toast.LENGTH_SHORT).show();
                }
            } else {
                enableBluetooth();
            }
        } else {
            //判断手机是否打开蓝牙
            enableBluetooth();
        }
    }

    private void enableBluetooth() {
        if (!BleUtil.isBleEnable(this)) {
            BleUtil.enableBluetooth(this, ENABLE_BLE_CODE);
        }
    }

    private void refreshLogTextView(String message) {
        tvMessage.append(message);
        int offset = tvMessage.getLineCount() * tvMessage.getLineHeight();
        if (offset > tvMessage.getHeight()) {
            tvMessage.scrollTo(0, offset - tvMessage.getHeight());
        }
    }

    private String reversalMacAddress(String macAddress) {
        String reversalMacAddress = macAddress;
        if (reversalMacAddress.contains(":")) {
            String[] split = reversalMacAddress.split(":");
            String[] tempMacAddress = new String[split.length];
            for (int i = 0; i < split.length; i++) {
                //完成倒叙的过程
                tempMacAddress[split.length - 1 - i] = split[i];
            }
            StringBuilder lastMacAdress = new StringBuilder();
            for (String address : tempMacAddress) {
                lastMacAdress.append(address).append(":");
            }
            reversalMacAddress = lastMacAdress.substring(0, lastMacAdress.length() - 1);
        }
        return reversalMacAddress;
    }
}
