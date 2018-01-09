package com.fyqu.sampler.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.PopupWindow;
import android.widget.SimpleAdapter;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

import com.fyqu.sampler.Myutils;
import com.fyqu.sampler.R;
import com.fyqu.sampler.SerialOrder;
import com.fyqu.sampler.Transform;
import com.fyqu.sampler.api.SerialPort;
import com.fyqu.sampler.database.Diary;
import com.fyqu.sampler.database.MyDatabaseHelper;
import com.fyqu.sampler.service.MyService;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    MyDatabaseHelper dbHelper;
    String[] items = new String[8];
    String[] origin = new String[8];
    Cursor cursor;
    int i = 0;
    int ii = 0;
    int hole = 9;
    float exam;
    boolean windFlag;
    ServiceConnection serviceConnection;
    MyService.MyBinder myBinder;
    MyService myService;
    Button btn_dataQuary;
    Button btn_diary;
    Button btn_powerOption;
    Button btn_exit;
    Button btn_run;
    Button btn_back;

    PopupWindow popup;
    GridView gridView;
    boolean farFlag = true;

    TextClock textClock;
    TextView text_shiji;
    TextView text_kucun;
    TextView text_power;
    TextView text_user;
    TextView text_shengYu;
    TextView text_temp;
    EditText edit_quYang;
    TextView textUp;
    TextView textDown;
    TextView textFan;

    FileOutputStream mOutputStream, motorOutputStream;
    FileInputStream mInputStream, motorInputStream;
    SerialPort sp, motor;
    Thread thread, motorThread;
    StringBuffer stringBuffer = new StringBuffer();
    Handler handler = new Handler();
    DataReceived dataReceived = new DataReceived();
    //Timer timer;

    String strKuCun;
    String strShengYu;

    /*TODO 读取串口的数据处理*/
    private class DataReceived implements Runnable {
        @Override
        public void run() {
            if (stringBuffer.length() != 0) {
                String msg = stringBuffer.toString();
                stringBuffer.delete(0, stringBuffer.length());
                Log.d("QWER", "run: " + msg);
//                streamdata = streamdata + msg;
//                Log.d("dddd", "run: " + streamdata);
                if (msg.startsWith("0366000000")) {
                    if (!msg.startsWith("036600000009")) {
                        try {
                            motorOutputStream.write(Myutils.getStrEna().getBytes());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        try {
                            long quYang = Integer.parseInt(edit_quYang.getText().toString());
                            long step = Integer.parseInt(Myutils.getStrStp());
                            long all = quYang * step;
                            String str_stp = "STP" + String.valueOf(all) + ";";
                            motorOutputStream.write(str_stp.getBytes());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        try {
                            motorOutputStream.write(Myutils.getStrSpd().getBytes());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        farFlag = false;
                        try {
                            long step = Integer.parseInt(Myutils.getStrDf());
                            String str_stp = "STP" + String.valueOf(step) + ";";
                            motorOutputStream.write(str_stp.getBytes());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        ////////////////////////刷新Item数据库
                        dbHelper.getReadableDatabase().execSQL(
                                "update Item set origin=? where item=?", new Object[]{
                                        strShengYu,
                                        text_shiji.getText().toString(),
                                });
                        /////////////////////刷新gridview
                        Cursor rCursor = dbHelper.getReadableDatabase().rawQuery("select * from Item", null);
                        while (rCursor.moveToNext()) {
                            items[ii] = rCursor.getString(rCursor.getColumnIndex("item"));
                            Log.d("iiii", "onCreate: " + items[ii] + ii);
                            origin[ii] = rCursor.getString(rCursor.getColumnIndex("origin"));
                            ii++;
                        }
                        List<Map<String, Object>> rListitems = new ArrayList<>();
                        for (int m = 0; m < items.length; m++) {
                            Map<String, Object> listitem = new HashMap<>();
                            listitem.put("items", items[m]);
                            listitem.put("origin", origin[m]);
                            rListitems.add(listitem);
                        }
                        SimpleAdapter simpleAdapter = new SimpleAdapter(MainActivity.this, rListitems, R.layout.list_item,
                                new String[]{"items", "origin"}, new int[]{R.id.text_item, R.id.text_origin});
                        gridView.setAdapter(simpleAdapter);
                        //////////////////////////////////////////
                        ii = 0;
                    }
                }
                if (msg.startsWith("cc00a8")) {
                    if (farFlag) {
                        try {
                            for (int i = 0; i < SerialOrder.holes[9].length(); i = i + 2) {
                                mOutputStream.write(Integer.parseInt(SerialOrder.holes[9].substring(i, i + 2), 16));
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        btn_run.setVisibility(View.VISIBLE);
                        btn_back.setVisibility(View.VISIBLE);
                        try {
                            motorOutputStream.write(Myutils.getStrOff().getBytes());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        popup.dismiss();
                        farFlag = true;
                    }
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textClock = (TextClock) findViewById(R.id.textClock);
        text_power = (TextView) findViewById(R.id.text_power);
        text_user = (TextView) findViewById(R.id.text_user);
        text_temp = (TextView) findViewById(R.id.text_temprature);
        textUp = (TextView) findViewById(R.id.text_up);
        textDown = (TextView) findViewById(R.id.text_down);
        textFan = (TextView) findViewById(R.id.text_fan);
        textClock.setFormat24Hour("yyyy-MM-dd\nHH:mm:ss");
        text_power.setText("权限:\n" + Myutils.getPowername());
        text_user.setText("用户名:\n" + Myutils.getUsername());
        textUp.setText("温度上限:\n" + (float) Myutils.getTempUp() / 10 + "℃");
        textDown.setText("温度下限:\n" + (float) Myutils.getTempDown() / 10 + "℃");

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

        /*TODO 打开串口*/
        try {
            sp = new SerialPort(new File("/dev/ttyAMA0"), 19200, 0);
            motor = new SerialPort(new File("/dev/ttyAMA3"), 9600, 0);
        } catch (IOException e) {
            e.printStackTrace();
        }

        mOutputStream = (FileOutputStream) sp.getOutputStream();
        mInputStream = (FileInputStream) sp.getInputStream();
        motorOutputStream = (FileOutputStream) motor.getOutputStream();
        motorInputStream = (FileInputStream) motor.getInputStream();

        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (mInputStream != null) {
                    try {
                        int length = mInputStream.available();
                        if (length > 0) {
                            byte[] buffer = new byte[length];
                            mInputStream.read(buffer);//该方法会阻塞线程直到接收到数据
                            stringBuffer.append(Transform.byte2hex(buffer));
                            handler.post(dataReceived);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        thread.start();

        motorThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (motorInputStream != null) {
                    try {
                        int length = motorInputStream.available();
                        if (length > 0) {
                            byte[] buffer = new byte[length];
                            motorInputStream.read(buffer);//该方法会阻塞线程直到接收到数据
                            stringBuffer.append(Transform.byte2hex(buffer));
                            handler.post(dataReceived);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        motorThread.start();

        View root = this.getLayoutInflater().inflate(R.layout.fragment_blank, null);
        popup = new PopupWindow(root, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        text_shiji = (TextView) root.findViewById(R.id.text_shiji);
        text_kucun = (TextView) root.findViewById(R.id.text_kucun);
        edit_quYang = (EditText) root.findViewById(R.id.edit_quyang);
        text_shengYu = (TextView) root.findViewById(R.id.text_shengyu);
        btn_run = (Button) root.findViewById(R.id.btn_run);
        btn_back = (Button) root.findViewById(R.id.btn_back);

        btn_dataQuary = (Button) findViewById(R.id.btn_quary);
        btn_diary = (Button) findViewById(R.id.btn_diary);
        btn_powerOption = (Button) findViewById(R.id.btn_power);
        btn_exit = (Button) findViewById(R.id.btn_exit);

        if (!Myutils.getPowername().equals("管理员"))
            btn_powerOption.setVisibility(View.INVISIBLE);
        dbHelper = new MyDatabaseHelper(this);
        diary(dbHelper, Diary.main_start);

        cursor = dbHelper.getReadableDatabase().rawQuery("select * from Item", null);
        if (!cursor.moveToFirst()) {
            String sqlString = "insert into Item values(null,?,?)";
            dbHelper.getReadableDatabase().execSQL(sqlString, new String[]{"物品一", "1000"});
            dbHelper.getReadableDatabase().execSQL(sqlString, new String[]{"物品二", "1000"});
            dbHelper.getReadableDatabase().execSQL(sqlString, new String[]{"物品三", "1000"});
            dbHelper.getReadableDatabase().execSQL(sqlString, new String[]{"物品四", "1000"});
            dbHelper.getReadableDatabase().execSQL(sqlString, new String[]{"物品五", "1000"});
            dbHelper.getReadableDatabase().execSQL(sqlString, new String[]{"物品六", "1000"});
            dbHelper.getReadableDatabase().execSQL(sqlString, new String[]{"物品七", "1000"});
            dbHelper.getReadableDatabase().execSQL(sqlString, new String[]{"物品八", "1000"});
            Log.d("iiii", "onCreate: ");
        }
        cursor = dbHelper.getReadableDatabase().rawQuery("select * from Item", null);
        while (cursor.moveToNext()) {
            items[i] = cursor.getString(cursor.getColumnIndex("item"));
            Log.d("iiii", "onCreate: " + items[i] + i);
            origin[i] = cursor.getString(cursor.getColumnIndex("origin"));
            i++;
        }
        List<Map<String, Object>> listitems = new ArrayList<>();
        for (int m = 0; m < items.length; m++) {
            Map<String, Object> listitem = new HashMap<>();
            listitem.put("items", items[m]);
            listitem.put("origin", origin[m]);
            listitems.add(listitem);
        }
        gridView = (GridView) findViewById(R.id.grid_table);
        SimpleAdapter simpleAdapter = new SimpleAdapter(this, listitems, R.layout.list_item,
                new String[]{"items", "origin"}, new int[]{R.id.text_item, R.id.text_origin});
        gridView.setAdapter(simpleAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                hole = position;
                TextView textItem = (TextView) view.findViewById(R.id.text_item);
                TextView textOrigin = (TextView) view.findViewById(R.id.text_origin);
                text_shiji.setText(textItem.getText().toString());
                String sqlString = "select origin from Item where item=?";
                cursor = dbHelper.getReadableDatabase().rawQuery(sqlString, new String[]{textItem.getText().toString()});
                cursor.moveToFirst();
                strKuCun = cursor.getString(cursor.getColumnIndex("origin"));
                text_kucun.setText(strKuCun + "ml");
                diary(dbHelper, Diary.main_selectList(textItem.getText().toString(), textOrigin.getText().toString()));
                popup.setOutsideTouchable(false);
                popup.setFocusable(true);
                popup.showAtLocation(view, Gravity.CENTER, 0, 0);
            }
        });

        TextWatcher textWatcher_sampler = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!(edit_quYang.getText().toString().startsWith(".") || edit_quYang.getText().toString().endsWith("."))) {
                    byte[] editByte = edit_quYang.getText().toString().getBytes();
                    for (int k = 0; k < editByte.length; k++) {
                        if (editByte[k] == '.') {
                            if (k != editByte.length - 2) {
                                edit_quYang.setText(edit_quYang.getText().subSequence(0, k + 2));
                                edit_quYang.setSelection(edit_quYang.length());
                            }
                            break;
                        }
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                diary(dbHelper, Diary.main_quYangLiang(s.toString()));
                if (!TextUtils.isEmpty(edit_quYang.getText())) {
                    if (new BigDecimal(edit_quYang.getText().toString()).compareTo(new BigDecimal(strKuCun)) == 1) {
                        edit_quYang.setText(strKuCun);
                        edit_quYang.setSelection(edit_quYang.length());
                    }
                    strShengYu = String.valueOf(new BigDecimal(strKuCun).subtract(new BigDecimal(edit_quYang.getText().toString())));
                    text_shengYu.setText(strShengYu + "ml");
                } else {
                    text_shengYu.setText("--");
                }
            }
        };
        edit_quYang.addTextChangedListener(textWatcher_sampler);
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                diary(dbHelper, Diary.main_cancel);
                popup.dismiss();
            }
        });
        btn_run.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                diary(dbHelper, Diary.main_run);
                if (!TextUtils.isEmpty(edit_quYang.getText())) {
                    if (edit_quYang.getText().toString().startsWith(".") || edit_quYang.getText().toString().endsWith(".")) {
                        Toast.makeText(MainActivity.this, "数字输入不规范", Toast.LENGTH_SHORT).show();
                    } else {
                        data(dbHelper);
                        try {
                            for (int i = 0; i < SerialOrder.holes[hole].length(); i = i + 2) {
                                mOutputStream.write(Integer.parseInt(SerialOrder.holes[hole].substring(i, i + 2), 16));
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        btn_run.setVisibility(View.INVISIBLE);
                        btn_back.setVisibility(View.INVISIBLE);
                    }
                } else {
                    Toast.makeText(MainActivity.this, "请先输入取样量", Toast.LENGTH_SHORT).show();
                }
            }
        });
        cursor.close();

        btn_dataQuary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                diary(dbHelper, Diary.dataSearch);
                startActivity(new Intent(MainActivity.this, DataScan.class));
                mInputStream = null;
                motorInputStream = null;
                unbindService(serviceConnection);
                finish();
            }
        });

        btn_diary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                diary(dbHelper, Diary.diaryScan);
                startActivity(new Intent(MainActivity.this, Option.class));
                mInputStream = null;
                motorInputStream = null;
                unbindService(serviceConnection);
                finish();
            }
        });

        btn_powerOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                diary(dbHelper, Diary.powerManage);
                startActivity(new Intent(MainActivity.this, UserPower.class));
                mInputStream = null;
                motorInputStream = null;
                unbindService(serviceConnection);
                finish();
            }
        });

        btn_exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                diary(dbHelper, Diary.loginExit);
                startActivity(new Intent(MainActivity.this, Login.class));
                mInputStream = null;
                motorInputStream = null;
                unbindService(serviceConnection);
                finish();
            }
        });
