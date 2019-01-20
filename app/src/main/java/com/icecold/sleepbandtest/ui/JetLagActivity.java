package com.icecold.sleepbandtest.ui;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.icecold.sleepbandtest.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class JetLagActivity extends AppCompatActivity {
    @BindView(R.id.start_city_use_description)
    RecyclerView mStartCityUseDescription;
    @BindView(R.id.end_city_use_description)
    RecyclerView mEndCityUseDescription;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        setContentView(R.layout.activity_jet_lag_calculator);
        ButterKnife.bind(this);
        initView();
        initData();
    }

    private void initView() {
        //设置recycleView相关的
        mStartCityUseDescription.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));
        mStartCityUseDescription.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));

    }

    private void initData() {

    }
}
