package com.icecold.sleepbandtest;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.icecold.sleepbandtest.adapter.CityAdapter;
import com.icecold.sleepbandtest.adapter.SyLinearLayoutManager;
import com.icecold.sleepbandtest.adapter.decoration.MovieDecoration;
import com.vise.log.ViseLog;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SelectCityActivity extends AppCompatActivity {

    @BindView(R.id.searchCity)
    EditText searchCityText;
    @BindView(R.id.recycler_view_city)
    RecyclerView cityRecyclerView;
    private CityAdapter cityAdapter;
    private ArrayList<String> cityItem;
    private ArrayList<String> lastCity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_city);
        ButterKnife.bind(this);

        initViewAndListener();
        initData();
        updateData(cityItem);

    }

    private void initData() {
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

    private void initViewAndListener() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this) {
            @Override
            public void onMeasure(RecyclerView.Recycler recycler, RecyclerView.State state, int widthSpec, int heightSpec) {
                View view = recycler.getViewForPosition(0);
                if (view != null) {
                    measureChild(view,widthSpec,heightSpec);
                    int measureWidth = View.MeasureSpec.getSize(widthSpec);
                    int measuredHeight = view.getMeasuredHeight();
                    int showHeight = measuredHeight * state.getItemCount();
                    if (state.getItemCount() > 5){
                        showHeight = measuredHeight * 5;
                    }
                    recycler.recycleView(view);
                    setMeasuredDimension(measureWidth,showHeight);
                }
            }
        };
        linearLayoutManager.setAutoMeasureEnabled(false);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        layoutManager.setAutoMeasureEnabled(true);
        SyLinearLayoutManager syLinearLayoutManager = new SyLinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        syLinearLayoutManager.setAutoMeasureEnabled(false);
        //设置recyclerView相关的
        cityRecyclerView.setLayoutManager(layoutManager);
        cityRecyclerView.addItemDecoration(new MovieDecoration());
        cityAdapter = new CityAdapter(R.layout.content_city, null);
        cityAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                ViseLog.d("点击事件触发内容 = ");
            }
        });
        cityRecyclerView.setHasFixedSize(false);
        cityRecyclerView.setAdapter(cityAdapter);
        searchCityText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                ViseLog.d("beforeTextChanged 字符 = "+s+" 开始 = "+ start + "个数 = " + count+" 之后 = "+after);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                ViseLog.d("onTextChanged 字符 = "+s+" 开始 = "+start+" 之前 = "+before+"个数 = "+count);
            }

            @Override
            public void afterTextChanged(Editable editable) {
                lastCity.clear();
                for (String city : cityItem) {
                    if (city.contains(editable.toString().trim())) {
                        lastCity.add(city);
                    }
                }
                updateData(lastCity);
                ViseLog.d("afterTextChanged = "+editable.toString());
            }
        });
    }

    private void updateData(List<String> mData) {
        cityAdapter.setNewData(mData);
    }
}
