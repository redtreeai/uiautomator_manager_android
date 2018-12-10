package com.example.chs.iskandar_client.service;


import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.chs.iskandar_client.model.Data;
import com.example.chs.iskandar_client.utils.Getcmdinfo;
import com.example.chs.iskandar_client.utils.HttpRequest;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by chs on 17-8-1.
 * 程序启动后此服务作为TaskManager的守护进程 (暂不启用)
 */

public class GuardManager extends Service {
    private GuardManager.MyThread myThread;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        this.myThread = new MyThread();
        this.myThread.start();
        super.onCreate();

        //第一次进入程序后立马启动taskmanamger service

        if(getRunningJobThreadId().equals("dead")){
            Intent inte = new Intent(this, TaskManager.class);
            Log.i("testlog", "心跳服务死亡");
            startService(inte);
            Log.i("testlog", "心跳服务已被守护进程guard唤醒");
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private class MyThread extends Thread {

        @Override
        public void run() {
            Data serviceFlag = (Data) getApplication();

            while (serviceFlag.getFlag()) {

                try {
                    // 每过10秒对TaskManager进行一次监听
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static String getRunningJobThreadId() { //监视iskandar 的live_service
        String cmdout = Getcmdinfo.getCmdResult("adb shell ps | grep iskandar");
       // u0_a98    14270 334   1853276 61940 SyS_epoll_ 7f782655fc S com.example.chs.iskandar_client
        //u0_a98    15728 334   1686472 40088 SyS_epoll_ 7f782655fc S com.example.chs.iskandar_client:live_service

       if(cmdout.contains("live_service")){
           Log.i("testlog", "心跳服务存活");
           return "alive";
       }else {
           Log.i("testlog", "心跳服务死亡");
           return "dead";
       }

    }

    public static void main(String[] args) {
        System.out.println(getRunningJobThreadId());
    }


}