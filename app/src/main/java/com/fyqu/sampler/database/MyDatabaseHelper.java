package com.fyqu.sampler.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by a on 2017/8/2.
 *
 */

public class MyDatabaseHelper extends SQLiteOpenHelper {
    private static final String SQLITE_DB_NAME = "sampler99.db";

    public MyDatabaseHelper(Context context) {
        super(context, SQLITE_DB_NAME, null, 1);
    }

    private static final String SQLITE_DB_ITEMS = "create table Item(" +
            "_id integer primary key autoincrement," +
            "item varchar(50)," +//试剂
            "origin varchar(50))";//库存量
    private static final String SQLITE_DB_DATA = "create table Data(" +
            "_id integer primary key autoincrement," +
            "user varchar(50)," +//用户
            "power varchar(50)," +//权限
            "item varchar(50)," +//试剂
            "origin varchar(50)," +//库存量
            "taking varchar(50)," +//取样量
            "left varchar(50),"+//剩余量
            "datetime varchar(50),"+//操作时间
            "complete varchar(50))";//是否完成
    private static final String SQLITE_DB_USER = "create table User(" +
            "_id integer primary key autoincrement," +
            "userPower int," +//用户权限？
            "registerTime long," +//注册日期or信息更新日期
            "name varchar(50)," +//权限名称
            "userName varchar(100) not null unique," +//用户名
            "password varchar(100))";//登录密码
    private static final String SQLITE_DB_DIARY = "create table Diary(" +
            "_id integer primary key autoincrement," +
            "dateTime long," +//时间日期
            "context varchar(100)," +//操作内容
            "powerName varchar(50)," +//权限名称
            "userName varchar(100))"; //用户名称
    @Override
    public void onCreate(SQLiteDatabase db) {
        //第一次使用数据库时自动建表
        db.execSQL(SQLITE_DB_ITEMS);
        db.execSQL(SQLITE_DB_DATA);
        db.execSQL(SQLITE_DB_USER);
        db.execSQL(SQLITE_DB_DIARY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        System.out.println("--------onUpgrade Called--------" + oldVersion + "--->" + newVersion);
    }
}
