package com.icecold.sleepbandtest.ui;

import android.bluetooth.BluetoothGattDescriptor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcel;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
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
import com.icecold.sleepbandtest.event.CallbackDataEvent;
import com.icecold.sleepbandtest.event.NotifyDataEvent;
import com.icecold.sleepbandtest.utils.Constant;
import com.icecold.sleepbandtest.utils.GlaUtils;
import com.icecold.sleepbandtest.utils.ParcelableUtil;
import com.icecold.sleepbandtest.utils.SPUtils;
import com.icecold.sleepbandtest.utils.Utils;
import com.vise.baseble.common.PropertyType;
import com.vise.baseble.core.BluetoothGattChannel;
import com.vise.baseble.model.BluetoothLeDevice;
import com.vise.baseble.utils.HexUtil;
import com.vise.log.ViseLog;
import com.vise.xsnow.event.BusManager;
import com.vise.xsnow.event.Subscribe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class OftenActivity extends AppCompatActivity {

    @BindView(R.id.heart_rate_chart)
    LineChart heartRateChart;
    @BindView(R.id.often_hr_value_tv)
    TextView oftenHrTv;
    @BindView(R.id.breath_chart)
    LineChart breathChart;
    @BindView(R.id.often_breath_value_tv)
    TextView oftenBreathTv;
    @BindView(R.id.back_ib)
    ImageButton backIb;
    @BindView(R.id.title_tv)
    TextView titleTv;
    @BindView(R.id.not_displayLine)
    Button notDisplayLine;
    @BindView(R.id.displayLine)
    Button displayLine;
    private BluetoothLeDevice myBluetooth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_often);
        ButterKnife.bind(this);

        Parcel parcel = ParcelableUtil.unmarshall(Base64.decode(
                SPUtils.getInstance(Constant.SHARED_PREFERENCE_NAME).getString(Constant.BLUETOOTH_DEVICE_KEY, "default")
                , 0));
        myBluetooth = null;
        myBluetooth = BluetoothLeDevice.CREATOR.createFromParcel(parcel);
        ViseLog.d("取出的 parcel = " + parcel.toString());
        ViseLog.i("得到的蓝牙对象 myBluetooth = " + myBluetooth.toString());

        BusManager.getBus().register(this);

        initView();
        //发送使能时时的模式
        if (myBluetooth != null) {

            if (BluetoothDeviceManager.getInstance().isConnected(myBluetooth)) {
                //先使能通道
                BluetoothDeviceManager.getInstance().initEnableChannel(myBluetooth);
                BluetoothDeviceManager.getInstance().bindChannel(myBluetooth, PropertyType.PROPERTY_NOTIFY,
                        UUID.fromString(GlaUtils.BAND_PEGASI_SERVICE_UUID),
                        UUID.fromString(GlaUtils.BAND_PEGASI_LIVE_MODE_CHARACTERISTIC_UUID),
                        null);
                BluetoothDeviceManager.getInstance().registerNotify(myBluetooth, false);
            }
        }
        ViseLog.i("栈 id = "+getTaskId());

    }

    @OnClick({R.id.back_ib,R.id.not_displayLine,R.id.displayLine})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.back_ib:
                this.onBackPressed();
                break;
            case R.id.not_displayLine:
                invisibleBrChart();
                break;
            case R.id.displayLine:
                visibleBrChart();
                break;
        }

    }

    @Override
    protected void onDestroy() {
        BusManager.getBus().unregister(this);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        //判段是否是连接的状态
        if (myBluetooth != null && BluetoothDeviceManager.getInstance().isConnected(myBluetooth)) {
            //关闭时时模式
            byte[] offLive = new byte[]{0x4c, 0x44};
            BluetoothDeviceManager.getInstance().bindChannel(myBluetooth, PropertyType.PROPERTY_WRITE,
                    UUID.fromString(GlaUtils.BAND_PEGASI_SERVICE_UUID),
                    UUID.fromString(GlaUtils.BAND_PEGASI_LIVE_MODE_CHARACTERISTIC_UUID),
                    null);
            BluetoothDeviceManager.getInstance().write(myBluetooth, offLive);
        }
        super.onBackPressed();
    }

    @Subscribe
    public void bleCallBackEvent(CallbackDataEvent event) {
        if (event != null && event.isSuccess()) {
            BluetoothGattChannel bluetoothGattChannel = event.getBluetoothGattChannel();
            if (bluetoothGattChannel != null) {
                //使能时时模式特征成功
                if (bluetoothGattChannel.getPropertyType() == PropertyType.PROPERTY_NOTIFY &&
                        bluetoothGattChannel.getCharacteristicUUID().
                                compareTo(UUID.fromString(GlaUtils.BAND_PEGASI_LIVE_MODE_CHARACTERISTIC_UUID)) == 0) {
                    if (Arrays.equals(event.getData(), BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)) {
                        //发送时时模式
                        byte[] liveMode = new byte[]{0x4c, 0x45};
                        if (myBluetooth != null) {
                            BluetoothDeviceManager.getInstance().bindChannel(myBluetooth, PropertyType.PROPERTY_WRITE,
                                    UUID.fromString(GlaUtils.BAND_PEGASI_SERVICE_UUID),
                                    UUID.fromString(GlaUtils.BAND_PEGASI_LIVE_MODE_CHARACTERISTIC_UUID),
                                    null);
                            BluetoothDeviceManager.getInstance().write(myBluetooth, liveMode);
                        }
                    }
                }
            }
        }
    }

    @Subscribe
    public void bleNotifyCallBackEvent(NotifyDataEvent event) {
        if (event != null && event.getBluetoothGattChannel() != null) {
            byte[] data = event.getData();
            if (event.getBluetoothGattChannel().getCharacteristicUUID()
                    .compareTo(UUID.fromString(GlaUtils.BAND_PEGASI_LIVE_MODE_CHARACTERISTIC_UUID)) == 0) {
                if (data[1] == 0x01) {
                    boolean isBed = (data[2] == 0x01);
                    byte[] bedTime = HexUtil.subBytes(data, 3, 2);
                    byte[] reverseTime = HexUtil.reverse(bedTime);
                    int inBedTime = Utils.getInstance().byteToInt(reverseTime);
                    int hrValue = data[5] & 0xff;
                    int brValue = data[6] & 0xff;
                    int hrAdcValue = data[7] & 0xff;
                    int brAdcValue = data[8] & 0xff;
                    boolean flagHrPk = (data[9] == 0x01);
                    boolean flagBrPk = (data[10] == 0x01);
                    ViseLog.i("在床时间 是否在床 isBed = " + isBed);
                    ViseLog.i("在床时间 bedTime = " + inBedTime);
                    ViseLog.i("hrValue = " + hrValue + ",brValue = " + brValue);
                    oftenHrTv.setText(String.valueOf(hrValue));
                    oftenBreathTv.setText(String.valueOf(brValue));
                    addHrEntry(heartRateChart, hrAdcValue, brAdcValue);
                    addBreathEntry(breathChart, brAdcValue);
                }
            }
        }
    }

    private void invisibleBrChart() {
        if (heartRateChart.getData() != null
                && heartRateChart.getData().getDataSetByIndex(1) != null) {

            heartRateChart.getData().getDataSetByIndex(0).setVisible(false);
            ILineDataSet brSet = heartRateChart.getData().getDataSetByIndex(1);
            brSet.setVisible(false);
//            heartRateChart.getData().removeDataSet(1);

            heartRateChart.getData().notifyDataChanged();
            heartRateChart.notifyDataSetChanged();
        }
    }

    private void visibleBrChart() {
        if (heartRateChart.getData() != null
                && heartRateChart.getData().getDataSetByIndex(1) != null) {
            heartRateChart.getData().getDataSetByIndex(0).setVisible(true);
            ILineDataSet brSet = heartRateChart.getData().getDataSetByIndex(1);
            brSet.setVisible(true);
//            heartRateChart.getData().removeDataSet(1);

            heartRateChart.getData().notifyDataChanged();
            heartRateChart.notifyDataSetChanged();
        }
    }

    private void initView() {
        titleTv.setText("综合图形");
        //心率图初始化
        gestureSetWithLineChart(heartRateChart);
        setXYAxis(heartRateChart, 0, 100);

        LineData rataData = new LineData();
        // add empty data
        heartRateChart.setData(rataData);

        //呼吸图初始化
        gestureSetWithLineChart(breathChart);
        setXYAxis(breathChart, 0, 60);
        LineData brData = new LineData();
        // add empty data
        breathChart.setData(brData);
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
    private LineDataSet createBrSet(float initValue) {
        ArrayList<Entry> list2 = new ArrayList<>();
        list2.add(new Entry(0f, initValue));
        LineDataSet set2 = new LineDataSet(list2, "Data-2");
        set2.setAxisDependency(YAxis.AxisDependency.LEFT);
        set2.setColor(ColorTemplate.rgb("#C63838"));
        set2.setCircleColor(Color.WHITE);//设置节点的颜色
        set2.setDrawCircles(false);
        set2.setLineWidth(2f);
        set2.setCircleRadius(4f);
        set2.setFillAlpha(65);
        set2.setMode(LineDataSet.Mode.LINEAR);
        set2.setFillColor(ColorTemplate.getHoloBlue());
//        set2.setHighLightColor(Color.rgb(244, 117, 117));
        set2.setValueTextColor(Color.WHITE);
        set2.setValueTextSize(9f);
        set2.setDrawValues(false);
        return set2;
    }

    private LineDataSet createMultipleSet(float initValue, LineData mData) {
        ArrayList<Entry> list1 = new ArrayList<>();
        list1.add(new Entry(0f, initValue));
        LineDataSet set1 = new LineDataSet(list1, "Data-1");
        set1.setAxisDependency(YAxis.AxisDependency.LEFT);
        set1.setColor(ColorTemplate.getHoloBlue());
        set1.setCircleColor(Color.WHITE);//设置节点的颜色
        set1.setDrawCircles(false);
        set1.setLineWidth(2f);
        set1.setCircleRadius(4f);
        set1.setFillAlpha(65);
        set1.setMode(LineDataSet.Mode.LINEAR);
        set1.setFillColor(ColorTemplate.getHoloBlue());
//        set1.setHighLightColor(Color.rgb(244, 117, 117));
        set1.setValueTextColor(Color.WHITE);
        set1.setValueTextSize(9f);
        set1.setDrawValues(false);

        //实验验证用于区分set1和set2与list1和list2有很大的关系，
        // 如果list1与list2指向的是一个地址，则set1与set2的值会变成一致
        ArrayList<Entry> list2 = new ArrayList<>();
        list1.add(new Entry(0f, initValue));
        LineDataSet set2 = new LineDataSet(list2, "Data-2");
        set2.setAxisDependency(YAxis.AxisDependency.LEFT);
        set2.setColor(ColorTemplate.rgb("#C63838"));
        set2.setCircleColor(Color.WHITE);//设置节点的颜色
        set2.setDrawCircles(false);
        set2.setLineWidth(2f);
        set2.setCircleRadius(4f);
        set2.setFillAlpha(65);
        set2.setMode(LineDataSet.Mode.LINEAR);
        set2.setFillColor(ColorTemplate.getHoloBlue());
//        set2.setHighLightColor(Color.rgb(244, 117, 117));
        set2.setValueTextColor(Color.WHITE);
        set2.setValueTextSize(9f);
        set2.setDrawValues(false);
        mData.addDataSet(set1);
        mData.addDataSet(set2);
        return set1;
    }

    private void addHrEntry(LineChart lineChart, float hrValue, float brValue) {

        LineData data = lineChart.getData();

        if (data != null) {

            ILineDataSet set = data.getDataSetByIndex(0);

            if (set == null) {
//                set = createSet(60f);
//                data.addDataSet(set);
                set = createSet(60f);
                data.addDataSet(set);
            }

            data.addEntry(new Entry(set.getEntryCount(), hrValue), 0);

            data.notifyDataChanged();

            // let the chart know it's data has changed
            lineChart.notifyDataSetChanged();

            // limit the number of visible entries
            lineChart.setVisibleXRangeMaximum(40);
            // mChart.setVisibleYRange(30, AxisDependency.LEFT);

            // move to the latest entry
            lineChart.moveViewToX(data.getEntryCount());
//            mChart.invalidate();

        }
    }

    private void addBreathEntry(LineChart lineChart, float breathValue) {

        LineData data = lineChart.getData();

        if (data != null) {

            ILineDataSet set = data.getDataSetByIndex(0);
            // set.addEntry(...); // can be called as well

            if (set == null) {
                set = createBrSet(20f);
                data.addDataSet(set);
            }

            data.addEntry(new Entry(set.getEntryCount(), breathValue), 0);
            data.notifyDataChanged();

            // let the chart know it's data has changed
            lineChart.notifyDataSetChanged();

            // limit the number of visible entries
            lineChart.setVisibleXRangeMaximum(40);
            // mChart.setVisibleYRange(30, AxisDependency.LEFT);

            // move to the latest entry
            lineChart.moveViewToX(data.getEntryCount());

        }
    }
}
