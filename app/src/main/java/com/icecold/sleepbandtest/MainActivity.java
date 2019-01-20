package com.icecold.sleepbandtest;

import android.Manifest;
import android.bluetooth.BluetoothGattDescriptor;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.icecold.sleepbandtest.adapter.CityAdapter;
import com.icecold.sleepbandtest.common.BluetoothDeviceManager;
import com.icecold.sleepbandtest.common.CustomPopWindow;
import com.icecold.sleepbandtest.event.CallbackDataEvent;
import com.icecold.sleepbandtest.event.ConnectEvent;
import com.icecold.sleepbandtest.event.NotifyDataEvent;
import com.icecold.sleepbandtest.event.ScanDeviceEvent;
import com.icecold.sleepbandtest.network.BandLoader;
import com.icecold.sleepbandtest.network.BaseRequest;
import com.icecold.sleepbandtest.ui.SelectCityActivity;
import com.icecold.sleepbandtest.utils.Constant;
import com.icecold.sleepbandtest.utils.FileUtil;
import com.icecold.sleepbandtest.utils.GlaUtils;
import com.icecold.sleepbandtest.utils.ParcelableUtil;
import com.icecold.sleepbandtest.utils.SPUtils;
import com.icecold.sleepbandtest.utils.Utils;
import com.icecold.sleepbandtest.widget.UncertainDialog;
import com.polidea.rxandroidble2.RxBleAdapterStateObservable;
import com.polidea.rxandroidble2.RxBleAdapterStateObservable_Factory;
import com.polidea.rxandroidble2.RxBleConnection;
import com.polidea.rxandroidble2.RxBleDevice;
import com.polidea.rxandroidble2.scan.ScanFilter;
import com.polidea.rxandroidble2.scan.ScanResult;
import com.polidea.rxandroidble2.scan.ScanSettings;
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

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import bleshadow.javax.inject.Provider;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.SingleSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.schedulers.Timed;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class MainActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 3;
    private static final int ENABLE_BLE_CODE = 4;
    private static final int SKIP_BYTE = 4;//需要跳过的字节数字
    private static final byte CYCLE_TIME = 0x0f;//循环的次数
    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;
    private static final String MAC_ADDRESS = "D1:04:CC:0C:F1:F6";
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
    private ArrayList<String> cityItem;
    private ArrayList<String> lastCity;
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
    private Disposable stateDisposable;
    private RxBleDevice rxBleDevice;
    private CompositeDisposable servicesDisposable = new CompositeDisposable();
    private Disposable connectDisposable;
    private RxBleConnection mRxBleConnection;
    private Disposable notificationDis;
    private CityAdapter mCityAdapter;

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
        EventBus.getDefault().register(this);
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
                scanAndConnectDevice(address);
//                String realAddress = reversalMacAddress(address);
//                BluetoothDeviceManager.getInstance().connectByMac(realAddress);
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
        ViseLog.uprootAll();
        Log.i(TAG, "销毁界面 ");
        ViseHttp.cancelTag("zhidao");
        if (bluetoothLeDevice != null) {
            BluetoothDeviceManager.getInstance().setmDeviceReconnected(bluetoothLeDevice, false);
        }
        servicesDisposable.clear();
//        退出APP的时候需要清除所有的资源并且断开所有的连接
        ViseBle.getInstance().clear();
        EventBus.getDefault().unregister(this);
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
//                switchFun();
//                Intent intent3 = new Intent(this,OftenActivity.class);
//                intent3.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                startActivity(intent3);
//                showUncertainDialog();

                IntentIntegrator integrator = new IntentIntegrator(this);
                integrator.setOrientationLocked(false);
                integrator.initiateScan();
                break;
            case R.id.btn_disconnect:
                //断开连接以及停止搜索
                servicesDisposable.clear();
//                switchFun();

//                ToastUtil.show(this,"断开连接");
//                ToastUtil.showAtCenter(this,"断开连接");
//                ToastUtil.makeText(this,"断开连接",Toast.LENGTH_SHORT);
//                if (bluetoothLeDevice != null) {
//                    BluetoothDeviceManager.getInstance().disconnect(bluetoothLeDevice);
//                }
                break;
            case R.id.btn_read_battery:
                byte[] deleteUserData = new byte[]{0x44};
                mRxBleConnection
                        .writeCharacteristic(UUID.fromString(GlaUtils.BAND_PEGASI_LOG_DATA_CHARACTERISTIC_UUID),deleteUserData)
                        .subscribeOn(Schedulers.computation())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<byte[]>() {
                            @Override
                            public void accept(byte[] bytes) throws Exception {
                                if (bytes[0] == 0x44){
                                    refreshLogTextView("删除数据成功\r\n");
                                }
                            }
                        }, new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) throws Exception {
                                refreshLogTextView("删除数据失败\r\n");
                            }
                        });
