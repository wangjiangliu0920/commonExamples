package com.icecold.sleepbandtest.widget;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.icecold.sleepbandtest.R;

/**
 * @Description: 不确定型的对话框
 * @author: icecold_laptop_2
 * @date: 2018/10/25
 */

public class UncertainDialog extends DialogFragment {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_TITLE,R.style.CustomDialog);
    }

    @Nullable
    @Override
    public View onCreateView(@Nullable LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.dialog_uncertain, container, false);
        getDialog().setCanceledOnTouchOutside(false);
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() == null) {
            return;
        }
        int dialogWidth = getResources().getDimensionPixelSize(R.dimen.dp_105);
        int dialogHeight = getResources().getDimensionPixelSize(R.dimen.dp_105);
        if (getDialog().getWindow() == null) {
            return;
        }
        getDialog().getWindow().setLayout(dialogWidth,dialogHeight);
    }

    public static UncertainDialog getInstance(){
        UncertainDialog uncertainDialog = new UncertainDialog();
        Bundle args = new Bundle();
        uncertainDialog.setArguments(args);
        return uncertainDialog;
    }
}
