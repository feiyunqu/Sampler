package com.dev.temp;

/**
 * Created by a on 2017/10/23.
 *
 */

public class dev {

    public native int Open();

    public native int Get();

    public native int Get2();

    public native int Close();

    public native int Adj();

    //添加C/C++动态库导入方法
    static {
        System.loadLibrary("temp_dev");
    }

}