//                mRxBleConnection
//                        .readCharacteristic(UUID.fromString(GlaUtils.PILLOW_CHAR_UUID_BLVL))
//                        .observeOn(AndroidSchedulers.mainThread())
//                        .subscribe(new Consumer<byte[]>() {
//                            @Override
//                            public void accept(byte[] bytes) throws Exception {
//                                int batter = bytes[0] & 0xff;
//                                refreshLogTextView("设备电量百分比 "+batter+"%\r\n");
//                            }
//                        }, new Consumer<Throwable>() {
//                            @Override
//                            public void accept(Throwable throwable) throws Exception {
//                                refreshLogTextView("读取设备电量失败");
//                            }
//                        });
                //读取电量
                if (bluetoothLeDevice != null) {
//                    BluetoothDeviceManager.getInstance().bindChannel(bluetoothLeDevice,PropertyType.PROPERTY_READ,
//                            UUID.fromString(GlaUtils.PILLOW_SERV_UUID_BATT),UUID.fromString(GlaUtils.PILLOW_CHAR_UUID_BLVL),
//                            null);
//                    BluetoothDeviceManager.getInstance().read(bluetoothLeDevice);

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
                notificationDis = mRxBleConnection
                        .setupNotification(UUID.fromString(GlaUtils.BAND_PEGASI_SYNC_TIME_CHARACTERISTIC_UUID))
                        .doOnNext(new Consumer<Observable<byte[]>>() {
                            @Override
                            public void accept(Observable<byte[]> observable) throws Exception {
                                byte[] timeRead = "R".getBytes();
                                mRxBleConnection.writeCharacteristic(UUID.fromString(GlaUtils.BAND_PEGASI_SYNC_TIME_CHARACTERISTIC_UUID),timeRead)
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(new Consumer<byte[]>() {
                                            @Override
                                            public void accept(byte[] bytes) throws Exception {
                                                refreshLogTextView("使能成功后写入读取时间的值\r\n");
                                            }
                                        });
                                ViseLog.d("使能notification");
                            }
                        })
                        .flatMap(new Function<Observable<byte[]>, ObservableSource<byte[]>>() {
                            @Override
                            public ObservableSource<byte[]> apply(Observable<byte[]> observable) throws Exception {
                                return observable;
                            }
                        })
                        .subscribe(new Consumer<byte[]>() {
                            @Override
                            public void accept(byte[] bytes) throws Exception {
                                String hexStr = HexUtil.encodeHexStr(bytes);
                                ViseLog.d("使能得到的值 = " + hexStr);
                            }
                        }, new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) throws Exception {
                                ViseLog.d("使能错误 msg = " + throwable.getMessage());
                            }
                        });
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
                View contentView = LayoutInflater.from(this).inflate(R.layout.activity_select_city, null);
                //处理popWindow的显示内容
                handleLogic(contentView);
                //创建并且显示popWindow
                CustomPopWindow customPopWindow = new CustomPopWindow.PopupWindowBuilder(this)
                        .setView(contentView)
                        .enableOutsideTouchableDissmiss(true)
                        .size(openTime.getWidth(), ViewGroup.LayoutParams.WRAP_CONTENT)
                        .create()
                        .showAsDropDown(openTime, 0, 15);
//                Intent intent2 = new Intent(this, OftenActivity.class);
//                startActivity(intent2);

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
                Intent intent = new Intent(this, SelectCityActivity.class);
