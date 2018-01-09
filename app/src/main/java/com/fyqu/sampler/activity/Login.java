package com.fyqu.sampler.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.dev.temp.dev;
import com.fyqu.sampler.Myutils;
import com.fyqu.sampler.R;
import com.fyqu.sampler.Transform;
import com.fyqu.sampler.api.SerialPort;
import com.fyqu.sampler.database.Diary;
import com.fyqu.sampler.database.MyDatabaseHelper;
import com.fyqu.sampler.service.MyService;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class Login extends AppCompatActivity {
    SharedPreferences coordinate;
    SharedPreferences preferences;
    Button btn_login;
    MyDatabaseHelper dbHelper;
    AutoCompleteTextView autouser;
    TextView textpower;
    EditText edit_password;
    ServiceConnection serviceConnection;
    MyService.MyBinder myBinder;
    MyService myService;
    Intent bindIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        coordinate = getSharedPreferences("option", MODE_PRIVATE);
        Myutils.setTempUp(coordinate.getInt("tempUp", 500));
        Myutils.setTempDown(coordinate.getInt("tempDown", 200));
        Myutils.setFanTime(coordinate.getLong("fanTime", 60000));
        Myutils.setFanLast(coordinate.getLong("fanLast", 30000));
        //////////////////////////////////////////////////////
        //////////////////////////////////////////////////////
        preferences = getSharedPreferences("motor", MODE_PRIVATE);
        Myutils.setStrAbc(preferences.getString("abc", ""));
        Myutils.setStrEna(preferences.getString("enable", ""));
        Myutils.setStrOff(preferences.getString("off", ""));
        Myutils.setStrCur(preferences.getString("cur", ""));
        Myutils.setStrAcr(preferences.getString("acr", ""));
        Myutils.setStrMcs(preferences.getString("mcs", ""));
        Myutils.setStrSpd(preferences.getString("spd", ""));
        Myutils.setStrStp(preferences.getString("stp", ""));
        Myutils.setStrDf(preferences.getString("df", ""));
        Myutils.setStrDf1(preferences.getString("df1", ""));
        Myutils.setStrDf2(preferences.getString("df2", ""));
        Myutils.setStrDf3(preferences.getString("df3", ""));
        Myutils.setStrDf4(preferences.getString("df4", ""));
        ////////////////////////////////////////////////////
        ////////////////////////////////////////////////////
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
        bindIntent = new Intent(this, MyService.class);
        startService(bindIntent);
        bindService(bindIntent, serviceConnection, BIND_AUTO_CREATE);

        dbHelper = new MyDatabaseHelper(this);
        diary(dbHelper, Diary.login_start);
        autouser = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView);
        textpower = (TextView) findViewById(R.id.text_power);
        edit_password = (EditText) findViewById(R.id.edit_password);
        final String[] struser = {"almighty", "111111"};
        try {
            Cursor cursor = dbHelper.getReadableDatabase().rawQuery(
                    "select _id,userName from User", null);
            inflateList(cursor);
        } catch (Exception e) {
            e.printStackTrace();
        }
        final TextWatcher textWatcher_power = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Cursor cursor = dbHelper.getReadableDatabase().rawQuery(
                        "select _id,name from User where userName=?", new String[]{s.toString()});
                while (cursor.moveToNext()) {
                    textpower.setText(cursor.getString(cursor.getColumnIndex("name")));
                }
                cursor.close();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };
        autouser.addTextChangedListener(textWatcher_power);
        btn_login = (Button) findViewById(R.id.btn_login);
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (autouser.getText().toString().equals(struser[0])
                        & edit_password.getText().toString().equals(struser[1])) {
                    Intent intent = new Intent(Login.this, MainActivity.class);
                    //intent.putExtra("power",textpower.getText().toString());
                    Myutils.setPowername("管理员");
                    Myutils.setUsername("almighty");
                    startActivity(intent);
                    unbindService(serviceConnection);
                } else {
                    logining(autouser.getText().toString(), edit_password.getText().toString());
                }
            }
        });
    }

    public void logining(String user, String password) {
        Cursor cursor = dbHelper.getReadableDatabase().rawQuery(
                "select _id,password from User where userName=?", new String[]{user});
        if (cursor.moveToFirst()) {//判断用户是否存在
            String truepassword = cursor.getString(cursor.getColumnIndex("password"));
            if (truepassword.equals(password)) {
                Myutils.setPowername(textpower.getText().toString());
                Myutils.setUsername(autouser.getText().toString());
                Intent intent = new Intent(Login.this, MainActivity.class);
                startActivity(intent);
                unbindService(serviceConnection);
                diary(dbHelper, Diary.login_userPower(Myutils.getUsername(), Myutils.getPowername()));
                autouser.setText("");
                edit_password.setText("");
            } else {
                Toast.makeText(getApplicationContext(), "密码输入错误", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getApplicationContext(), "用户不存在", Toast.LENGTH_SHORT).show();
        }
        cursor.close();
    }

    private void inflateList(Cursor cursor) {
        ArrayList<String> list = new ArrayList<>();
        while (cursor.moveToNext()) {
            list.add(cursor.getString(cursor.getColumnIndex("userName")));
        }
        ArrayAdapter<String> autotext = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, list);
        autouser.setAdapter(autotext);
        cursor.close();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbindService(serviceConnection);
        stopService(bindIntent);
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
