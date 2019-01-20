package com.icecold.sleepbandtest.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.icecold.sleepbandtest.R;
import com.icecold.sleepbandtest.common.BluetoothDeviceManager;
import com.icecold.sleepbandtest.event.CallbackDataEvent;
import com.icecold.sleepbandtest.event.ConnectEvent;
import com.icecold.sleepbandtest.event.NotifyDataEvent;
import com.icecold.sleepbandtest.utils.GlaUtils;
import com.icecold.sleepbandtest.utils.Utils;
import com.vise.baseble.ViseBle;
import com.vise.baseble.common.PropertyType;
import com.vise.baseble.model.BluetoothLeDevice;
import com.vise.baseble.utils.BleUtil;
import com.vise.baseble.utils.HexUtil;
import com.vise.log.ViseLog;
import com.vise.log.inner.LogcatTree;
import com.vise.xsnow.event.BusManager;
import com.vise.xsnow.event.Subscribe;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class BrainwaveActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 2;
    private static final int ENABLE_BLE_CODE = 3;
    private static final int CUT_COUNT = 3;
    @BindView(R.id.connect_device)
    Button connectDevice;
    @BindView(R.id.btn_signal)
    Button btnSignal;
    @BindView(R.id.btn_delta)
    Button btnDelta;
    @BindView(R.id.btn_theta)
    Button btnTheta;
    @BindView(R.id.btn_lowAlpha)
    Button btnLowAlpha;
    @BindView(R.id.btn_highAlpha)
    Button btnHighAlpha;
    @BindView(R.id.btn_lowBeta)
    Button btnLowBeta;
    @BindView(R.id.btn_highBeta)
    Button btnHighBeta;
    @BindView(R.id.btn_lowGamma)
    Button btnLowGamma;
    @BindView(R.id.btn_middleGamma)
    Button btnMiddleGamma;
    @BindView(R.id.btn_attention)
    Button btnAttention;
    @BindView(R.id.btn_meditation)
    Button btnMeditation;
    @BindView(R.id.eeg_power)
    LineChart eegPower;
    @BindView(R.id.other_item)
    LineChart otherItem;
    private BluetoothLeDevice bluetoothLeDevice;
    private byte[] tempData;
    private byte[] realData;
    private boolean deltaNotSelete;
    private boolean thetaNotSelete;
    private boolean lowAlphaNotSelete;
    private boolean highAlphaNotSelete;
    private boolean lowBetaNotSelete;
    private boolean highBetaNotSelete;
    private boolean lowGammaNotSelete;
    private boolean middleGammaNotSelete;
    private boolean signalNotSelete;
    private boolean attentionNotSelete;
    private boolean meditationNotSelete;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_brainwave);

        ButterKnife.bind(this);
        ViseLog.getLogConfig().configAllowLog(true);//配置日志信息
        ViseLog.plant(new LogcatTree());//添加logcat打印信息
        BusManager.getBus().register(this);
        //初始化蓝牙
        BluetoothDeviceManager.getInstance().init(this);
        initView();
        checkBluetoothPermission();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "取消扫描", Toast.LENGTH_LONG).show();
            } else {
                String address = result.getContents();
                showToastWithMessage("连接中,耐心等会");
                BluetoothDeviceManager.getInstance().connectByMac(address);
            }
        }
        if (requestCode == RESULT_CANCELED) {
            //取消打开蓝牙，直接退出
            finish();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @OnClick({R.id.connect_device, R.id.btn_signal, R.id.btn_delta, R.id.btn_theta, R.id.btn_lowAlpha,
            R.id.btn_highAlpha, R.id.btn_lowBeta, R.id.btn_highBeta, R.id.btn_lowGamma, R.id.btn_middleGamma,
            R.id.btn_attention, R.id.btn_meditation})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.connect_device:
                IntentIntegrator integrator = new IntentIntegrator(this);
                integrator.setOrientationLocked(false);
                integrator.initiateScan();
                break;
            case R.id.btn_signal:
                signalNotSelete = !signalNotSelete;

                btnSignal.setSelected(!signalNotSelete);
                if (otherItem.getData() != null && otherItem.getData().getDataSetByIndex(0) != null) {
                    otherItem.getData().getDataSetByIndex(0).setVisible(!signalNotSelete);
                }
                break;
            case R.id.btn_delta:
                deltaNotSelete = !deltaNotSelete;
                //改变选中状态
//                if (deltaNotSelete) {
//                    //灰
//                    btnDelta.setBackgroundColor(ColorTemplate.rgb("#5e5b5b"));
//                }else {
//                    //红色
//                    btnDelta.setBackgroundColor(ColorTemplate.rgb("#FF4081"));
//                }

                btnDelta.setSelected(!deltaNotSelete);
                //隐藏或者显示对应的线
                if (eegPower.getData() != null && eegPower.getData().getDataSetByIndex(0) != null) {
                    eegPower.getData().getDataSetByIndex(0).setVisible(!deltaNotSelete);
                }
                break;
            case R.id.btn_theta:
                thetaNotSelete = !thetaNotSelete;

                btnTheta.setSelected(!thetaNotSelete);

                if (eegPower.getData() != null && eegPower.getData().getDataSetByIndex(1) != null) {
                    eegPower.getData().getDataSetByIndex(1).setVisible(!thetaNotSelete);
                }
                break;
            case R.id.btn_lowAlpha:
                lowAlphaNotSelete = !lowAlphaNotSelete;

                btnLowAlpha.setSelected(!lowAlphaNotSelete);

                if (eegPower.getData() != null && eegPower.getData().getDataSetByIndex(2) != null) {
                    eegPower.getData().getDataSetByIndex(2).setVisible(!lowAlphaNotSelete);
                }
                break;
            case R.id.btn_highAlpha:
                highAlphaNotSelete = !highAlphaNotSelete;

                btnHighAlpha.setSelected(!highAlphaNotSelete);

                if (eegPower.getData() != null && eegPower.getData().getDataSetByIndex(3) != null) {
                    eegPower.getData().getDataSetByIndex(3).setVisible(!highAlphaNotSelete);
                }
                break;
            case R.id.btn_lowBeta:
                lowBetaNotSelete = !lowBetaNotSelete;

                btnLowBeta.setSelected(!lowBetaNotSelete);

                if (eegPower.getData() != null && eegPower.getData().getDataSetByIndex(4) != null) {
                    eegPower.getData().getDataSetByIndex(4).setVisible(!lowBetaNotSelete);
                }

                break;
            case R.id.btn_highBeta:
                highBetaNotSelete = !highBetaNotSelete;

                btnHighBeta.setSelected(!highBetaNotSelete);

                if (eegPower.getData() != null && eegPower.getData().getDataSetByIndex(5) != null) {
                    eegPower.getData().getDataSetByIndex(5).setVisible(!highBetaNotSelete);
                }
                break;
            case R.id.btn_lowGamma:
                lowGammaNotSelete = !lowGammaNotSelete;

                btnLowGamma.setSelected(!lowGammaNotSelete);

                if (eegPower.getData() != null && eegPower.getData().getDataSetByIndex(6) != null) {
                    eegPower.getData().getDataSetByIndex(6).setVisible(!lowGammaNotSelete);
                }

                break;
            case R.id.btn_middleGamma:
                middleGammaNotSelete = !middleGammaNotSelete;

                btnMiddleGamma.setSelected(!middleGammaNotSelete);

                if (eegPower.getData() != null && eegPower.getData().getDataSetByIndex(7) != null) {
                    eegPower.getData().getDataSetByIndex(7).setVisible(!middleGammaNotSelete);
                }
                break;
            case R.id.btn_attention:
                attentionNotSelete = !attentionNotSelete;

                btnAttention.setSelected(!attentionNotSelete);
                if (otherItem.getData() != null && otherItem.getData().getDataSetByIndex(1) != null) {
                    otherItem.getData().getDataSetByIndex(1).setVisible(!attentionNotSelete);
                }
                break;
            case R.id.btn_meditation:
                meditationNotSelete = !meditationNotSelete;

                btnMeditation.setSelected(!meditationNotSelete);
                if (otherItem.getData() != null && otherItem.getData().getDataSetByIndex(2) != null) {
                    otherItem.getData().getDataSetByIndex(2).setVisible(!meditationNotSelete);
                }
                break;
        }
    }

    @Subscribe
    public void showConnectDevice(ConnectEvent event) {
        if (event != null) {
            if (event.isSuccess()) {
                //显示成功
                if (event.getDeviceMirror() != null) {
                    showToastWithMessage("连接成功");
                    bluetoothLeDevice = event.getDeviceMirror().getBluetoothLeDevice();
                    if (bluetoothLeDevice != null) {
                        //使能相应的notify
                        BluetoothDeviceManager.getInstance().initEnableChannel(bluetoothLeDevice);
                        BluetoothDeviceManager.getInstance().bindChannel(bluetoothLeDevice, PropertyType.PROPERTY_NOTIFY,
                                UUID.fromString(GlaUtils.PEGASI_BRAINWAVE_SERVICE_UUID),
                                UUID.fromString(GlaUtils.PEGASI_BRAINWAVE_TX_SERVICE_CHARACTERISTIC_UUID),
                                null);
                        BluetoothDeviceManager.getInstance().registerNotify(bluetoothLeDevice, false);
                    }
                }
            } else {
                if (event.isDisconnected()) {
                    showToastWithMessage("主动断开连接");
                } else {
                    showToastWithMessage("意外断开连接");
                }
            }
        }
    }

    @Subscribe
    public void operationCallBack(CallbackDataEvent event) {

    }

    //notify回来的数据在这里接收
    @Subscribe
    public void bleNotifyCallBackEvent(NotifyDataEvent event) {
        if (event != null) {
            if (event.getBluetoothGattChannel() != null) {
                if (event.getBluetoothGattChannel().getCharacteristicUUID()
                        .compareTo(UUID.fromString(GlaUtils.PEGASI_BRAINWAVE_TX_SERVICE_CHARACTERISTIC_UUID)) == 0) {
                    byte[] eventData = event.getData();
                    if (eventData[0] == (byte) 0xaa && eventData[1] == (byte) 0xaa) {
                        realData = new byte[eventData.length + 16];
                        tempData = eventData;
                        System.arraycopy(tempData, 0, realData, 0, tempData.length);
//                        ViseLog.i("第一次接收到的数据 fData = "+ HexUtil.encodeHexStr(tempData));
                    } else {
                        System.arraycopy(eventData, 0, realData, 20, eventData.length);
//                        ViseLog.i("第二次接收到的数据 sData = "+ HexUtil.encodeHexStr(eventData));
                        ViseLog.i("存起来有用的数据 realData = " + HexUtil.encodeHexStr(realData));
                        int delta = Utils.getInstance().threeByteToInt(HexUtil.subBytes(realData, 7, CUT_COUNT), 0, true);
                        int theta = Utils.getInstance().threeByteToInt(HexUtil.subBytes(realData, 10, CUT_COUNT), 0, true);
                        int lowAlpha = Utils.getInstance().threeByteToInt(HexUtil.subBytes(realData, 13, CUT_COUNT), 0, true);
                        int highAlpha = Utils.getInstance().threeByteToInt(HexUtil.subBytes(realData, 16, CUT_COUNT), 0, true);
                        int lowBeta = Utils.getInstance().threeByteToInt(HexUtil.subBytes(realData, 19, CUT_COUNT), 0, true);
                        int highBeta = Utils.getInstance().threeByteToInt(HexUtil.subBytes(realData, 22, CUT_COUNT), 0, true);
                        int lowGamma = Utils.getInstance().threeByteToInt(HexUtil.subBytes(realData, 25, CUT_COUNT), 0, true);
                        int middleGamma = Utils.getInstance().threeByteToInt(HexUtil.subBytes(realData, 28, CUT_COUNT), 0, true);

                        int signal = realData[4] & 0xff;
                        int attention = realData[32] & 0xff;
                        int meditation = realData[34] & 0xff;
                        LinkedList linkedList = new LinkedList();
                        ViseLog.i("signal = "+signal+" attention = "+attention+" meditation = "+meditation);
//                        ViseLog.i("delta = " + delta + " theta = " + theta + " lowAlpha = " + lowAlpha + " highAlpha = " + highAlpha
//                                + " lowBeta = " + lowBeta + " highBeta = " + highBeta + " lowGamma = " + lowGamma + " middleGamma = " + middleGamma);
                        addEntry(eegPower,delta,theta,
                                lowAlpha,highAlpha,lowBeta,
                                highBeta,lowGamma,middleGamma);
                        addOtherEntry(otherItem,signal,attention,meditation);
                    }
                }
            }
        }

    }

    @Override
    protected void onDestroy() {

        //退出APP的时候需要清除所有的资源并且断开所有的连接
        ViseBle.getInstance().clear();
        BusManager.getBus().unregister(this);
        super.onDestroy();
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

    private void showToastWithMessage(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    private void enableBluetooth() {
        if (!BleUtil.isBleEnable(this)) {
            BleUtil.enableBluetooth(this, ENABLE_BLE_CODE);
        }
    }

    private void initView() {
        connectDevice.setSelected(true);
        btnSignal.setSelected(true);
        btnDelta.setSelected(true);
        btnTheta.setSelected(true);
        btnLowAlpha.setSelected(true);
        btnHighAlpha.setSelected(true);
        btnLowBeta.setSelected(true);
        btnHighBeta.setSelected(true);
        btnLowGamma.setSelected(true);
        btnMiddleGamma.setSelected(true);
        btnAttention.setSelected(true);
        btnMeditation.setSelected(true);
        gestureSetWithLineChart(eegPower);
        setXYAxis(eegPower,1000f,2000000f);
        LineData powerEmptyData = new LineData();
        //add empty data
        eegPower.setData(powerEmptyData);

        gestureSetWithLineChart(otherItem);
        setXYAxis(otherItem,0f,256f);
        LineData otherData = new LineData();
        //add empty data
        otherItem.setData(otherData);
    }
    private void gestureSetWithLineChart(LineChart lineChart) {
        lineChart.getDescription().setEnabled(false);
        lineChart.setPinchZoom(false);
        lineChart.setDragEnabled(false);
        lineChart.setScaleEnabled(false);
        lineChart.setTouchEnabled(false);
    }

    private void setXYAxis(LineChart lineChart, float min, float max) {
        //x轴
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setEnabled(false);
        //y轴
        YAxis axisLeft = lineChart.getAxisLeft();
        axisLeft.setDrawGridLines(true);//画背景线
        axisLeft.setDrawAxisLine(false);//是否画y轴的线
        axisLeft.setSpaceTop(5f);
        axisLeft.setLabelCount(4, true);
        axisLeft.setAxisMinimum(min);
        axisLeft.setAxisMaximum(max);
        axisLeft.setGridLineWidth(1f);
        axisLeft.setGridColor(ColorTemplate.rgb("#eeeeee"));
        axisLeft.setEnabled(false);

        YAxis axisRight = lineChart.getAxisRight();
        axisRight.setEnabled(false);
        //设置描述
        Legend legend = lineChart.getLegend();
        legend.setEnabled(false);
    }
    private LineDataSet createEegMultipleSet(float initValue, LineData mData) {

        LineDataSet deltaSet = generateDataSet(initValue,"data-1");
        setLineProperties(deltaSet,ColorTemplate.rgb("#FF0000"));

        //实验验证用于区分set1和set2与list1和list2有很大的关系，
        // 如果list1与list2指向的是一个地址，则set1与set2的值会变成一致
        LineDataSet thetaSet = generateDataSet(initValue, "data-2");
        setLineProperties(thetaSet,ColorTemplate.rgb("#FF7F00"));

        LineDataSet lowAlphaSet = generateDataSet(initValue, "data-3");
        setLineProperties(lowAlphaSet,ColorTemplate.rgb("#FFFF00"));

        LineDataSet highAlphaSet = generateDataSet(initValue, "data-4");
        setLineProperties(highAlphaSet,ColorTemplate.rgb("#00FF00"));

        LineDataSet lowBetaSet = generateDataSet(initValue, "data-5");
        setLineProperties(lowBetaSet,ColorTemplate.rgb("#00FFFF"));

        LineDataSet highBetaSet = generateDataSet(initValue, "data-6");
        setLineProperties(highBetaSet,ColorTemplate.rgb("#0000FF"));

        LineDataSet lowGammaSet = generateDataSet(initValue, "data-7");
        setLineProperties(lowGammaSet,ColorTemplate.rgb("#8B00FF"));

        LineDataSet middleGammaSet = generateDataSet(initValue, "data-8");
        setLineProperties(middleGammaSet,ColorTemplate.rgb("#5e5b5b"));

        mData.addDataSet(deltaSet);
        mData.addDataSet(thetaSet);
        mData.addDataSet(lowAlphaSet);
        mData.addDataSet(highAlphaSet);
        mData.addDataSet(lowBetaSet);
        mData.addDataSet(highBetaSet);
        mData.addDataSet(lowGammaSet);
        mData.addDataSet(middleGammaSet);
        return deltaSet;
    }
    private LineDataSet createOtherMultipleSet(float initValue, LineData mData) {
        
        LineDataSet signalSet = generateDataSet(initValue, "data-9");
        setLineProperties(signalSet,ColorTemplate.rgb("#F08080"));

        LineDataSet attentionSet = generateDataSet(initValue, "data-10");
        setLineProperties(attentionSet,ColorTemplate.rgb("#FF6347"));

        LineDataSet meditationSet = generateDataSet(initValue, "data-11");
        setLineProperties(meditationSet,ColorTemplate.rgb("FF4500"));

        mData.addDataSet(signalSet);
        mData.addDataSet(attentionSet);
        mData.addDataSet(meditationSet);

        return signalSet;
    }
    private void addEntry(LineChart lineChart, float delta, float theta,
                          float lowAlpha,float highAlpha,float lowBeta,
                          float highBeta,float lowGamma,float middleGamma) {

        LineData data = lineChart.getData();

        if (data != null) {

            ILineDataSet set = data.getDataSetByIndex(0);
            if (set == null) {

                set = createEegMultipleSet(500000f, data);
            }

            data.addEntry(new Entry(set.getEntryCount(), delta), 0);
            data.addEntry(new Entry(set.getEntryCount(), theta), 1);
            data.addEntry(new Entry(set.getEntryCount(), lowAlpha), 2);
            data.addEntry(new Entry(set.getEntryCount(), highAlpha), 3);
            data.addEntry(new Entry(set.getEntryCount(), lowBeta), 4);
            data.addEntry(new Entry(set.getEntryCount(), highBeta), 5);
            data.addEntry(new Entry(set.getEntryCount(), lowGamma), 6);
            data.addEntry(new Entry(set.getEntryCount(), middleGamma), 7);

            data.notifyDataChanged();
            // let the chart know it's data has changed
            lineChart.notifyDataSetChanged();
            // limit the number of visible entries
            lineChart.setVisibleXRangeMaximum(40);
            // move to the latest entry
            lineChart.moveViewToX(data.getEntryCount());
        }
    }
    private void addOtherEntry(LineChart lineChart,float signal,float attention,float meditation){

        LineData data = lineChart.getData();
        if (data != null) {

            ILineDataSet set = data.getDataSetByIndex(0);
            if (set == null) {

                set = createOtherMultipleSet(70f, data);
            }

            data.addEntry(new Entry(set.getEntryCount(), signal), 0);
            data.addEntry(new Entry(set.getEntryCount(), attention), 1);
            data.addEntry(new Entry(set.getEntryCount(), meditation), 2);


            data.notifyDataChanged();
            // let the chart know it's data has changed
            lineChart.notifyDataSetChanged();
            // limit the number of visible entries
            lineChart.setVisibleXRangeMaximum(40);
            // move to the latest entry
            lineChart.moveViewToX(data.getEntryCount());
        }
    }
    private LineDataSet generateDataSet(float startValue,String labelName){

        ArrayList<Entry> data = new ArrayList<>();
        data.add(new Entry(0f,startValue));
        return new LineDataSet(data, labelName);
    }
    private void setLineProperties(LineDataSet dataSet,int lineColor){
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
}