//                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
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

    private void handleLogic(View contentView) {
        initDefaultCityData();
        RecyclerView cityRecyclerView = contentView.findViewById(R.id.recycler_view_city);
        EditText searchCityText = contentView.findViewById(R.id.searchCity);

        //设置recyclerView相关的
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this) {
            @Override
            public void onMeasure(RecyclerView.Recycler recycler, RecyclerView.State state, int widthSpec, int heightSpec) {
//                View view = recycler.getViewForPosition(0);
//                if (view != null) {
//                    measureChild(view,widthSpec,heightSpec);
//                    int measureWidth = View.MeasureSpec.getSize(widthSpec);
//                    int measuredHeight = view.getMeasuredHeight();
//                    int showHeight = measuredHeight * state.getItemCount();
//                    if (state.getItemCount() > 5){
//                        showHeight = measuredHeight * 5;
//                    }
//                    recycler.recycleView(view);
//                    setMeasuredDimension(measureWidth,showHeight);
//                }
                try {
                    if (mCityAdapter != null && mCityAdapter.getItemHeight() > 0){
                        int measureWidth = View.MeasureSpec.getSize(widthSpec);
                        int itemHeight = mCityAdapter.getItemHeight();
                        int showHeight = itemHeight * state.getItemCount();
                        if (state.getItemCount() > 3){
                            showHeight = itemHeight * 3;
                        }
                        setMeasuredDimension(measureWidth,showHeight);
                    }else {
                        super.onMeasure(recycler, state, widthSpec, heightSpec);
                    }
                }catch (Exception e){
                    super.onMeasure(recycler, state, widthSpec, heightSpec);
                }
            }
        };
        linearLayoutManager.setAutoMeasureEnabled(false);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        layoutManager.setAutoMeasureEnabled(true);
        cityRecyclerView.setHasFixedSize(false);
        cityRecyclerView.setLayoutManager(linearLayoutManager);
        cityRecyclerView.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));
        mCityAdapter = new CityAdapter(R.layout.content_city, cityItem);
        cityRecyclerView.setAdapter(mCityAdapter);

        //设置searchEditText相关的
        searchCityText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (TextUtils.isEmpty(editable.toString())) {
                    updateData(cityItem);
                }else {
                    lastCity.clear();
                    for (String city : cityItem) {
                        if (city.contains(editable.toString().trim())) {
                            lastCity.add(city);
                        }
                    }
                    updateData(lastCity);
                }
            }
        });
    }

    private void initDefaultCityData() {
        cityItem = new ArrayList<>();
        lastCity = new ArrayList<>();
        cityItem.add("Mumbai");
        cityItem.add("Delhi");
        cityItem.add("Bengaluru");
        cityItem.add("Hyderabad");
        cityItem.add("Ahmedabad");
        cityItem.add("Chennai");
        cityItem.add("Kolkata");
        cityItem.add("Surat");
        cityItem.add("Pune");
        cityItem.add("Jaipur");
        cityItem.add("Lucknow");
        cityItem.add("Kanpur");
    }
    private void updateData(List<String> mData) {
        mCityAdapter.setNewData(mData);
    }

    @org.greenrobot.eventbus.Subscribe
    public void deviceConnectedState(ScanDeviceEvent scanDeviceEvent){
        if (scanDeviceEvent.isScanSuccess()) {
            refreshLogTextView("连接设备成功\r\n");
            //搜索到设备，发起连接以及同步时间操作
            if (scanDeviceEvent.getRxBleDevice() != null) {
                rxBleDevice = scanDeviceEvent.getRxBleDevice();
                long currentWriteTime = System.currentTimeMillis() / 1000;
                byte[] writeDeviceTime = "W".getBytes();
                final byte[] data = new byte[]{writeDeviceTime[0],(byte) (currentWriteTime & 0xff),
                        (byte) ((currentWriteTime >> 8) & 0xff),
                        (byte) ((currentWriteTime >> 16) & 0xff),
                        (byte) ((currentWriteTime >> 24) & 0xff),
                        (byte) 0, (byte) 0, (byte) 0, (byte) 0};
                String hexStr = HexUtil.encodeHexStr(data);
                ViseLog.d("写入的值的hex = "+hexStr);
                connectDisposable = rxBleDevice.establishConnection(false)
                        .subscribeOn(Schedulers.io())
                        .flatMapSingle(new Function<RxBleConnection, SingleSource<byte[]>>() {
                            @Override
                            public SingleSource<byte[]> apply(RxBleConnection rxBleConnection) throws Exception {
                                mRxBleConnection = rxBleConnection;
                                return rxBleConnection.writeCharacteristic(UUID.fromString(GlaUtils.BAND_PEGASI_SYNC_TIME_CHARACTERISTIC_UUID), data);
                            }
                        })
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<byte[]>() {
                            @Override
                            public void accept(byte[] bytes) throws Exception {
                                String writeString = HexUtil.encodeHexStr(bytes);
                                ViseLog.d("写入成功了,写入的值是" + writeString);
                            }
                        }, new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) throws Exception {
                                ViseLog.i("写操作不成功 msg = "+throwable.getMessage());
                            }
                        });
                servicesDisposable.add(connectDisposable);
            }
        }
        if (scanDeviceEvent.isScanTimeout()) {
            refreshLogTextView("搜索设备超时\r\n");
            ViseLog.d("搜索设备超时了");
        }
    }

    private void switchFun() {
        Observable<Integer> integerObservable = Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
                for (int i = 0; i < 5; i++) {
                    if (i > 2) {
                        emitter.onError(new RuntimeException("VALUE TO MAX"));
                    }
                    emitter.onNext(i);
                }
                emitter.onComplete();
            }
        }).subscribeOn(Schedulers.computation());
        final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss", Locale.CHINA);
        ViseLog.d("delay start :"+dateFormat.format(new Date()));
        integerObservable
                .delay(3,TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        ViseLog.i("delay onNext: " + dateFormat.format(new Date()) + "-->" + integer);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        ViseLog.i("delay onError "+throwable.getMessage()+" thread = "+Thread.currentThread().getName());
                    }
                }, new Action() {
                    @Override
                    public void run() throws Exception {
                        ViseLog.i("delay onCompleted time = "+dateFormat.format(new Date())+" thread = "+Thread.currentThread().getName());
                    }
                });
        RxJavaPlugins.setErrorHandler(new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                if (throwable instanceof RuntimeException) {
                    ViseLog.i("runTime cause = "+throwable.getCause().getMessage());
                }
                ViseLog.i("错误处理器处理错误  msg = "+throwable.getMessage());
            }
        });
        ViseLog.d("delaySubscription start:"+dateFormat.format(new Date()));
        integerObservable
                .delaySubscription(3,TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        ViseLog.i("delaySubscription onNext:"+dateFormat.format(new Date())+"-->"+integer);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        ViseLog.i("delaySubscription onError "+throwable.getMessage()+" thread = "+Thread.currentThread().getName());
                    }
                }, new Action() {
                    @Override
                    public void run() throws Exception {
                        ViseLog.i("delaySubscription onCompleted time = "+dateFormat.format(new Date())+" thread = "+Thread.currentThread().getName());
                    }
                });
