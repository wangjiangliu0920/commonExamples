package com.icecold.sleepbandtest.ui;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.icecold.sleepbandtest.R;
import com.icecold.sleepbandtest.adapter.CityAdapter;
import com.icecold.sleepbandtest.common.CustomPopWindow;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class JetLagActivity extends AppCompatActivity {
    @BindView(R.id.start_city_use_description)
    RecyclerView mStartCityUseDescription;
    @BindView(R.id.end_city_use_description)
    RecyclerView mEndCityUseDescription;
    @BindView(R.id.start_city_tv)
    TextView startCityTv;
    @BindView(R.id.start_city_iv)
    ImageView startCityIv;
    @BindView(R.id.start_city)
    RelativeLayout startCity;
    @BindView(R.id.arrival_city_tv)
    TextView arrivalCityTv;
    @BindView(R.id.arrival_city_iv)
    ImageView arrivalCityIv;
    @BindView(R.id.arrival_city)
    RelativeLayout arrivalCity;

    private CityAdapter mCityAdapter;
    private ArrayList<String> cityItem;
    private ArrayList<String> lastCity;

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
        mStartCityUseDescription.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mStartCityUseDescription.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

    }

    private void initData() {

    }

    @OnClick({R.id.start_city, R.id.arrival_city})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.start_city:
                View contentView = LayoutInflater.from(this).inflate(R.layout.activity_select_city, null);
                //处理popWindow的显示内容
                handleLogic(contentView);
                //创建并且显示popWindow
                CustomPopWindow customPopWindow = new CustomPopWindow.PopupWindowBuilder(this)
                        .setView(contentView)
                        .enableOutsideTouchableDissmiss(true)
                        .size(startCity.getWidth(), ViewGroup.LayoutParams.WRAP_CONTENT)
                        .setOnDissmissListener(new PopupWindow.OnDismissListener() {
                            @Override
                            public void onDismiss() {

                            }
                        })
                        .create()
                        .showAsDropDown(startCity, 0, 4);
                break;
            case R.id.arrival_city:
                break;
        }
    }
    private void handleLogic(View contentView) {
        initDefaultCityData();
        RecyclerView cityRecyclerView = contentView.findViewById(R.id.recycler_view_city);
        EditText searchCityText = contentView.findViewById(R.id.searchCity);

        //设置recyclerView相关的
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this) {
            @Override
            public void onMeasure(RecyclerView.Recycler recycler, RecyclerView.State state, int widthSpec, int heightSpec) {
//                View view = recycler.getViewForPosition(0);
//                if (view != null) {
//                    measureChild(view,widthSpec,heightSpec);
//                    int measureWidth = View.MeasureSpec.getSize(widthSpec);
//                    int measuredHeight = view.getMeasuredHeight();
//                    int showHeight = measuredHeight * state.getItemCount();
//                    if (state.getItemCount() > 5){
//                        showHeight = measuredHeight * 5;
//                    }
//                    recycler.recycleView(view);
//                    setMeasuredDimension(measureWidth,showHeight);
//                }
                try {
                    if (mCityAdapter != null && mCityAdapter.getItemHeight() > 0){
                        int measureWidth = View.MeasureSpec.getSize(widthSpec);
                        int itemHeight = mCityAdapter.getItemHeight();
                        int showHeight = itemHeight * state.getItemCount();
                        if (state.getItemCount() > 3){
                            showHeight = itemHeight * 3;
                        }
                        setMeasuredDimension(measureWidth,showHeight);
                    }else {
                        super.onMeasure(recycler, state, widthSpec, heightSpec);
                    }
                }catch (Exception e){
                    super.onMeasure(recycler, state, widthSpec, heightSpec);
                }
            }

            @Override
            public boolean isAutoMeasureEnabled() {
                return false;
            }
        };
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        layoutManager.setAutoMeasureEnabled(true);
        cityRecyclerView.setHasFixedSize(false);
        cityRecyclerView.setLayoutManager(linearLayoutManager);
        cityRecyclerView.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));
        mCityAdapter = new CityAdapter(R.layout.content_city, cityItem);
        cityRecyclerView.setAdapter(mCityAdapter);

        //设置searchEditText相关的
        searchCityText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (TextUtils.isEmpty(editable.toString())) {
                    updateData(cityItem);
                }else {
                    lastCity.clear();
                    for (String city : cityItem) {
                        if (city.contains(editable.toString().trim())) {
                            lastCity.add(city);
                        }
                    }
                    updateData(lastCity);
                }
            }
        });
    }

    private void initDefaultCityData() {
        cityItem = new ArrayList<>();
        lastCity = new ArrayList<>();
        cityItem.add("Mumbai");
        cityItem.add("Delhi");
        cityItem.add("Bengaluru");
        cityItem.add("Hyderabad");
        cityItem.add("Ahmedabad");
        cityItem.add("Chennai");
        cityItem.add("Kolkata");
        cityItem.add("Surat");
        cityItem.add("Pune");
        cityItem.add("Jaipur");
        cityItem.add("Lucknow");
        cityItem.add("Kanpur");
    }
    private void updateData(List<String> mData) {
        mCityAdapter.setNewData(mData);
    }
}
