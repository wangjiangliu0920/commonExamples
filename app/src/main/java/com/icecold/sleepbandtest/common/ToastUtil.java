package com.icecold.sleepbandtest.common;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.dovar.dtoast.DToast;
import com.dovar.dtoast.inner.IToast;
import com.icecold.sleepbandtest.R;

/**
 * @Description: 吐司框的工具类
 * @author: icecold_laptop_2
 * @date: 2018/12/29
 */

public class ToastUtil {
    private static Toast mToast;

    public static void makeText(Context context,String text,int time){
        //这样实现是为了多次调用退出应用还会显示toast的问题
        if (mToast == null) {
            mToast = Toast.makeText(context,text,time);
        }else {
            mToast.setText(text);
            mToast.setDuration(time);
        }
        mToast.show();
    }

    public static void show(Context mContext, String msg) {
        if (mContext == null) return;
        if (msg == null) return;
        IToast toast= DToast.make(mContext);
        TextView tv_text = (TextView) toast.getView().findViewById(R.id.tv_content);
        if (tv_text != null) {
            tv_text.setText(msg);
        }
        toast.setGravity(Gravity.BOTTOM|Gravity.CENTER,0,30).show();
    }


    public static void showAtCenter(Context mContext, String msg) {
        if (mContext == null) return;
        if (msg == null) return;
        View toastRoot = View.inflate(mContext, R.layout.layout_toast_center, null);
        TextView tv_text = (TextView) toastRoot.findViewById(R.id.tv_content);
        if (tv_text != null) {
            tv_text.setText(msg);
        }
        DToast.make(mContext)
                .setView(toastRoot)
                .setGravity(Gravity.CENTER, 0, 0)
                .show();
    }

    //退出APP时调用
    public static void cancelAll() {
        DToast.cancel();
    }
}
