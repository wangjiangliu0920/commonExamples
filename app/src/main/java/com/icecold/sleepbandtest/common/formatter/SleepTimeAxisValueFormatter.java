package com.icecold.sleepbandtest.common.formatter;

import android.text.format.DateFormat;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.vise.log.ViseLog;

/**
 *
 */

public class SleepTimeAxisValueFormatter implements IAxisValueFormatter {

    private long startSleepTime = 0;

    public SleepTimeAxisValueFormatter(long beginTime) {
        this.startSleepTime = beginTime;
    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        long labelTime = startSleepTime +  (long) value;
        ViseLog.i("进入自定义的时间格式的 labelTime = " + labelTime);
        if (value <= axis.getAxisMinimum() || value >= axis.getAxisMaximum()) {
            return (String) DateFormat.format("HH:mm", labelTime * 1000);
        }

//        int minutes = Integer.parseInt((String) DateFormat.format("mm", labelTime * 1000));
//        if (minutes > 30) {
//            labelTime = labelTime + 30 * 60;
//        }
        return (String) DateFormat.format("HH:mm", labelTime * 1000);
    }
}