//        ///////////////////////////////////////////
//        timer = new Timer();
//        timer.schedule(new TimerTask() {
//            @Override
//            public void run() {
//                Message message = new Message();
//                mhandler.sendMessage(message);
//            }
//        }, 0, 500);
    }

    private void diary(MyDatabaseHelper myDatabaseHelper, String context) {
        String sqlString = "insert into Diary values(null,?,?,?,?)";
        myDatabaseHelper.getReadableDatabase().execSQL(sqlString, new String[]{
                Myutils.formatDateTime(System.currentTimeMillis()),
                context,
                Myutils.getPowername(),
                Myutils.getUsername()
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        timer.cancel();
        sp.close();
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

    private void data(MyDatabaseHelper myDatabaseHelper) {
        String sqlString = "insert into Data values(null,?,?,?,?,?,?,?,?)";
        myDatabaseHelper.getReadableDatabase().execSQL(sqlString, new String[]{
                Myutils.getUsername(),
                Myutils.getPowername(),
                text_shiji.getText().toString(),
                strKuCun,
                edit_quYang.getText().toString(),
                strShengYu,
                Myutils.formatDateTime(System.currentTimeMillis()),
                "成功"
        });
    }

//    private Handler mhandler = new Handler(new Handler.Callback() {
//        @Override
//        public boolean handleMessage(Message msg) {
//            text_temp.setText("温度:\n" + exam + '℃');
//            return false;
//        }
//    });
}
