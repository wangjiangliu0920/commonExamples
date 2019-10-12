package com.icecold.sleepbandtest.widget;

import java.util.Locale;

import cn.aigestudio.datepicker.bizs.languages.CN;
import cn.aigestudio.datepicker.bizs.languages.DPLManager;

/**
 * @Description: com.icecold.PEGASI.customview.theme
 * @author: icecold_laptop_2
 * @date: 2019/1/25
 */

public class DPJetLagLanguage extends DPLManager {

    private static DPLManager sLanguage;

    public DPJetLagLanguage() {
        super();
    }

    public static DPLManager getInstance() {
        if (null == sLanguage) {
            String locale = Locale.getDefault().getLanguage().toLowerCase();
            if (locale.equals("zh")) {
                sLanguage = new CN();
            } else {
                sLanguage = new CN();
            }
        }
        return sLanguage;
    }
    @Override
    public String[] titleMonth() {
        return new String[0];
    }

    @Override
    public String titleEnsure() {
        return null;
    }

    @Override
    public String titleBC() {
        return null;
    }

    @Override
    public String[] titleWeek() {
        return new String[0];
    }
}
