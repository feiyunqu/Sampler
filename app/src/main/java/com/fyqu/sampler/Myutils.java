package com.fyqu.sampler;

import android.database.Cursor;

import com.fyqu.sampler.database.MyDatabaseHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by a on 2017/2/20.
 */

public class Myutils {
    public static String formatDate(long time) {
        String part = "yyyy-MM-dd";
        SimpleDateFormat formatter = new SimpleDateFormat(part, Locale.getDefault());
        return formatter.format(new Date(time));
    }

    public static String formatDateTime(long time) {
        String part = "yyyy-MM-dd HH:mm:ss";
        SimpleDateFormat formatter = new SimpleDateFormat(part, Locale.getDefault());
        return formatter.format(new Date(time));
    }

    private static String powername = "";
    private static String username = "";
    private static int tempUp = 500;
    private static int tempDown = 200;
    private static long fanTime=60000;
    private static long fanLast=30000;

    public static long getFanLast() {return fanLast;}

    public static void setFanLast(long fanLast) {Myutils.fanLast = fanLast;}

    public static long getFanTime() {
        return fanTime;
    }

    public static void setFanTime(long fanTime) {Myutils.fanTime = fanTime;}

    public static void setPowername(String powername) {
        Myutils.powername = powername;
    }

    public static String getPowername() {
        return powername;
    }

    public static void setUsername(String username) {
        Myutils.username = username;
    }

    public static String getUsername() {
        return username;
    }

    public static int getTempUp() {
        return tempUp;
    }

    public static void setTempUp(int tempUp) {
        Myutils.tempUp = tempUp;
    }

    public static int getTempDown() {
        return tempDown;
    }

    public static void setTempDown(int tempDown) {
        Myutils.tempDown = tempDown;
    }

    private static String strAbc = "";
    private static String strEna = "";
    private static String strOff = "";
    private static String strCur = "";
    private static String strAcr = "";
    private static String strMcs = "";
    private static String strSpd = "";
    private static String strStp = "";
    private static String strDf = "";
    private static String strDf1 = "";
    private static String strDf2 = "";
    private static String strDf3 = "";
    private static String strDf4 = "";

    public static String getStrAbc() {return strAbc;}
    public static void setStrAbc(String strAbc) {Myutils.strAbc = strAbc;}

    public static String getStrEna() {return strEna;}
    public static void setStrEna(String strEna) {Myutils.strEna = strEna;}

    public static String getStrOff() {return strOff;}
    public static void setStrOff(String strOff) {Myutils.strOff = strOff;}

    public static String getStrCur() {return strCur;}
    public static void setStrCur(String strCur) {Myutils.strCur = strCur;}

    public static String getStrAcr() {return strAcr;}
    public static void setStrAcr(String strAcr) {Myutils.strAcr = strAcr;}

    public static String getStrMcs() {return strMcs;}
    public static void setStrMcs(String strMcs) {Myutils.strMcs = strMcs;}

    public static String getStrSpd() {return strSpd;}
    public static void setStrSpd(String strSpd) {Myutils.strSpd = strSpd;}

    public static String getStrStp() {return strStp;}
    public static void setStrStp(String strStp) {Myutils.strStp = strStp;}

    public static String getStrDf() {return strDf;}
    public static void setStrDf(String strDf) {Myutils.strDf = strDf;}

    public static String getStrDf1() {return strDf1;}
    public static void setStrDf1(String strDf1) {Myutils.strDf1 = strDf1;}

    public static String getStrDf2() {return strDf2;}
    public static void setStrDf2(String strDf2) {Myutils.strDf2 = strDf2;}

    public static String getStrDf3() {return strDf3;}
    public static void setStrDf3(String strDf3) {Myutils.strDf3 = strDf3;}

    public static String getStrDf4() {return strDf4;}
    public static void setStrDf4(String strDf4) {Myutils.strDf4 = strDf4;}
}
