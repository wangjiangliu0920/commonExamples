package com.icecold.sleepbandtest.ui;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.icecold.sleepbandtest.R;
import com.icecold.sleepbandtest.adapter.CityAdapter;
import com.icecold.sleepbandtest.adapter.SyLinearLayoutManager;
import com.icecold.sleepbandtest.adapter.decoration.MovieDecoration;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.vise.log.ViseLog;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class SelectCityActivity extends AppCompatActivity {

    @BindView(R.id.searchCity)
    EditText searchCityText;
    @BindView(R.id.recycler_view_city)
    RecyclerView cityRecyclerView;
    @BindView(R.id.calendarView)
    MaterialCalendarView mCalendarView;
    @BindView(R.id.select_calendar)
    EditText mSelectCalendar;
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

    @Override
    protected void onResume() {
        super.onResume();
        Disposable disposable = Observable.timer(200, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        searchCityText.setFocusable(true);
                        searchCityText.setFocusableInTouchMode(true);
                    }
                });
//        setEditTextShowSoftInput(searchCityText,true);
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
                    if (cityAdapter != null && cityAdapter.getItemHeight() > 0) {
                        int measureWidth = View.MeasureSpec.getSize(widthSpec);
                        int itemHeight = cityAdapter.getItemHeight();
                        int showHeight = itemHeight * state.getItemCount();
                        if (state.getItemCount() > 2) {
                            showHeight = itemHeight * 2;
                        }
                        setMeasuredDimension(measureWidth, showHeight);
                    } else {
                        super.onMeasure(recycler, state, widthSpec, heightSpec);
                    }
                } catch (Exception e) {
                    super.onMeasure(recycler, state, widthSpec, heightSpec);
                }
            }
        };
        linearLayoutManager.setAutoMeasureEnabled(false);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        layoutManager.setAutoMeasureEnabled(true);
        SyLinearLayoutManager syLinearLayoutManager = new SyLinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        syLinearLayoutManager.setAutoMeasureEnabled(false);
        //设置recyclerView相关的
        cityRecyclerView.setLayoutManager(linearLayoutManager);
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
        //editText相关的
//        setEditTextShowSoftInput(searchCityText,false);
        searchCityText.setFocusable(false);
        searchCityText.setFocusableInTouchMode(false);
        searchCityText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                ViseLog.d("beforeTextChanged 字符 = " + s + " 开始 = " + start + "个数 = " + count + " 之后 = " + after);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                ViseLog.d("onTextChanged 字符 = " + s + " 开始 = " + start + " 之前 = " + before + "个数 = " + count);
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
                ViseLog.d("afterTextChanged = " + editable.toString());
            }
        });
        //日历相关的内容
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        mCalendarView.setDateSelected(calendar, true);
        mCalendarView.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
                ViseLog.d("日期 : " + date.getYear() + "-" + date.getMonth() + 1 + "-" + date.getDay());
                ViseLog.d("控件显示的日期 = " + widget.getSelectedDate().toString() + " 日期 = " + date.toString() + " 选择 = " + selected);
            }
        });
//        setEditTextShowSoftInput(mSelectCalendar,false);
        mSelectCalendar.setFocusable(false);
        mSelectCalendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ViseLog.d("获取到的文字 = "+mSelectCalendar.getText().toString());
                Toast.makeText(SelectCityActivity.this,"填入日期",Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateData(List<String> mData) {
        cityAdapter.setNewData(mData);
    }

    public static void setEditTextShowSoftInput(EditText editText,boolean show) {

        Class editClass = editText.getClass().getSuperclass();
        Class textClass = editClass.getSuperclass();
        try {
            Field editorField = textClass.getDeclaredField("mEditor");
            editorField.setAccessible(true);
            Object editorObject = editorField.get(editText);
            Class editorClass = editorObject.getClass();
            if (!"Editor".equals(editorClass.getSimpleName())) {
                editorClass = editorClass.getSuperclass(); // 防止类似于华为使用的自身的HwEditor
            }
            Field mShowInput = editorClass.getDeclaredField("mShowSoftInputOnFocus");
            mShowInput.setAccessible(true);
            mShowInput.set(editorObject, show);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

}
