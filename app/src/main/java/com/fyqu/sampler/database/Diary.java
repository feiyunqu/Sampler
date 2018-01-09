package com.fyqu.sampler.database;

/**
 * Created by a on 2017/4/13.
 */

public class Diary {
    //login_activity
    public static final String login_start="开机进入登录界面";
    public static String login_userPower(String string1,String string2){return "登录用户名为/"+string1+"/,权限等级为/"+string2+"/";}
    //main_activity
    public static final String main_start="进入主界面";
    public static String main_selectList(String string1,String string2){return "选择试剂名称为/"+string1+"/,当前库存量为"+string2;}
    public static final String dataSearch="点击/数据检索/按钮";
    public static final String diaryScan="点击/浏览日志/按钮";
    public static final String powerManage="点击/权限管理/按钮";
    public static final String loginExit="点击/退出登录/按钮";
    public static final String main_run="点击/运行/按钮";
    public static final String main_cancel="点击/取消/按钮";
    public static String main_quYangLiang(String string){return "取样量设置为/"+string+"/";}
    //权限管理
    public static final String power_start="进入/权限管理/界面";
    public static String power_kuCun(String string1,String string2){return "/"+string1+"/的库存设置为"+string2;}
    public static final String power_update="点击/更新库存/按钮";
    public static final String userPower_add="点击/注册账户/按钮";
    public static final String userPower_change="点击/修改密码/按钮";
    public static final String power_back="点击/返回/按钮";
    public static String userPower_power(String string){return "权限选择为"+string;}
    public static String userPower_name_input(String string){return "用户名输入为"+string;}
    //浏览日志
    public static final String diary_start="进入/浏览日志/界面";
    public static final String diary_back="点击/返回/按钮";
    public static final String diary_search="点击/检索/按钮";
}
