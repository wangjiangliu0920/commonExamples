package com.icecold.sleepbandtest.db;

import com.icecold.sleepbandtest.dao.Student;

import org.greenrobot.greendao.AbstractDao;

/**
 * @Description: 具体表的实现类,一个表对应一个类
 * @author: icecold_laptop_2
 * @date: 2019/2/15
 */
public class StudentHelper extends BaseDbHelper<Student,Long> {

    public StudentHelper(AbstractDao mDao) {
        super(mDao);
    }
}
