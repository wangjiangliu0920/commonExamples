package com.icecold.sleepbandtest.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcel;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.View;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.icecold.sleepbandtest.R;
import com.icecold.sleepbandtest.common.BluetoothDeviceManager;
import com.icecold.sleepbandtest.event.NotifyDataEvent;
import com.icecold.sleepbandtest.utils.Constant;
import com.icecold.sleepbandtest.utils.GlaUtils;
import com.icecold.sleepbandtest.utils.ParcelableUtil;
import com.icecold.sleepbandtest.utils.RxTimer;
import com.icecold.sleepbandtest.utils.SPUtils;
import com.icecold.sleepbandtest.utils.Utils;
import com.vise.baseble.common.PropertyType;
import com.vise.baseble.model.BluetoothLeDevice;
import com.vise.xsnow.event.BusManager;
import com.vise.xsnow.event.Subscribe;

import java.util.ArrayList;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class BrightCurveActivity extends AppCompatActivity implements RxTimer.RxAction {

    @BindView(R.id.title_tv)
    TextView mTitleTv;
    @BindView(R.id.bright_value_chart)
    LineChart mBrightValueChart;
    @BindView(R.id.bright_value)
    TextView mBrightValue;
    private RxTimer mRxTimer;
    private BluetoothLeDevice mBluetooth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bright_curve);
        ButterKnife.bind(this);
        BusManager.getBus().register(this);
        initView();
        initData();
        //发送读取设备光亮的指令
        readDeviceLightOperate();
        //启动定时器，并且在时间到达时，发送读取设备的光亮指令
        mRxTimer.interval(500, this);
    }

    @Override
    protected void onDestroy() {
        BusManager.getBus().unregister(this);
        super.onDestroy();
        if (mRxTimer != null) {
            mRxTimer.cancel();
        }
    }

    @Override
    public void action(long number) {
        readDeviceLightOperate();
    }

    @OnClick(R.id.back_ib)
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.back_ib:
                this.onBackPressed();
                break;
            default:
                break;
        }
    }

    @Subscribe
    public void bleNotifyCallBackEvent(NotifyDataEvent event) {
        if (event != null && event.getBluetoothGattChannel() != null) {
            byte[] data = event.getData();
            if (event.getBluetoothGattChannel().getCharacteristicUUID()
                    .compareTo(UUID.fromString(GlaUtils.PEGASI_BRAINWAVE_TX_SERVICE_CHARACTERISTIC_UUID)) == 0) {
                addBrightEntry(mBrightValueChart, Utils.getInstance().byteToInt(data));
            }
        }
    }

    private void initView() {
        mTitleTv.setText("光照度");

    }

    private void initData() {
        //获取连接到设备的蓝牙对象
        Parcel parcel = ParcelableUtil.unmarshall(Base64.decode(
                SPUtils.getInstance(Constant.SHARED_PREFERENCE_NAME).getString(Constant.BLUETOOTH_DEVICE_KEY, "default")
                , 0));
        mBluetooth = null;
        mBluetooth = BluetoothLeDevice.CREATOR.createFromParcel(parcel);
        //初始化定时器
        if (null == mRxTimer) {
            mRxTimer = new RxTimer();
        }
        //光线值图初始化
        gestureSetWithLineChart(mBrightValueChart);
        setXYAxis(mBrightValueChart, 0);
        LineData brightData = new LineData();
        // add empty data
        mBrightValueChart.setData(brightData);
    }

    private void readDeviceLightOperate() {
        if (mBluetooth != null) {
            byte[] lightCommand = "@DB".getBytes();
            if (BluetoothDeviceManager.getInstance().isConnected(mBluetooth)) {
//                BluetoothDeviceManager.getInstance().initEnableChannel(mBluetooth);
                BluetoothDeviceManager.getInstance().bindChannel(mBluetooth, PropertyType.PROPERTY_WRITE,
                        UUID.fromString(GlaUtils.PEGASI_BRAINWAVE_SERVICE_UUID),
                        UUID.fromString(GlaUtils.PILLOW_BRAINWAVE_CHARACTERISTIC_WRITE),
                        null);
                BluetoothDeviceManager.getInstance().write(mBluetooth, lightCommand);
            }
        }
    }

    private void gestureSetWithLineChart(LineChart lineChart) {
        lineChart.getDescription().setEnabled(false);
        lineChart.setPinchZoom(false);
        lineChart.setDragEnabled(false);
        lineChart.setScaleEnabled(false);
        lineChart.setTouchEnabled(false);
    }

    private void setXYAxis(LineChart lineChart, float min) {
        //x轴
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(true);
        xAxis.setEnabled(true);
//        xAxis.setAxisMaximum(100);
        //y轴
        YAxis axisLeft = lineChart.getAxisLeft();
        axisLeft.setDrawGridLines(true);//画背景线
        axisLeft.setDrawAxisLine(false);//是否画y轴的线
        axisLeft.setSpaceTop(5f);
        axisLeft.setLabelCount(4, true);
        axisLeft.setAxisMinimum(min);
//        axisLeft.setAxisMaximum(max);
        axisLeft.setGridLineWidth(1f);
        axisLeft.setGridColor(ColorTemplate.rgb("#eeeeee"));
        axisLeft.setEnabled(true);

        YAxis axisRight = lineChart.getAxisRight();
        axisRight.setEnabled(false);
        //设置描述
        Legend legend = lineChart.getLegend();
        legend.setEnabled(false);
    }

    private void addBrightEntry(LineChart lineChart, float hrValue) {

        mBrightValue.setText(String.valueOf(hrValue));

        LineData data = lineChart.getData();

        if (data != null) {

            ILineDataSet set = data.getDataSetByIndex(0);

            if (set == null) {
//                set = createSet(60f);
//                data.addDataSet(set);
                set = createSet(3000f);
                data.addDataSet(set);
            }

            data.addEntry(new Entry(set.getEntryCount(), hrValue), 0);

            data.notifyDataChanged();

            // let the chart know it's data has changed
            lineChart.notifyDataSetChanged();

            // limit the number of visible entries
            lineChart.setVisibleXRangeMaximum(60);
            // mChart.setVisibleYRange(30, AxisDependency.LEFT);

            // move to the latest entry
            lineChart.moveViewToX(data.getEntryCount());
//            mChart.invalidate();

        }
    }

    private LineDataSet createSet(float initValue) {

        ArrayList<Entry> list = new ArrayList<>();
        list.add(new Entry(0f, initValue));
        LineDataSet set = new LineDataSet(list, "Data-1");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(ColorTemplate.getHoloBlue());
        set.setCircleColor(Color.WHITE);//设置节点的颜色
        set.setDrawCircles(false);
        set.setLineWidth(2f);
        set.setCircleRadius(4f);
        set.setFillAlpha(65);
        set.setMode(LineDataSet.Mode.LINEAR);
        set.setFillColor(ColorTemplate.getHoloBlue());
//        set.setHighLightColor(Color.rgb(244, 117, 117));
        set.setValueTextColor(Color.WHITE);
        set.setValueTextSize(9f);
        set.setDrawValues(false);
        return set;
    }
}
