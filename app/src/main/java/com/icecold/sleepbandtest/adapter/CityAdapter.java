package com.icecold.sleepbandtest.adapter;

import android.support.annotation.Nullable;
import android.view.ViewTreeObserver;

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
    private int itemHeight;

    public CityAdapter(int layoutResId, @Nullable List<String> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(final BaseViewHolder helper, String item) {
        helper.itemView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                itemHeight = helper.itemView.findViewById(R.id.city_content).getMeasuredHeight();
                return true;
            }
        });
        helper.setText(R.id.city_content,item);
        helper.addOnClickListener(R.id.city_content);
    }

    public int getItemHeight() {
        return itemHeight;
    }
}
