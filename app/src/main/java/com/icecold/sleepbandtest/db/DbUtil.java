package com.icecold.sleepbandtest.db;

import com.icecold.sleepbandtest.dao.StudentDao;

/**
 * @Description: 获取各张表的操作类
 * @author: icecold_laptop_2
 * @date: 2019/2/15
 */
public class DbUtil {

    private static StudentHelper studentHelper;

    public static StudentHelper getStudentHelper(){
        if (studentHelper == null) {
            studentHelper = new StudentHelper(getStudentDao());
        }
        return studentHelper;
    }

    private static StudentDao getStudentDao() {
        return DbCore.getDaoSession().getStudentDao();
    }
}
