package com.icecold.sleepbandtest.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

import com.vise.log.ViseLog;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @Description: 监听ScrollView停止滚动
 * @author: MrWang
 * @date: 2019/8/9
 */
public class MyScrollView extends ScrollView {
    public MyScrollView(Context context) {
        super(context);
    }

    public MyScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        ViseLog.i("scrollView停止滑动吗 = " + isFinishScroll());
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return super.onTouchEvent(ev);
    }

    public boolean isFinishScroll(){

        boolean isFinish = false;
        Class<ScrollView> scrollView = ScrollView.class;
        try {
            Field scrollFiled = scrollView.getDeclaredField("mScroller");
            scrollFiled.setAccessible(true);
            Object scroller = scrollFiled.get(this);
            Class<?> overscroller = scrollFiled.getType();
            Method finishField = overscroller.getMethod("isFinished");
            finishField.setAccessible(true);
            isFinish = (boolean) finishField.invoke(scroller);
            ViseLog.i("真的拿到状态值了");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return isFinish;
    }
}