//        Disposable disposable = Observable.just(5)
//                .subscribeOn(Schedulers.io())
//                .switchMap(new Function<Integer, ObservableSource<String>>() {
//                    @Override
//                    public ObservableSource<String> apply(Integer integer) throws Exception {
//                        String[] showText = new String[integer];
//                        for (Integer i = 0; i < integer; i++) {
//                            showText[i] = "显示的值 value = " + i;
//                        }
//                        ViseLog.i(Thread.currentThread().getName());
//                        return Observable.fromArray(showText);
//                    }
//                })
//                .observeOn(AndroidSchedulers.mainThread())
//                .doFinally(new Action() {
//                    @Override
//                    public void run() throws Exception {
//                        ViseLog.i(Thread.currentThread().getName());
//                        ViseLog.d("说明取消订阅了");
//                    }
//                })
//                .subscribe(new Consumer<String>() {
//                    @Override
//                    public void accept(String s) throws Exception {
//                        ViseLog.i(s);
//                    }
//                });
//        disposable.dispose();
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
    private void scanAndConnectDevice(final String macAddress){
        final long[] startTime = new long[1];
        startTime[0] = System.currentTimeMillis() / 1000;
        Disposable scanDeviceDisposable = MyApplication.getRxBleClient(this)
                .scanBleDevices(
                        new ScanSettings.Builder()
                                .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
                                .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                                .build(),
                        new ScanFilter.Builder()
//                .setDeviceAddress(MAC_ADDRESS)
                                .build())
                .timestamp(TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                //实现类似于超时时间与过滤的设置的操作
                .filter(new Predicate<Timed<ScanResult>>() {
                    @Override
                    public boolean test(Timed<ScanResult> scanResultTimed) throws Exception {
                        long time = scanResultTimed.time(TimeUnit.SECONDS);
                        if (time - startTime[0] > 60) {//超时的时间设置
                            ViseLog.d("返回false ");
//                            return false;
                            throw new RuntimeException("超时了");
                        } else if (scanResultTimed.value().getBleDevice().getMacAddress().equals(macAddress)) {
                            ViseLog.d("返回true ");
                            return true;
                        } else {
                            return false;
                        }
                    }
                })
                .firstElement()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Timed<ScanResult>>() {
                    @Override
                    public void accept(Timed<ScanResult> scanResultTimed) throws Exception {
                        EventBus.getDefault().post(
                                new ScanDeviceEvent()
                                        .setScanSuccess(true)
                                        .setRxBleDevice(scanResultTimed.value().getBleDevice()));
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        EventBus.getDefault().post(
                                new ScanDeviceEvent()
                                        .setScanTimeout(true));
                    }
                });
        servicesDisposable.add(scanDeviceDisposable);
    }

    private void checkBluetoothPermission() {
        RxBleAdapterStateObservable_Factory stateObservableFactory = RxBleAdapterStateObservable_Factory.create(new Provider<Context>() {
            @Override
            public Context get() {
                return MainActivity.this;
            }
        });
        Disposable stateDisposable = stateObservableFactory.get().subscribe(new Consumer<RxBleAdapterStateObservable.BleAdapterState>() {
            @Override
            public void accept(RxBleAdapterStateObservable.BleAdapterState bleAdapterState) throws Exception {
                if (RxBleAdapterStateObservable.BleAdapterState.STATE_ON == bleAdapterState) {
                    ViseLog.d("蓝牙打开了");
                }
                if (RxBleAdapterStateObservable.BleAdapterState.STATE_OFF == bleAdapterState) {
                    ViseLog.d("蓝牙关闭了");
                }
//                ViseLog.i("观察的状态 "+bleAdapterState.isUsable());
            }
        });
        servicesDisposable.add(stateDisposable);
        /*
        final long[] startTime = new long[1];
        stateDisposable = MyApplication.getRxBleClient(this)
                .observeStateChanges()
                .switchMap(new Function<RxBleClient.State, ObservableSource<Timed<ScanResult>>>() {
                    @Override
                    public ObservableSource<Timed<ScanResult>> apply(RxBleClient.State state) throws Exception {
                        switch (state) {
                            case READY:
                                startTime[0] = System.currentTimeMillis() / 1000;
                                ViseLog.d("开始的观察时间 = "+startTime[0]);
                                return MyApplication.getRxBleClient(MainActivity.this)
                                        .scanBleDevices(
                                                new ScanSettings.Builder()
                                                        .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                                                        .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
                                                        .build(),
                                                new ScanFilter.Builder()
//                                                        .setDeviceAddress("D1:04:CC:0C:F1:F6")
                                                        .build())
                                        .timestamp(TimeUnit.SECONDS);
                            case BLUETOOTH_NOT_AVAILABLE:
                            case BLUETOOTH_NOT_ENABLED:
                                enableBluetooth();
                            case LOCATION_SERVICES_NOT_ENABLED:
                            case LOCATION_PERMISSION_NOT_GRANTED:
                            default:
                                return Observable.empty();
                        }
                    }
                })
                //实现类似于超时时间与过滤的设置的操作
                .filter(new Predicate<Timed<ScanResult>>() {
                    @Override
                    public boolean test(Timed<ScanResult> scanResultTimed) throws Exception {
                        long time = scanResultTimed.time(TimeUnit.SECONDS);
                        if (time - startTime[0] > 60){//超时的时间设置
                            ViseLog.d("返回false ");
//                            return false;
                            throw new RuntimeException("超时了");
                        }else if (scanResultTimed.value().getBleDevice().getMacAddress().equals(MAC_ADDRESS)){
                            ViseLog.d("返回true ");
                            return true;
                        }else {
                            return false;
                        }
                    }
                })
                .firstElement()//只会发射被观察者的onNext中的第一项元素,发完以后就会调用Cancel取消订阅
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Timed<ScanResult>>() {
                    @Override
                    public void accept(Timed<ScanResult> scanResultTimed) throws Exception {
                        ViseLog.d("搜索到设备信息 = " + scanResultTimed.value().getBleDevice().getMacAddress());
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        ViseLog.d("发生错误 = " + throwable.getMessage());
                    }
                });
        servicesDisposable.add(stateDisposable);
        */
        /*
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
        */
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
