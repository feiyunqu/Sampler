package com.fyqu.sampler.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.fyqu.sampler.Myutils;
import com.fyqu.sampler.R;
import com.fyqu.sampler.SerialOrder;
import com.fyqu.sampler.Transform;
import com.fyqu.sampler.api.SerialPort;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class Motor extends AppCompatActivity {
    Handler handler = new Handler();
    FileOutputStream mOutputStream, holeOutputStream;
    FileInputStream mInputStream, holeIutputStream;
    SerialPort sp, hole;
    Thread thread, holeThread;
    StringBuffer stringBuffer = new StringBuffer();
    DataReceived dataReceived = new DataReceived();

    Button btnHand;
    Button btnEnable;
    Button btnOff;
    Button btnCur;
    Button btnAcr;
    Button btnMcs;
    Button btnSpeed;
    Button btnStep;
    Button btnDefine;
    Button btnDefine1;
    Button btnDefine2;
    Button btnDefine3;
    Button btnDefine4;
    Button btnBack;
    EditText editCur;
    EditText editAcr;
    EditText editMsc;
    EditText editSpeed;
    EditText editStep;
    EditText editDefine;
    EditText editDefine1;
    EditText editDefine2;
    EditText editDefine3;
    EditText editDefine4;

    SharedPreferences perferences;
    SharedPreferences preferences;
    String str_abc, str_enable, str_off;
    String str_cur, str_acr, str_mcs, str_spd, str_stp;
    String str_df, str_df1, str_df2, str_df3, str_df4;

    Button bHole1, bHole2, bHole3, bHole4, bHole5, bHole6, bHole7, bHole8, bHole9, bHole10;

    /*TODO 读取串口的数据处理*/
    private class DataReceived implements Runnable {
        @Override
        public void run() {
            if (stringBuffer.length() != 0) {
                String msg = stringBuffer.toString();
                stringBuffer.delete(0, stringBuffer.length());
                Log.d("logining", "run: " + msg);
//                streamdata = streamdata + msg;
//                Log.d("dddd", "run: " + streamdata);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_motor);
        perferences = getSharedPreferences("motor", MODE_PRIVATE);
        btnHand = (Button) findViewById(R.id.btn_hand);
        btnEnable = (Button) findViewById(R.id.btn_enable);
        btnOff = (Button) findViewById(R.id.btn_off);
        btnCur = (Button) findViewById(R.id.btn_cur);
        btnAcr = (Button) findViewById(R.id.btn_acr);
        btnMcs = (Button) findViewById(R.id.btn_mcs);
        btnSpeed = (Button) findViewById(R.id.btn_speed);
        btnStep = (Button) findViewById(R.id.btn_step);
        btnDefine = (Button) findViewById(R.id.btn_define);
        btnDefine1 = (Button) findViewById(R.id.btn_define1);
        btnDefine2 = (Button) findViewById(R.id.btn_define2);
        btnDefine3 = (Button) findViewById(R.id.btn_define3);
        btnDefine4 = (Button) findViewById(R.id.btn_define4);
        btnBack = (Button) findViewById(R.id.btn_back);
        editCur = (EditText) findViewById(R.id.edit_cur);
        editAcr = (EditText) findViewById(R.id.edit_acr);
        editMsc = (EditText) findViewById(R.id.edit_mcs);
        editSpeed = (EditText) findViewById(R.id.edit_speed);
        editStep = (EditText) findViewById(R.id.edit_step);
        editDefine = (EditText) findViewById(R.id.edit_define);
        editDefine1 = (EditText) findViewById(R.id.edit_define1);
        editDefine2 = (EditText) findViewById(R.id.edit_define2);
        editDefine3 = (EditText) findViewById(R.id.edit_define3);
        editDefine4 = (EditText) findViewById(R.id.edit_define4);

        bHole1 = (Button) findViewById(R.id.btnHole1);
        bHole2 = (Button) findViewById(R.id.btnHole2);
        bHole3 = (Button) findViewById(R.id.btnHole3);
        bHole4 = (Button) findViewById(R.id.btnHole4);
        bHole5 = (Button) findViewById(R.id.btnHole5);
        bHole6 = (Button) findViewById(R.id.btnHole6);
        bHole7 = (Button) findViewById(R.id.btnHole7);
        bHole8 = (Button) findViewById(R.id.btnHole8);
        bHole9 = (Button) findViewById(R.id.btnHole9);
        bHole10 = (Button) findViewById(R.id.btnHole10);

        preferences = getSharedPreferences("motor", MODE_PRIVATE);
        if (!TextUtils.isEmpty(preferences.getString("cur", "")))
            editCur.setText(preferences.getString("cur", "").substring(3, preferences.getString("cur", "").length() - 1));
        if (!TextUtils.isEmpty(preferences.getString("acr", "")))
            editAcr.setText(preferences.getString("acr", "").substring(3, preferences.getString("acr", "").length() - 1));
        if (!TextUtils.isEmpty(preferences.getString("mcs", "")))
            editMsc.setText(preferences.getString("mcs", "").substring(3, preferences.getString("mcs", "").length() - 1));
        if (!TextUtils.isEmpty(preferences.getString("spd", "")))
            editSpeed.setText(preferences.getString("spd", "").substring(3, preferences.getString("spd", "").length() - 1));
        if (!TextUtils.isEmpty(preferences.getString("stp", "")))
            editStep.setText(preferences.getString("stp", ""));
        if (!TextUtils.isEmpty(preferences.getString("df", "")))
            editDefine.setText(preferences.getString("df", ""));
        if (!TextUtils.isEmpty(preferences.getString("df1", "")))
            editDefine1.setText(preferences.getString("df1", "").substring(0, preferences.getString("df1", "").length() - 1));
        if (!TextUtils.isEmpty(preferences.getString("df2", "")))
            editDefine2.setText(preferences.getString("df2", "").substring(0, preferences.getString("df2", "").length() - 1));
        if (!TextUtils.isEmpty(preferences.getString("df3", "")))
            editDefine3.setText(preferences.getString("df3", "").substring(0, preferences.getString("df3", "").length() - 1));
        if (!TextUtils.isEmpty(preferences.getString("df4", "")))
            editDefine4.setText(preferences.getString("df4", "").substring(0, preferences.getString("df4", "").length() - 1));

        /*TODO 打开串口*/
        try {
            sp = new SerialPort(new File("/dev/ttyAMA3"), 9600, 0);
            hole = new SerialPort(new File("/dev/ttyAMA0"), 19200, 0);
        } catch (IOException e) {
            e.printStackTrace();
        }

        mOutputStream = (FileOutputStream) sp.getOutputStream();
        mInputStream = (FileInputStream) sp.getInputStream();
        holeOutputStream = (FileOutputStream) hole.getOutputStream();
        holeIutputStream = (FileInputStream) hole.getInputStream();

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

        holeThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (holeIutputStream != null) {
                    try {
                        int length = holeIutputStream.available();
                        if (length > 0) {
                            byte[] buffer = new byte[length];
                            holeIutputStream.read(buffer);//该方法会阻塞线程直到接收到数据 
                            stringBuffer.append(Transform.byte2hex(buffer));
                            handler.post(dataReceived);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        holeThread.start();

        str_abc = "ABC;";
        str_enable = "ENA;";
        str_off = "OFF;";

        btnHand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    mOutputStream.write(str_abc.getBytes());
                    Thread.sleep(500);
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        btnEnable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    mOutputStream.write(str_enable.getBytes());
                    Thread.sleep(500);
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        btnOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    mOutputStream.write(str_off.getBytes());
                    Thread.sleep(500);
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        btnCur.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(editCur.getText())) {
                    str_cur = "CUR" + editCur.getText().toString() + ";";
                    try {
                        mOutputStream.write(str_cur.getBytes());
                        Thread.sleep(500);
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(Motor.this, "我是按键君", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(Motor.this, "没有输入数据", Toast.LENGTH_SHORT).show();
                }
            }
        });
        btnAcr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(editAcr.getText())) {
                    str_acr = "ACR" + editAcr.getText().toString() + ";";
                    try {
                        mOutputStream.write(str_acr.getBytes());
                        Thread.sleep(500);
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(Motor.this, "我是按键君", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(Motor.this, "没有输入数据", Toast.LENGTH_SHORT).show();
                }
            }
        });
        btnMcs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(editMsc.getText())) {
                    str_mcs = "MCS" + editMsc.getText().toString() + ";";
                    try {
                        mOutputStream.write(str_mcs.getBytes());
                        Thread.sleep(500);
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(Motor.this, "我是按键君", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(Motor.this, "没有输入数据", Toast.LENGTH_SHORT).show();
                }
            }
        });
        btnSpeed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(editSpeed.getText())) {
                    str_spd = "SPD" + editSpeed.getText().toString() + ";";
                    try {
                        mOutputStream.write(str_spd.getBytes());
                        Thread.sleep(500);
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(Motor.this, "我是按键君", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(Motor.this, "没有输入数据", Toast.LENGTH_SHORT).show();
                }
            }
        });
        btnStep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(editStep.getText())) {
                    str_stp = "STP" + editStep.getText().toString() + ";";
                    try {
                        mOutputStream.write(str_stp.getBytes());
                        Thread.sleep(500);
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(Motor.this, "我是按键君", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(Motor.this, "没有输入数据", Toast.LENGTH_SHORT).show();
                }
            }
        });
        btnDefine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(editDefine.getText())) {
                    str_df = "STP" + editDefine.getText().toString() + ";";
                    try {
                        mOutputStream.write(str_df.getBytes());
                        Thread.sleep(500);
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(Motor.this, "我是按键君", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(Motor.this, "没有输入数据", Toast.LENGTH_SHORT).show();
                }
            }
        });
        btnDefine1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(editDefine1.getText())) {
                    str_df1 = editDefine1.getText().toString() + ";";
                    try {
                        mOutputStream.write(str_df1.getBytes());
                        Thread.sleep(500);
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(Motor.this, "我是按键君", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(Motor.this, "没有输入数据", Toast.LENGTH_SHORT).show();
                }
            }
        });
        btnDefine2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(editDefine2.getText())) {
                    str_df2 = editDefine2.getText().toString() + ";";
                    try {
                        mOutputStream.write(str_df2.getBytes());
                        Thread.sleep(500);
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(Motor.this, "我是按键君", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(Motor.this, "没有输入数据", Toast.LENGTH_SHORT).show();
                }
            }
        });
        btnDefine3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(editDefine3.getText())) {
                    str_df3 = editDefine3.getText().toString() + ";";
                    try {
                        mOutputStream.write(str_df3.getBytes());
                        Thread.sleep(500);
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(Motor.this, "我是按键君", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(Motor.this, "没有输入数据", Toast.LENGTH_SHORT).show();
                }
            }
        });
        btnDefine4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(editDefine4.getText())) {
                    str_df4 = editDefine4.getText().toString() + ";";
                    try {
                        mOutputStream.write(str_df4.getBytes());
                        Thread.sleep(500);
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(Motor.this, "我是按键君", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(Motor.this, "没有输入数据", Toast.LENGTH_SHORT).show();
                }
            }
        });
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                str_cur = "CUR" + editCur.getText().toString() + ";";
                str_acr = "ACR" + editAcr.getText().toString() + ";";
                str_mcs = "MCS" + editMsc.getText().toString() + ";";
                str_spd = "SPD" + editSpeed.getText().toString() + ";";
                str_stp = "STP" + editStep.getText().toString() + ";";
                str_df = editDefine.getText().toString() + ";";
                str_df1 = editDefine1.getText().toString() + ";";
                str_df2 = editDefine2.getText().toString() + ";";
                str_df3 = editDefine3.getText().toString() + ";";
                str_df4 = editDefine4.getText().toString() + ";";

                Myutils.setStrAbc(str_abc);
                Myutils.setStrEna(str_enable);
                Myutils.setStrOff(str_off);
                Myutils.setStrCur(str_cur);
                Myutils.setStrAcr(str_acr);
                Myutils.setStrMcs(str_mcs);
                Myutils.setStrSpd(str_spd);
                Myutils.setStrStp(editStep.getText().toString());
                Myutils.setStrDf(editDefine.getText().toString());
                Myutils.setStrDf1(str_df1);
                Myutils.setStrDf2(str_df2);
                Myutils.setStrDf3(str_df3);
                Myutils.setStrDf4(str_df4);

                SharedPreferences.Editor editor;
                editor = perferences.edit();
                editor.putString("abc", str_abc);
                editor.putString("enable", str_enable);
                editor.putString("off", str_off);
                editor.putString("cur", str_cur);
                editor.putString("acr", str_acr);
                editor.putString("mcs", str_mcs);
                editor.putString("spd", str_spd);
                editor.putString("stp", editStep.getText().toString());
                editor.putString("df", editDefine.getText().toString());
                editor.putString("df1", str_df1);
                editor.putString("df2", str_df2);
                editor.putString("df3", str_df3);
                editor.putString("df4", str_df4);
                editor.apply();
                mInputStream = null;
                holeIutputStream = null;
                startActivity(new Intent(Motor.this, MainActivity.class));
                finish();
            }
        });

        bHole1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    for (int i = 0; i < SerialOrder.holes[0].length(); i = i + 2) {
                        holeOutputStream.write(Integer.parseInt(SerialOrder.holes[0].substring(i, i + 2), 16));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        bHole2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    for (int i = 0; i < SerialOrder.holes[1].length(); i = i + 2) {
                        holeOutputStream.write(Integer.parseInt(SerialOrder.holes[1].substring(i, i + 2), 16));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        bHole3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    for (int i = 0; i < SerialOrder.holes[2].length(); i = i + 2) {
                        holeOutputStream.write(Integer.parseInt(SerialOrder.holes[2].substring(i, i + 2), 16));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        bHole4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    for (int i = 0; i < SerialOrder.holes[3].length(); i = i + 2) {
                        holeOutputStream.write(Integer.parseInt(SerialOrder.holes[3].substring(i, i + 2), 16));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        bHole5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    for (int i = 0; i < SerialOrder.holes[4].length(); i = i + 2) {
                        holeOutputStream.write(Integer.parseInt(SerialOrder.holes[4].substring(i, i + 2), 16));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        bHole6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    for (int i = 0; i < SerialOrder.holes[5].length(); i = i + 2) {
                        holeOutputStream.write(Integer.parseInt(SerialOrder.holes[5].substring(i, i + 2), 16));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        bHole7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    for (int i = 0; i < SerialOrder.holes[6].length(); i = i + 2) {
                        holeOutputStream.write(Integer.parseInt(SerialOrder.holes[6].substring(i, i + 2), 16));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        bHole8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    for (int i = 0; i < SerialOrder.holes[7].length(); i = i + 2) {
                        holeOutputStream.write(Integer.parseInt(SerialOrder.holes[7].substring(i, i + 2), 16));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        bHole9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    for (int i = 0; i < SerialOrder.holes[8].length(); i = i + 2) {
                        holeOutputStream.write(Integer.parseInt(SerialOrder.holes[8].substring(i, i + 2), 16));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        bHole10.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    for (int i = 0; i < SerialOrder.holes[9].length(); i = i + 2) {
                        holeOutputStream.write(Integer.parseInt(SerialOrder.holes[9].substring(i, i + 2), 16));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        hole.close();
        sp.close();
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
