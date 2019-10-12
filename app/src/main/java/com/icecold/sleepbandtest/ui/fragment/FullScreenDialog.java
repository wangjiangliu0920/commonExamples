package com.icecold.sleepbandtest.ui.fragment;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.icecold.sleepbandtest.R;
import com.icecold.sleepbandtest.utils.RxTimer;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * @Description: com.icecold.peg.lite.mvp.ui.fragment
 * @author: icecold_laptop_2
 * @date: 2019/5/14
 */
public class FullScreenDialog extends DialogFragment implements RxTimer.RxAction {

    @BindView(R.id.transparent_dialog_second_tv)
    TextView mRemainTime;
    private Unbinder mUnbinder;
    private RxTimer mTimer;
    private static int INIT_TIME = 39;
    private int countDownTime;

    public static FullScreenDialog newInstance() {
        FullScreenDialog fragment = new FullScreenDialog();
        return fragment;
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_dialog,container,false);
        mUnbinder = ButterKnife.bind(this, rootView);
        initView();
        initData();
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
//            getDialog().getWindow().getDecorView().setFitsSystemWindows(true);
            getDialog().getWindow().getDecorView().setSystemUiVisibility
                    (View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        }
        super.onActivityCreated(savedInstanceState);
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(0x00000000));
            getDialog().getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        }

    }

    @Override
    public void onDestroyView() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        if (mUnbinder != null && mUnbinder != Unbinder.EMPTY) {
            try {
                mUnbinder.unbind();
            } catch (IllegalStateException e) {
                e.printStackTrace();
                //fix Bindings already cleared
            }
        }
        super.onDestroyView();
    }

    private void initData() {
        countDownTime = INIT_TIME;
        //开启定时器
        if (mTimer == null) {
            mTimer = new RxTimer();
        }
        mTimer.interval(100,this);
    }

    private void initView() {
        mRemainTime.setText(String.valueOf(INIT_TIME / 10));
    }

    @Override
    public void action(long number) {
        countDownTime = (int) (countDownTime - 1);
//        Logger.d("计时时间 countDownTime = "+countDownTime);
        if (countDownTime < 5){
            //解决先退出这个dialog引起内存泄漏的问题
            if (mTimer != null) {
                mTimer.cancel();
                mTimer = null;
            }
//            Logger.d("计时时间到达了");
            dismissAllowingStateLoss();
        }else {
            mRemainTime.setText(String.valueOf((int) countDownTime / 10));
        }
    }
}
