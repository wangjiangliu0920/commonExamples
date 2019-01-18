package com.icecold.sleepbandtest.adapter;

import android.support.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.icecold.sleepbandtest.R;

import java.util.List;

/**
 * @Description: 城市相关的adapter
 * @author: icecold_laptop_2
 * @date: 2019/1/17
 */

public class CityAdapter extends BaseQuickAdapter<String,BaseViewHolder> {

    public CityAdapter(int layoutResId, @Nullable List<String> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, String item) {
        helper.setText(R.id.city_content,item);
        helper.addOnClickListener(R.id.city_content);
    }
}
