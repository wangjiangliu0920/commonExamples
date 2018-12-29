package com.icecold.sleepbandtest.utils;

import android.animation.ValueAnimator;
import android.support.annotation.NonNull;

import java.lang.reflect.Field;

/**
 *
 * Created by icecold_laptop_2 on 2018/7/26.
 */

public class ValueAnimatorUtil {
    /**
     * 如果动画被禁用，则重置动画缩放时长
     */
    public static void resetDurationScaleIfDisable() {
        if (getDurationScale() == 0)
            resetDurationScale();
    }

    /**
     * 重置动画缩放时长
     */
    public static void resetDurationScale() {
        try {
            getField().setFloat(null, 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static float getDurationScale() {
        try {
            return getField().getFloat(null);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    @NonNull
    private static Field getField() throws NoSuchFieldException {
        Field sDurationScale = ValueAnimator.class.getDeclaredField("sDurationScale");
        sDurationScale.setAccessible(true);
        return sDurationScale;
    }
}
