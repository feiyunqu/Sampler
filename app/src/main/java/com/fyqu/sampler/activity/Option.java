package com.fyqu.sampler.activity;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextClock;
import android.widget.TextView;

import com.fyqu.sampler.Myutils;
import com.fyqu.sampler.R;
import com.fyqu.sampler.database.Diary;
import com.fyqu.sampler.database.MyDatabaseHelper;
import com.fyqu.sampler.fragment.Diarylist;
import com.fyqu.sampler.service.MyService;

import java.io.FileOutputStream;

public class Option extends AppCompatActivity {
    MyDatabaseHelper dbHelper;
    Button btnDiaryQuary;
    Button btnback;
    Button btnHide;
    Diarylist diarylist;
    FragmentManager fragmentManager;
    FragmentTransaction transaction;
    LinearLayout linearLayout;

    TextClock textClock;
    TextView text_power;
    TextView text_user;
    TextView text_temp;
    TextView textUp;
    TextView textDown;
    TextView textFan;
    ServiceConnection serviceConnection;
    MyService.MyBinder myBinder;
    MyService myService;
    Handler handler = new Handler();
    float exam;
    boolean windFlag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_option);
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
        textClock = (TextClock) findViewById(R.id.textClock7);
        text_power = (TextView) findViewById(R.id.text_power_9);
        text_user = (TextView) findViewById(R.id.text_user_9);
        text_temp = (TextView) findViewById(R.id.text_temprature9);
        textUp = (TextView) findViewById(R.id.text_up4);
        textDown = (TextView) findViewById(R.id.text_down4);
        textFan = (TextView) findViewById(R.id.text_fan9);
        textClock.setFormat24Hour("yyyy-MM-dd\nHH:mm:ss");
        text_power.setText("权限:\n" + Myutils.getPowername());
        text_user.setText("用户名:\n" + Myutils.getUsername());
        textUp.setText("温度上限:\n" + (float) Myutils.getTempUp() / 10 + "℃");
        textDown.setText("温度下限:\n" + (float) Myutils.getTempDown() / 10 + "℃");

        dbHelper = new MyDatabaseHelper(this);
        diary(dbHelper, Diary.diary_start);
        linearLayout = (LinearLayout) findViewById(R.id.fragLayout);
        Cursor cursor = dbHelper.getReadableDatabase().rawQuery("select * from Diary where dateTime like ?",
                new String[]{Myutils.formatDate(System.currentTimeMillis()) + "%"});//cursor里必须包含主键"_id"
        inflateList(cursor);

        btnDiaryQuary = (Button) findViewById(R.id.btn_diary_quary);
        btnDiaryQuary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                diary(dbHelper, Diary.diary_search);
                if (diarylist == null) {
                    fragmentManager = getFragmentManager();
                    transaction = fragmentManager.beginTransaction();
                    diarylist = new Diarylist();
                    transaction.replace(R.id.fragLayout, diarylist);
                    transaction.commit();
                }
                if (linearLayout.getVisibility() == View.INVISIBLE) {
                    linearLayout.setVisibility(View.VISIBLE);
                } else {
                    linearLayout.setVisibility(View.INVISIBLE);
                }
            }
        });

        btnback = (Button) findViewById(R.id.btnback);
        btnback.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                diary(dbHelper, Diary.diary_back);
                startActivity(new Intent(Option.this, MainActivity.class));
                unbindService(serviceConnection);
                finish();
            }
        });
        btnHide = (Button) findViewById(R.id.btn_hide);
        if (text_user.getText().toString().equals("用户名:\nalmighty")) {
            btnHide.setVisibility(View.VISIBLE);
        } else {
            btnHide.setVisibility(View.GONE);
        }

        btnHide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Option.this, Motor.class));
                unbindService(serviceConnection);
                finish();
            }
        });
    }

    private void inflateList(Cursor cursor) {
        final ListView diarylist = (ListView) findViewById(R.id.diarylist);
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                this,
                R.layout.item_diary,
                cursor,
                new String[]{"_id", "dateTime", "context", "powerName", "userName"},
                new int[]{
                        R.id.item_id,
                        R.id.item_date,
                        R.id.item_context,
                        R.id.item_user,
                        R.id.item_power},
                CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        diarylist.setAdapter(adapter);
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
    public void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
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
