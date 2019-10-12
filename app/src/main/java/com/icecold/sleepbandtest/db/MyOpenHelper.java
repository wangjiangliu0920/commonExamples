package com.icecold.sleepbandtest.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.icecold.sleepbandtest.dao.DaoMaster;
import com.icecold.sleepbandtest.dao.StudentDao;

import org.greenrobot.greendao.database.Database;

/**
 * @Description: 数据库创建于升级的集体管理
 * @author: icecold_laptop_2
 * @date: 2019/2/15
 */
public class MyOpenHelper extends DaoMaster.OpenHelper {
    public MyOpenHelper(Context context, String name) {
        super(context, name);
    }

    public MyOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory) {
        super(context, name, factory);
    }

    @Override
    public void onUpgrade(Database db, int oldVersion, int newVersion) {
        //自己处理数据库升级的逻辑
        switch (oldVersion) {
            case 1:

                //不能先删除表，否则数据都木了
//                StudentDao.dropTable(db, true);

                StudentDao.createTable(db, true);

                // 加入新字段 score
                db.execSQL("ALTER TABLE 'STUDENT' ADD 'SCORE' TEXT;");

                break;
        }
    }
}
