package com.fyqu.sampler.activity;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

import com.fyqu.sampler.Myutils;
import com.fyqu.sampler.R;
import com.fyqu.sampler.database.Diary;
import com.fyqu.sampler.database.MyDatabaseHelper;
import com.fyqu.sampler.fragment.UserFragment;
import com.fyqu.sampler.service.MyService;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class UserPower extends AppCompatActivity {
    Button btnadd;
    Button btnupdate;
    Button btnkucun;
    Button btnback;
    EditText edit_user;
    EditText edit_password;
    EditText[] editLiquid;
    EditText[] editLiquidName;
    TextClock textClock;
    TextView text_power;
    TextView text_user;
    TextView text_temp;
    EditText edit_truepassword;
    TextView textUp;
    TextView textDown;
    TextView textFan;
    Spinner spinner_power;
    MyDatabaseHelper dbHelper;
    List<String> spinnerList;
    ServiceConnection serviceConnection;
    MyService.MyBinder myBinder;
    MyService myService;
    Handler handler = new Handler();
    float exam;
    boolean windFlag;
    Cursor cursor;
    int i = 0;
    UserFragment userFragment;
    FragmentManager fragmentManager;
    FragmentTransaction transaction;
    LinearLayout linearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_power);
        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                myBinder = (MyService.MyBinder) service;
                myService = myBinder.getService();
                myBinder.threadGo();
                myBinder.press();
                myBinder.fanPress();
                myService.setValues(new MyService.CallBacks() {
                    @Override
                    public void startRead(float f, boolean flag) {
                        exam = f;
                        windFlag = flag;
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                text_temp.setText("当前温度:\n" + exam + '℃');
                                if (windFlag) {
                                    textFan.setText("风扇:\n正在工作");
                                } else {
                                    textFan.setText("风扇:\n停止工作");
                                }
                            }
                        });
                    }

                    @Override
                    public void output(FileOutputStream outputStream) {

                    }
                });
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };
        Intent bindIntent = new Intent(this, MyService.class);
        bindService(bindIntent, serviceConnection, BIND_AUTO_CREATE);

        dbHelper = new MyDatabaseHelper(this);
        diary(dbHelper, Diary.power_start);
        textClock = (TextClock) findViewById(R.id.textClock2);
        text_power = (TextView) findViewById(R.id.text_power2);
        text_user = (TextView) findViewById(R.id.text_user2);
        text_temp = (TextView) findViewById(R.id.text_temprature2);
        textUp = (TextView) findViewById(R.id.text_up2);
        textDown = (TextView) findViewById(R.id.text_down2);
        textFan = (TextView) findViewById(R.id.text_fan2);
        textClock.setFormat24Hour("yyyy-MM-dd\nHH:mm:ss");
        text_power.setText("权限:\n" + Myutils.getPowername());
        text_user.setText("用户名:\n" + Myutils.getUsername());
        textUp.setText("温度上限:\n" + (float) Myutils.getTempUp() / 10 + "℃");
        textDown.setText("温度下限:\n" + (float) Myutils.getTempDown() / 10 + "℃");
        editLiquid = new EditText[8];
        editLiquid[0] = (EditText) findViewById(R.id.edit_liquid_0);
        editLiquid[1] = (EditText) findViewById(R.id.edit_liquid_1);
        editLiquid[2] = (EditText) findViewById(R.id.edit_liquid_2);
        editLiquid[3] = (EditText) findViewById(R.id.edit_liquid_3);
        editLiquid[4] = (EditText) findViewById(R.id.edit_liquid_4);
        editLiquid[5] = (EditText) findViewById(R.id.edit_liquid_5);
        editLiquid[6] = (EditText) findViewById(R.id.edit_liquid_6);
        editLiquid[7] = (EditText) findViewById(R.id.edit_liquid_7);
        editLiquidName = new EditText[8];
        editLiquidName[0] = (EditText) findViewById(R.id.edit_name_0);
        editLiquidName[1] = (EditText) findViewById(R.id.edit_name_1);
        editLiquidName[2] = (EditText) findViewById(R.id.edit_name_2);
        editLiquidName[3] = (EditText) findViewById(R.id.edit_name_3);
        editLiquidName[4] = (EditText) findViewById(R.id.edit_name_4);
        editLiquidName[5] = (EditText) findViewById(R.id.edit_name_5);
        editLiquidName[6] = (EditText) findViewById(R.id.edit_name_6);
        editLiquidName[7] = (EditText) findViewById(R.id.edit_name_7);
        linearLayout = (LinearLayout) findViewById(R.id.userlayout);
        cursor = dbHelper.getReadableDatabase().rawQuery("select * from Item", null);
        while (cursor.moveToNext()) {
            Log.d("iddd", "onCreate: " + cursor.getString(cursor.getColumnIndex("_id")));
            editLiquidName[i].setText(cursor.getString(cursor.getColumnIndex("item")));
            editLiquid[i].setText(cursor.getString(cursor.getColumnIndex("origin")));
            i++;
        }
        btnkucun = (Button) findViewById(R.id.btn_update_kucun);
        btnkucun.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                diary(dbHelper, Diary.power_update);
                for (int n = 0; n < editLiquid.length; n++) {
                    dbHelper.getReadableDatabase().execSQL(
                            "update Item set item=?, origin=? where _id=?", new Object[]{
                                    editLiquidName[n].getText().toString(),
                                    editLiquid[n].getText().toString(),
                                    n + 1
                            });
                }
                Toast.makeText(getApplicationContext(), "更新成功", Toast.LENGTH_SHORT).show();
            }
        });

        ////////////////////////////////////////////////////////////////////
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                diary(dbHelper, Diary.userPower_name_input(s.toString()));
            }
        };
        edit_user = (EditText) findViewById(R.id.edit_user);
        edit_user.addTextChangedListener(textWatcher);
        edit_password = (EditText) findViewById(R.id.edit_password);
        edit_truepassword = (EditText) findViewById(R.id.edit_truepassword);
        spinner_power = (Spinner) findViewById(R.id.spinner_power);
        ArrayAdapter<String> spinnerAadapter = new ArrayAdapter<>(this, R.layout.spinner_text_item, getDataSource());
        spinnerAadapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        spinner_power.setAdapter(spinnerAadapter);
        spinner_power.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                diary(dbHelper, Diary.userPower_power(spinnerList.get(position)));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        btnadd = (Button) findViewById(R.id.btnadd);
        btnadd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                diary(dbHelper, Diary.userPower_add);
                if (!TextUtils.isEmpty(edit_user.getText())) {
                    Cursor cursor = dbHelper.getReadableDatabase().rawQuery(
                            "select name from User where userName=?", new String[]{edit_user.getText().toString()});
                    if (!cursor.moveToFirst()) {
                        if (edit_password.getText().toString().equals(edit_truepassword.getText().toString())) {
                            dbHelper.getReadableDatabase().execSQL("insert into User values(null,?,?,?,?,?)", new Object[]{
                                    spinner_power.getSelectedItemPosition(),
                                    Myutils.formatDateTime(System.currentTimeMillis()),
                                    spinner_power.getSelectedItem().toString(),
                                    edit_user.getText().toString(),
                                    edit_password.getText().toString()
                            });
                            edit_user.setText("");
                            edit_password.setText("");
                            edit_truepassword.setText("");
                            Toast.makeText(getApplicationContext(), "添加成功",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "确认密碼不匹配，请重新输入",
                                    Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "用户已存在",
                                Toast.LENGTH_SHORT).show();
                    }
                    cursor.close();
                } else {
                    Toast.makeText(getApplicationContext(), "用户名不能为空",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnupdate = (Button) findViewById(R.id.btnupdate);
        btnupdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                diary(dbHelper, Diary.userPower_change);
//                if (edit_password.getText().toString().equals(edit_truepassword.getText().toString())) {
//                    Cursor cursor = dbHelper.getReadableDatabase().rawQuery(
//                            "select name from User where userName=?", new String[]{edit_user.getText().toString()});
//                    if (cursor.moveToFirst()) {
//                        if (!cursor.getString(cursor.getColumnIndex("name")).equals(spinner_power.getSelectedItem().toString())) {
//                            dbHelper.getReadableDatabase().execSQL(
//                                    "update User set userPower=?,registerTime=?,name=?,password=? where userName=?", new Object[]{
//                                            spinner_power.getSelectedItemPosition(),
//                                            Myutils.formatDateTime(System.currentTimeMillis()),
//                                            spinner_power.getSelectedItem().toString(),
//                                            edit_password.getText().toString(),
//                                            edit_user.getText().toString()
//                                    });
//                            Toast.makeText(getApplicationContext(), "更新成功",
//                                    Toast.LENGTH_SHORT).show();
//                        } else {
//                            Toast.makeText(getApplicationContext(), "用户权限设置冲突，请重新设置权限",
//                                    Toast.LENGTH_SHORT).show();
//                        }
//                    } else {
//                        Toast.makeText(getApplicationContext(), "用户不存在", Toast.LENGTH_SHORT).show();
//                    }
//                    cursor.close();
//                } else {
//                    Toast.makeText(getApplicationContext(), "确认密碼不匹配，请重新输入",
//                            Toast.LENGTH_SHORT).show();
//                }
                if (userFragment == null) {
                    fragmentManager = getFragmentManager();
                    transaction = fragmentManager.beginTransaction();
                    userFragment = new UserFragment();
                    transaction.replace(R.id.userlayout, userFragment);
                    transaction.commit();
                }
                if (linearLayout.getVisibility() == View.INVISIBLE) {
                    linearLayout.setVisibility(View.VISIBLE);
                } else {
                    userFragment = null;
                    linearLayout.setVisibility(View.INVISIBLE);
                }
            }
        });
        btnback = (Button) findViewById(R.id.btn_back);
        btnback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                diary(dbHelper, Diary.power_back);
                startActivity(new Intent(UserPower.this, MainActivity.class));
                unbindService(serviceConnection);
                finish();
            }
        });
        text_temp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                warning();
            }
        });

        textFan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                windWarning();
            }
        });
    }

    public List<String> getDataSource() {
        spinnerList = new ArrayList<>();
        spinnerList.add("管理员");
        spinnerList.add("操作员");
        //spinnerList.add("维护员");
        return spinnerList;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }

    private void diary(MyDatabaseHelper myDatabaseHelper, String context) {
        myDatabaseHelper.getReadableDatabase().execSQL(
                "insert into Diary values(null,?,?,?,?)",
                new String[]{
                        Myutils.formatDateTime(System.currentTimeMillis()),
                        context,
                        Myutils.getPowername(),
                        Myutils.getUsername()
                });
    }

    public void warning() {
        LinearLayout temp_form = (LinearLayout) getLayoutInflater().inflate(R.layout.temp_form, null);
        final EditText tempUp = (EditText) temp_form.findViewById(R.id.edit_up);
        final EditText tempDown = (EditText) temp_form.findViewById(R.id.edit_down);
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("报警温度设置")
                .setIcon(R.drawable.warning)
                .setView(temp_form)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if ((!TextUtils.isEmpty(tempUp.getText())) && (!TextUtils.isEmpty(tempDown.getText()))) {
                            int up = (int) (Float.parseFloat(tempUp.getText().toString()) * 10);
                            int down = (int) (Float.parseFloat(tempDown.getText().toString()) * 10);
                            if (up > down) {
                                Myutils.setTempUp(up);
                                Myutils.setTempDown(down);
                                setSharedPreference("tempUp", up, 0, true);
                                setSharedPreference("tempDown", down, 0, true);
                                textUp.setText("温度上限:\n" + (float) Myutils.getTempUp() / 10 + "℃");
                                textDown.setText("温度下限:\n" + (float) Myutils.getTempDown() / 10 + "℃");
                                Toast.makeText(UserPower.this, "报警温度设置成功", Toast.LENGTH_SHORT).show();
                            } else {
                                String str = "报警温度范围上限值必须大于下限值";
                                Toast.makeText(UserPower.this, str, Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            String str = "报警温度范围未设置";
                            Toast.makeText(UserPower.this, str, Toast.LENGTH_SHORT).show();
                        }
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        builder.create().show();
    }

    public void windWarning() {
        LinearLayout wind_form = (LinearLayout) getLayoutInflater().inflate(R.layout.wind_form, null);
        final EditText hour = (EditText) wind_form.findViewById(R.id.edit_hour);
        final EditText minite = (EditText) wind_form.findViewById(R.id.edit_minite);
        final EditText second = (EditText) wind_form.findViewById(R.id.edit_second);
        final EditText hour1 = (EditText) wind_form.findViewById(R.id.edit_hour1);
        final EditText minite1 = (EditText) wind_form.findViewById(R.id.edit_minite1);
        final EditText second1 = (EditText) wind_form.findViewById(R.id.edit_second1);
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("风扇参数设置")
                .setIcon(R.drawable.warning)
                .setView(wind_form)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if ((!TextUtils.isEmpty(hour.getText())) || (!TextUtils.isEmpty(minite.getText()) || (!TextUtils.isEmpty(second.getText())))) {
                            if ((!TextUtils.isEmpty(hour1.getText())) || (!TextUtils.isEmpty(minite1.getText()) || (!TextUtils.isEmpty(second1.getText())))) {
                                long lHour, lMinite, lSecond, lTime;
                                long lHour1, lMinite1, lSecond1, lTime1;
                                if (TextUtils.isEmpty(hour.getText())) {
                                    lHour = 0;
                                } else {
                                    int iHour = Integer.parseInt(hour.getText().toString());
                                    lHour = (long) iHour;
                                }
                                if (TextUtils.isEmpty(minite.getText())) {
                                    lMinite = 0;
                                } else {
                                    int iMinite = Integer.parseInt(minite.getText().toString());
                                    lMinite = (long) iMinite;
                                }
                                if (TextUtils.isEmpty(second.getText())) {
                                    lSecond = 0;
                                } else {
                                    int iSecond = Integer.parseInt(second.getText().toString());
                                    lSecond = (long) iSecond;
                                }
                                lTime = lHour * 3600000 + lMinite * 60000 + lSecond * 1000;

                                if (TextUtils.isEmpty(hour1.getText())) {
                                    lHour1 = 0;
                                } else {
                                    int iHour1 = Integer.parseInt(hour1.getText().toString());
                                    lHour1 = (long) iHour1;
                                }
                                if (TextUtils.isEmpty(minite1.getText())) {
                                    lMinite1 = 0;
                                } else {
                                    int iMinite1 = Integer.parseInt(minite1.getText().toString());
                                    lMinite1 = (long) iMinite1;
                                }
                                if (TextUtils.isEmpty(second1.getText())) {
                                    lSecond1 = 0;
                                } else {
                                    int iSecond1 = Integer.parseInt(second1.getText().toString());
                                    lSecond1 = (long) iSecond1;
                                }
                                lTime1 = lHour1 * 3600000 + lMinite1 * 60000 + lSecond1 * 1000;

                                if (lTime < 3600 * 24 * 1000) {
                                    if (lTime > lTime1) {
                                        Myutils.setFanTime(lTime);
                                        setSharedPreference("fanTime", 0, lTime, false);
                                        Myutils.setFanLast(lTime1);
                                        setSharedPreference("fanLast", 0, lTime1, false);
                                        Toast.makeText(UserPower.this, "风扇参数设置成功", Toast.LENGTH_SHORT).show();
                                    } else {
                                        String str = "风扇启动间隔时间必须大于风扇启动持续时间";
                                        Toast.makeText(UserPower.this, str, Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    String str = "风扇启动间隔时间必须小于24小时";
                                    Toast.makeText(UserPower.this, str, Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                String str = "风扇启动持续时间未设置";
                                Toast.makeText(UserPower.this, str, Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            String str = "风扇启动间隔时间未设置";
                            Toast.makeText(UserPower.this, str, Toast.LENGTH_SHORT).show();
                        }
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        builder.create().show();
    }

    public void setSharedPreference(String key, int values, long values1, boolean flag) {
        SharedPreferences coefficient;
        coefficient = getSharedPreferences("option", MODE_PRIVATE);
        SharedPreferences.Editor editor;
        editor = coefficient.edit();
        if (flag)
            editor.putInt(key, values);
        if (!flag)
            editor.putLong(key, values1);
        editor.apply();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (isShouldHideInput(v, ev)) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

                }
            }
            return super.dispatchTouchEvent(ev);
        }
        if (getWindow().superDispatchTouchEvent(ev)) {
            return true;
        }
        return onTouchEvent(ev);
    }

    public boolean isShouldHideInput(View v, MotionEvent event) {
        if (v != null && (v instanceof EditText)) {
            int[] leftTop = {0, 0};
            v.getLocationInWindow(leftTop);
            int left = leftTop[0];
            int top = leftTop[1];
            int bottom = top + v.getHeight();
            int right = left + v.getWidth();
            if (event.getX() > left && event.getX() < right && event.getY() > top && event.getY() < bottom) {
                return false;
            } else {
                return true;
            }
        }
        return false;
    }
}
