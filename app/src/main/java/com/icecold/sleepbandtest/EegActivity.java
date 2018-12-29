package com.icecold.sleepbandtest;

import android.Manifest;
import android.bluetooth.BluetoothGattDescriptor;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Base64;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.icecold.sleepbandtest.common.BluetoothDeviceManager;
import com.icecold.sleepbandtest.common.formatter.SleepTimeAxisValueFormatter;
import com.icecold.sleepbandtest.entity.EegInformation;
import com.icecold.sleepbandtest.event.CallbackDataEvent;
import com.icecold.sleepbandtest.event.ConnectEvent;
import com.icecold.sleepbandtest.event.NotifyDataEvent;
import com.icecold.sleepbandtest.utils.Constant;
import com.icecold.sleepbandtest.utils.FileUtil;
import com.icecold.sleepbandtest.utils.GlaUtils;
import com.icecold.sleepbandtest.utils.ParcelableUtil;
import com.icecold.sleepbandtest.utils.SPUtils;
import com.vise.baseble.ViseBle;
import com.vise.baseble.common.PropertyType;
import com.vise.baseble.core.BluetoothGattChannel;
import com.vise.baseble.model.BluetoothLeDevice;
import com.vise.baseble.utils.BleUtil;
import com.vise.baseble.utils.HexUtil;
import com.vise.log.ViseLog;
import com.vise.xsnow.event.BusManager;
import com.vise.xsnow.event.Subscribe;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class EegActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 5;
    private static final int ENABLE_BLE_CODE = 6;
    private static final byte CYCLE_TIME = 0x0f;
    private static final int ONE_SKIP_BYTE = 8;
    private static final int SKIP_BYTE = 4;
    public static final String TAG = "EegActivity";
    private static final int PACKET_LENGTH = 72;
    @BindView(R.id.eeg_tv_message)
    TextView eegTvMessage;
    @BindView(R.id.eeg_btn_connect)
    Button eegBtnConnect;
    @BindView(R.id.eeg_btn_disconnect)
    Button eegBtnDisconnect;
    @BindView(R.id.eeg_btn_sync_data)
    Button eegBtnSyncData;
    @BindView(R.id.eeg_btn_read_time)
    Button eegBtnReadTime;
    @BindView(R.id.eeg_btn_storage)
    Button eegBtnStorage;
    @BindView(R.id.eeg_btn_erasure)
    Button eegBtnErasure;
    @BindView(R.id.eeg_line)
    LineChart eegLine;
    private BluetoothLeDevice bluetoothLeDevice;
    private int currentWriteTime;
    private int lastStorageTime;
    private int sendNumberPage;
    private ArrayList<String> totalData = new ArrayList<>();
    private Runnable writeFileRun = new Runnable() {

        @Override
        public void run() {
            //写入数据到sd卡中
            FileUtil.writeFileToSDCard(totalData, FileUtil.DOWNLOAD_FOLDER, FileUtil.DOWNLOAD_FILE_NAME, true, false);
            //写入数据完成清空所有的临时数据
            totalData.clear();
//            String zipPath = Environment.getExternalStorageDirectory()
//                    + File.separator + FileUtil.DOWNLOAD_FOLDER + File.separator + FileUtil.DOWNLOAD_ZIP;
//            File file = new File(zipPath);
        }
    };
    private int devicePageStartTime;
    private boolean enterOnce = false;//只进入一次记录最开始的时间值
    private ArrayList<Entry> meditationListData = new ArrayList<>();
    private ArrayList<Entry> attentionListData = new ArrayList<>();
    private ArrayList<Entry> dividedListData = new ArrayList<>();
    private ArrayList<EegInformation> eegInformations = new ArrayList<>();
    private int startTimeToDevice;//对于设备记录的开始时间
    private int xAxisValue;//记录开始记录的最初时间
    private int checkValue;
    private final Object lock = new Object();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eeg);
        ButterKnife.bind(this);

        initViewAndEvent();
        checkBluetoothPermission();
        initLineChart(eegLine);
        EventBus.getDefault().register(this);
        BusManager.getBus().register(this);
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
                BluetoothDeviceManager.getInstance().connectByMac(address);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        ViseBle.getInstance().clear();
        EventBus.getDefault().unregister(this);
        BusManager.getBus().unregister(this);
        super.onDestroy();
    }

    @OnClick({R.id.eeg_btn_connect, R.id.eeg_btn_disconnect, R.id.eeg_btn_sync_data, R.id.eeg_btn_read_time, R.id.eeg_btn_storage, R.id.eeg_btn_erasure})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.eeg_btn_connect:
                IntentIntegrator integrator = new IntentIntegrator(this);
                integrator.setOrientationLocked(false);
                integrator.initiateScan();
                break;
            case R.id.eeg_btn_disconnect:
                if (bluetoothLeDevice != null) {
                    BluetoothDeviceManager.getInstance().disconnect(bluetoothLeDevice);
                }
                break;
            case R.id.eeg_btn_sync_data:
                initDataWithSync();
                if (bluetoothLeDevice != null) {
                    BluetoothDeviceManager.getInstance().bindChannel(bluetoothLeDevice, PropertyType.PROPERTY_WRITE,
                            UUID.fromString(GlaUtils.PEGASI_BRAINWAVE_SERVICE_UUID),
                            UUID.fromString(GlaUtils.PILLOW_BRAINWAVE_CHARACTERISTIC_WRITE),
                            null);
                    BluetoothDeviceManager.getInstance().write(bluetoothLeDevice, Constant.STOP_STORAGE_DEVICE.getBytes());
                    refreshLogTextView("同步开始了\r\n");
                }
                break;
            case R.id.eeg_btn_read_time:
                refreshLogTextView("开始画图\r\n");
                //读取数据
                Observable.just(1)
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io())
                        .unsubscribeOn(Schedulers.io())
                        .map(new Function<Integer, Observable<String>>() {

                            @Override
                            public Observable<String> apply(Integer integer) throws Exception {
                                Log.d(TAG, "apply: map 所在的线程 "+Thread.currentThread().getName());
                                attentionListData.clear();
                                meditationListData.clear();
                                dividedListData.clear();
                                attentionListData.add(new Entry(0,1));
                                meditationListData.add(new Entry(0,1));
                                dividedListData.add(new Entry(0,1));
                                xAxisValue = 1;
                                String folderPath = Environment.getExternalStorageDirectory()
                                        + File.separator + FileUtil.DOWNLOAD_FOLDER + File.separator + FileUtil.DOWNLOAD_FILE_NAME;
                                String fileContent = FileUtil.readFileContent(folderPath);
                                String[] allPackData = null;
                                if (fileContent != null) {
                                    allPackData = fileContent.split("aaaa2002");
//                                    allPackData = new String[fileContent.length() / PACKET_LENGTH];
//                                    ViseLog.i("总共的数组长度 length = "+fileContent.length());
//                                    for (int i = 0; i < fileContent.length() / PACKET_LENGTH; i++) {
//                                        String onePack = fileContent.substring(PACKET_LENGTH * i, PACKET_LENGTH * (i + 1));
//                                        allPackData[i] = onePack;
//                                    }
                                }
//                                Log.i(TAG, "读取到文件的内容 fileContent = " + content + " 分割出来的长度 length = " + content.length());
                                return Observable.fromArray(allPackData);
                            }
                        })
                        .flatMap(new Function<Observable<String>, ObservableSource<String>>() {
                            @Override
                            public ObservableSource<String> apply(Observable<String> stringObservable) throws Exception {
                                Log.d(TAG, "apply: flatMap 所在的线程 = "+Thread.currentThread().getName());
                                return stringObservable;
                            }
                        })
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<String>() {
                            int time = 0;
                            int meditationTotal = 0;
                            int attentionTotal = 0;

                            @Override
                            public void accept(String content) throws Exception {
                                byte[] allByte = hexStringToByte(content);
                                if (allByte != null) {
                                    if (allByte.length > 5){
                                        if (allByte[allByte.length - 5] == 0x04 && allByte[allByte.length - 3] == 0x05){
                                            int meditation = allByte[allByte.length - 2] & 0xFF;
                                            int attention = allByte[allByte.length - 4] & 0xFF;
                                            EegInformation information = new EegInformation();
                                            information.setAttention(attention);
                                            information.setMeditation(meditation);
                                            information.setTimeStamp(startTimeToDevice + xAxisValue-1);
                                            eegInformations.add(information);
                                            meditationTotal += meditation;
                                            attentionTotal += attention;
                                            Log.d(TAG, "accept: 放松度 meditation = " + meditation + " 专注度 attention = " + attention);
                                            Log.d(TAG, "accept: 次数 time = " + time);

                                            //说明产生了一个有效的数据
                                            if (time % 2 == 1) {
                                                //y轴的数值
                                                int yAxisMeditationValue = meditationTotal / 2;
                                                int yAxisAttentionValue = attentionTotal / 2;
                                                attentionListData.add(new Entry(xAxisValue, yAxisAttentionValue));
                                                meditationListData.add(new Entry(xAxisValue, yAxisMeditationValue));
                                                if (yAxisAttentionValue != 0){
                                                    dividedListData.add(new Entry(xAxisValue, ((float) yAxisMeditationValue / yAxisAttentionValue) * 10));
                                                }
//                                              xAxisAttentionValue = xAxisMeditationValue;
                                                ViseLog.i("有效的专注度的x轴的值 = " + xAxisValue + " 有效的专注度的y轴的值 = " + yAxisAttentionValue);
                                                ViseLog.i("有效的放松度的x轴的值 = " + xAxisValue + " 有效的放松度的y轴的值 = " + yAxisMeditationValue);
//                                              ViseLog.i(" 有效的专注度的y轴的值 = "+yAxisAttentionValue);
//                                              ViseLog.i(" 有效的放松度的y轴的值 = "+yAxisMeditationValue);
                                                meditationTotal = 0;
                                                attentionTotal = 0;

                                            }
                                        }
                                    }
                                }

                                if (allByte != null) {
                                    if (allByte.length > 5){

                                        if (allByte[allByte.length - 5] == 0x04 && allByte[allByte.length - 3] == 0x05){

                                            time++;
                                        }
                                    }
                                    ViseLog.i("累加时间值 = "+ xAxisValue);
                                    xAxisValue = xAxisValue + 1;
                                }
//                                Log.d(TAG, "accept: 订阅所在的线程 = "+Thread.currentThread().getName());
                                Log.i(TAG, "读取到文件的内容 fileContent = " + content + " 分割出来的长度 length = " + content.length());
                            }
                        }, new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) throws Exception {

                            }
                        }, new Action() {
                            @Override
                            public void run() throws Exception {
                                Log.d(TAG, "run: 运行的线程 = "+Thread.currentThread().getName());
                                String folderPath = Environment.getExternalStorageDirectory()
                                        + File.separator + FileUtil.DOWNLOAD_FOLDER + File.separator + FileUtil.DOWNLOAD_CSV_FILE_NAME;
                                if (eegInformations.size() > 0){
                                    FileUtil.writeCsv(eegInformations,folderPath);
                                }
                                setSleepXYAxis(eegLine,startTimeToDevice,lastStorageTime);
                                //完成所有数据的循环
                                generateMultipleLineDataSet(eegLine,attentionListData,meditationListData,dividedListData);

                            }
                        });
                break;
            case R.id.eeg_btn_storage:
                if (bluetoothLeDevice != null) {
                    BluetoothDeviceManager.getInstance().bindChannel(bluetoothLeDevice, PropertyType.PROPERTY_WRITE,
                            UUID.fromString(GlaUtils.PEGASI_BRAINWAVE_SERVICE_UUID),
                            UUID.fromString(GlaUtils.PILLOW_BRAINWAVE_CHARACTERISTIC_WRITE),
                            null);
                    BluetoothDeviceManager.getInstance().write(bluetoothLeDevice, Constant.START_STORAGE_DEVICE.getBytes());
                    refreshLogTextView("开始存储\r\n");
                }
                break;
            case R.id.eeg_btn_erasure:
                if (bluetoothLeDevice != null) {
                    BluetoothDeviceManager.getInstance().bindChannel(bluetoothLeDevice, PropertyType.PROPERTY_WRITE,
                            UUID.fromString(GlaUtils.PEGASI_BRAINWAVE_SERVICE_UUID),
                            UUID.fromString(GlaUtils.PILLOW_BRAINWAVE_CHARACTERISTIC_WRITE),
                            null);
                    BluetoothDeviceManager.getInstance().write(bluetoothLeDevice, Constant.ERASURE_DEVICE.getBytes());
                }
                break;
            default:
                break;
        }
    }

    @Subscribe
    public void showConnectDevice(ConnectEvent event) {
        if (event != null) {
            if (event.isSuccess()) {
                //显示成功
                if (event.getDeviceMirror() != null) {
                    bluetoothLeDevice = event.getDeviceMirror().getBluetoothLeDevice();
                    ViseLog.d("eegActivity 连接成功了");
                    refreshLogTextView("连接成功了\r\n");
                    //设置重连接
                    if (bluetoothLeDevice != null) {
                        //保存蓝牙对象
                        byte[] bluetoothByte = ParcelableUtil.marshall(bluetoothLeDevice);
                        ViseLog.d("连接后得到的蓝牙对象 = " + bluetoothLeDevice.toString());
                        SPUtils.getInstance(Constant.SHARED_PREFERENCE_NAME).put(Constant.BLUETOOTH_DEVICE_KEY, Base64.encodeToString(bluetoothByte, 0));

                        //发起同步时间,先使能特征
                        BluetoothDeviceManager.getInstance().bindChannel(bluetoothLeDevice, PropertyType.PROPERTY_NOTIFY,
                                UUID.fromString(GlaUtils.PEGASI_BRAINWAVE_SERVICE_UUID),
                                UUID.fromString(GlaUtils.PEGASI_BRAINWAVE_TX_SERVICE_CHARACTERISTIC_UUID),
                                null);
                        BluetoothDeviceManager.getInstance().registerNotify(bluetoothLeDevice, false);
//                        BluetoothDeviceManager.getInstance().setmDeviceReconnected(bluetoothLeDevice, true);
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
    public void bleEegCallBackEvent(CallbackDataEvent event) {
        if (event != null) {
            if (event.isSuccess()) {
                //成功收到数据回调
                BluetoothGattChannel bluetoothGattChannel = event.getBluetoothGattChannel();
                if (bluetoothGattChannel != null) {
                    //使能log特征成功
                    if (bluetoothGattChannel.getPropertyType() == PropertyType.PROPERTY_NOTIFY &&
                            bluetoothGattChannel.getCharacteristicUUID().
                                    compareTo(UUID.fromString(GlaUtils.PEGASI_BRAINWAVE_TX_SERVICE_CHARACTERISTIC_UUID)) == 0) {
                        if (Arrays.equals(event.getData(), BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)) {
                            refreshLogTextView("使能特征成功了\r\n");
                            currentWriteTime = (int) (System.currentTimeMillis() / 1000);
                            ViseLog.i("获取当前需要写入的时间 time = " + currentWriteTime);
                            byte[] writeDeviceTime = Constant.SYNC_DEVICE_TIME.getBytes();
                            //同步时间低位在前
                            byte[] data = new byte[]{writeDeviceTime[0], (byte) (currentWriteTime & 0xff),
                                    (byte) ((currentWriteTime >> 8) & 0xff),
                                    (byte) ((currentWriteTime >> 16) & 0xff),
                                    (byte) ((currentWriteTime >> 24) & 0xff)};
                            if (bluetoothLeDevice != null) {
                                BluetoothDeviceManager.getInstance().bindChannel(bluetoothLeDevice, PropertyType.PROPERTY_WRITE,
                                        UUID.fromString(GlaUtils.PEGASI_BRAINWAVE_SERVICE_UUID),
                                        UUID.fromString(GlaUtils.PILLOW_BRAINWAVE_CHARACTERISTIC_WRITE),
                                        null);
                                BluetoothDeviceManager.getInstance().write(bluetoothLeDevice, data);
                            }
                        }
                    }

                } else {
                    //接收数据出现错误
                    refreshLogTextView("接收数据出错了\r\n");
                }
            }
        }
    }

    @org.greenrobot.eventbus.Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(NotifyDataEvent event) {
        if (event != null) {
            if (event.getBluetoothGattChannel() != null) {
                byte[] data = event.getData();
                if (event.getBluetoothGattChannel().getCharacteristicUUID()
                        .compareTo(UUID.fromString(GlaUtils.PEGASI_BRAINWAVE_TX_SERVICE_CHARACTERISTIC_UUID)) == 0) {
                    if (data.length == 4) {
                        int obtainTime = HexUtil.byteToInt(data, 0, true);
                        if (obtainTime == currentWriteTime) {
                            refreshLogTextView("写入时间成功\r\n");
                        }
                        //说明读取的是最新的时间戳
//                        if (data[0] == 0x5b){
                        lastStorageTime = obtainTime;
                        ViseLog.i("最后存储的时间的值 = " + lastStorageTime);
                        sendNumberPage = 0;
                        byte[] lastTime = Constant.DEVICE_LAST_TIME.getBytes();
                        byte[] readDeviceData = new byte[]{lastTime[0], (byte) (sendNumberPage & 0xff), (byte) (sendNumberPage >> 8 & 0xff)};
                        if (bluetoothLeDevice != null) {
                            BluetoothDeviceManager.getInstance().bindChannel(bluetoothLeDevice, PropertyType.PROPERTY_WRITE,
                                    UUID.fromString(GlaUtils.PEGASI_BRAINWAVE_SERVICE_UUID),
                                    UUID.fromString(GlaUtils.PILLOW_BRAINWAVE_CHARACTERISTIC_WRITE),
                                    null);
                            BluetoothDeviceManager.getInstance().write(bluetoothLeDevice, readDeviceData);
                        }
//                        }
                    }
                    if (data.length == 1 && data[0] == (byte) 0xee) {
                        //设备收到停止存储了需要查询设备最后存的时间点
                        if (bluetoothLeDevice != null) {
                            BluetoothDeviceManager.getInstance().bindChannel(bluetoothLeDevice, PropertyType.PROPERTY_WRITE,
                                    UUID.fromString(GlaUtils.PEGASI_BRAINWAVE_SERVICE_UUID),
                                    UUID.fromString(GlaUtils.PILLOW_BRAINWAVE_CHARACTERISTIC_WRITE),
                                    null);
                            BluetoothDeviceManager.getInstance().write(bluetoothLeDevice, Constant.DEVICE_LAST_TIME.getBytes());
                        }
                    }
                    if (data.length == 1 && data[0] == (byte) 0xe2) {
                        refreshLogTextView("擦除数据成功\r\n");
                    }
                    //是同步的数据
                    if (data.length > 8) {
                        if (data.length == 20){
                            synchronized (lock){
                                if (data[0] <= CYCLE_TIME) {
                                    byte location = data[0];
                                    if (location == 0) {
                                        //拿到这条数据的时间戳
                                        byte[] timeByte = new byte[4];
                                        System.arraycopy(data, 4, timeByte, 0, 4);
                                        devicePageStartTime = HexUtil.byteToInt(timeByte, 0, true);
                                        if (enterOnce) {
                                            enterOnce = false;
                                            startTimeToDevice = devicePageStartTime;
                                        }
                                        byte[] sleepData = new byte[data.length - ONE_SKIP_BYTE];
                                        System.arraycopy(data, ONE_SKIP_BYTE, sleepData, 0, data.length - ONE_SKIP_BYTE);
                                        String hexStr = HexUtil.encodeHexStr(sleepData);
                                        ViseLog.i("整晚的睡眠数据:" + hexStr);
                                        totalData.add(hexStr);
                                    } else {
                                        byte[] sleepData = new byte[data.length - SKIP_BYTE];
                                        System.arraycopy(data, SKIP_BYTE, sleepData, 0, data.length - SKIP_BYTE);
                                        String hexStr = HexUtil.encodeHexStr(sleepData);
                                        ViseLog.i("整晚的睡眠数据:" + hexStr);
                                        totalData.add(hexStr);

                                        if (location == CYCLE_TIME) {
                                            sendNumberPage = sendNumberPage + 1;
//                                            ViseLog.i("发送的页数 = " + sendNumberPage);
                                            refreshLogTextView("发送的页数 = " + sendNumberPage+"\r\n");
                                            ViseLog.i("devicePageStartTime = " + devicePageStartTime);
                                            //需要发送读取下一页的数据
                                            if (lastStorageTime > devicePageStartTime) {
                                                byte[] lastTime = Constant.DEVICE_LAST_TIME.getBytes();
                                                byte[] readDeviceData = new byte[]{lastTime[0], (byte) (sendNumberPage & 0xff), (byte) (sendNumberPage >> 8 & 0xff)};
                                                if (bluetoothLeDevice != null) {
                                                    BluetoothDeviceManager.getInstance().bindChannel(bluetoothLeDevice, PropertyType.PROPERTY_WRITE,
                                                            UUID.fromString(GlaUtils.PEGASI_BRAINWAVE_SERVICE_UUID),
                                                            UUID.fromString(GlaUtils.PILLOW_BRAINWAVE_CHARACTERISTIC_WRITE),
                                                            null);
                                                    BluetoothDeviceManager.getInstance().write(bluetoothLeDevice, readDeviceData);
                                                }
                                            } else {
                                                refreshLogTextView("同步结束\r\n");
//                                                ViseLog.i("读取结束");
                                                //结束了,需要存到文件中
                                                if (totalData.size() > 0) {
                                                    Thread mThread = new Thread(writeFileRun);
                                                    mThread.start();
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }else {
                            refreshLogTextView("同步数据字节错误\r\n");
                        }
                    }
//                    ViseLog.d("接收到的数据 data = "+ new String(data));
                }
            }
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
        if (totalData != null) {
            totalData.clear();
        }
        checkValue = 0;
        enterOnce = true;
        sendNumberPage = 0;
        devicePageStartTime = 0;
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

    private void initViewAndEvent() {
        eegTvMessage.setOnTouchListener(new View.OnTouchListener() {
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
        eegTvMessage.setMovementMethod(ScrollingMovementMethod.getInstance());
    }
    private void refreshLogTextView(String message) {
        eegTvMessage.append(message);
        int offset = eegTvMessage.getLineCount() * eegTvMessage.getLineHeight();
        if (offset > eegTvMessage.getHeight()) {
            eegTvMessage.scrollTo(0, offset - eegTvMessage.getHeight());
        }
    }

    private byte[] hexStringToByte(String hex) {
        if (TextUtils.isEmpty(hex)) {
            return null;
        }
        int len = (hex.length() / 2);
        byte[] result = new byte[len];
        char[] achar = hex.toCharArray();
        for (int i = 0; i < len; i++) {
            int pos = i * 2;
            result[i] = (byte) (charToByte(achar[pos]) << 4 | charToByte(achar[pos + 1]));
        }
        return result;
    }
    private byte charToByte(char c) {
        return (byte) "0123456789abcdef".indexOf(c);
    }

    public void initLineChart(LineChart lineChart) {
        lineChart.getDescription().setEnabled(false);
        lineChart.setPinchZoom(false);
        lineChart.setDragEnabled(false);
        lineChart.setScaleEnabled(false);
        lineChart.setTouchEnabled(false);
    }

    public void setSleepXYAxis(LineChart lineChart, int beginTime,int endTime) {
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity((endTime - beginTime) / 5);//5分钟
        IAxisValueFormatter sleepFormatter = new SleepTimeAxisValueFormatter(beginTime);
        xAxis.setValueFormatter(sleepFormatter);

        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setLabelCount(5, true);
        leftAxis.setDrawGridLines(true);
        leftAxis.setDrawAxisLine(true);
        leftAxis.setGridColor(ColorTemplate.rgb("#eeeeee"));
        leftAxis.setGridLineWidth(1f);
        leftAxis.setSpaceTop(4f);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setAxisMaximum(110f);

        YAxis rightAxis = lineChart.getAxisRight();
        rightAxis.setEnabled(false);

        Legend lengend = lineChart.getLegend();
        lengend.setEnabled(true);
    }
    private void setLineProperties(LineDataSet dataSet, int lineColor){
        dataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        dataSet.setColor(lineColor);
        dataSet.setCircleColor(Color.WHITE);//设置节点的颜色
        dataSet.setDrawCircles(false);
        dataSet.setLineWidth(1f);
        dataSet.setCircleRadius(4f);
        dataSet.setFillAlpha(65);
        dataSet.setMode(LineDataSet.Mode.LINEAR);
        dataSet.setFillColor(ColorTemplate.getHoloBlue());
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(9f);
        dataSet.setDrawValues(false);
    }
    private void generateMultipleLineDataSet(LineChart lineChart,ArrayList<Entry> attentionData,ArrayList<Entry> meditationData,ArrayList<Entry> dividedData){
        LineDataSet attentionLine = new LineDataSet(attentionData, "专注度");
        setLineProperties(attentionLine,ColorTemplate.rgb("#FF7F00"));
        LineDataSet meditationLine = new LineDataSet(meditationData, "放松度");
        setLineProperties(meditationLine,ColorTemplate.rgb("#FFFF00"));
        LineDataSet dividedLine = new LineDataSet(dividedData, "差值");
        setLineProperties(dividedLine,ColorTemplate.rgb("#1F48CF"));


        LineData lineData = new LineData();
        lineData.addDataSet(meditationLine);
        lineData.addDataSet(attentionLine);
        lineData.addDataSet(dividedLine);
        lineChart.setData(lineData);
        //重新绘制
        lineChart.invalidate();
    }
}
