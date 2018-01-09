package com.fyqu.sampler.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.dev.temp.dev;
import com.fyqu.sampler.Myutils;
import com.fyqu.sampler.Transform;
import com.fyqu.sampler.activity.UserPower;
import com.fyqu.sampler.api.SerialPort;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class MyService extends Service {
    Handler handler = new Handler();
    FileOutputStream mOutputStream;
    FileInputStream mInputStream;
    SerialPort sp;
    Thread thread, threadPress;
    StringBuffer stringBuffer = new StringBuffer();
    CallBacks callbacks;
    boolean flag = true;
    dev dTemp = new dev();
    DataReceived dataReceived = new DataReceived();
    float exam;
    int realExam;
    boolean windFlag = false;
    boolean lightFlag = false;
    final String lightOpen = "A21T";//开灯
    final String lightClose = "A20T";//关灯
    final String windOpen = "A11T";//吹风
    final String windClose = "A10T";//停风
    Timer timer;
    long index;
    int r;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return new MyBinder();
    }

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
                if (msg.startsWith("4f5554") && msg.endsWith("0d0a")) {
                    switch (msg) {
                        case "4f555432204f4e0d0a":
                            if (!windFlag) {
                                try {
                                    mOutputStream.write(windOpen.getBytes());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                windFlag = true;
                            }
                            break;//OUT2_ON 点灯成功
                        case "4f555432204f46460d0a":
                            if (windFlag) {
                                try {
                                    mOutputStream.write(windClose.getBytes());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                windFlag = false;
                            }
                            break;//OUT2_OFF 关灯成功
                        case "4f555431204f4e0d0a":
                            if (lightFlag)
                                Toast.makeText(MyService.this, "当前温度超标，已开启风扇", Toast.LENGTH_SHORT).show();
                            break;//OUT1_ON 刮风成功
                        case "4f555431204f46460d0a":
                            if (lightFlag)
                                Toast.makeText(MyService.this, "温度回复正常，已关闭风扇", Toast.LENGTH_SHORT).show();
                            break;//OUT1_OFF 风停了
                    }
                }
            }
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //dTemp.Adj();
        r = dTemp.Open();

        /*TODO 打开串口*/
        try {
            sp = new SerialPort(new File("/dev/ttyAMA2"), 9600, 0);
        } catch (IOException e) {
            e.printStackTrace();
        }

        mOutputStream = (FileOutputStream) sp.getOutputStream();
        mInputStream = (FileInputStream) sp.getInputStream();

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

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (index * 1000 % Myutils.getFanTime() == 0) {
                    if (!windFlag) {
                        if (!lightFlag) {
                            try {
                                mOutputStream.write(windOpen.getBytes());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            windFlag = true;
                        }
                    }
                }
                if (index * 1000 % Myutils.getFanTime() == Myutils.getFanLast()) {
                    try {
                        mOutputStream.write(windClose.getBytes());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    windFlag = false;
                }
                index++;
            }
        }, 0, 1000);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d("tag", "onUnbind: ");
        flag = false;
        return true;
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
        Log.d("tag", "onRebind: ");
        if (threadPress.isInterrupted()) {
            thread.start();
            Log.d("tag", "isinterruppted");
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            dTemp.Close();
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("yyyy", "onDestroy: ");
        }
        timer.cancel();
        mInputStream = null;
        flag = false;
        sp.close();
    }

    public class MyBinder extends Binder {
        public MyService getService() {
            return MyService.this;
        }

        public void press() {
            threadPress = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (flag) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if (r!=0){
                            r=dTemp.Open();
                        }else {
                            exam = dTemp.Get();
                        }

                        exam = exam / 10;
                        if (callbacks != null) {
                            callbacks.startRead(exam, windFlag);
                        }
                        realExam = (int) (exam * 10);
                        if (realExam < 1000) {
                            if (realExam > Myutils.getTempUp()) {
                                if (!lightFlag) {
                                    try {
                                        mOutputStream.write(lightOpen.getBytes());
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    lightFlag = true;
                                }
                            } else if (realExam < Myutils.getTempDown()) {
                                if (lightFlag) {
                                    try {
                                        mOutputStream.write(lightClose.getBytes());
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    lightFlag = false;
                                }
                            }
                        }
                    }
                    Log.d("tag", "run: false");
                }
            });
            threadPress.start();
        }

        public void threadGo() {
            flag = true;
        }

        public void fanPress() {

        }
    }

    public interface CallBacks {
        void startRead(float f, boolean flag);

        void output(FileOutputStream outputStream);
    }

    public void setValues(CallBacks callBacks) {
        this.callbacks = callBacks;
        if (callBacks != null) {
            callbacks.output(mOutputStream);
            callbacks.startRead(exam, windFlag);
        }
    }
}
