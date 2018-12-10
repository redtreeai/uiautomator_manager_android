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
 * 程序启动后启动此服务进行服务端交互
 */

public class TaskManager extends Service {
    private MyThread myThread;
    private String dftaskName = "空闲";
    private String dfconfigVersion = "1";
    private String dfspeakId = "456";
    private String dftype = "text";
    private String dfspeak = "你好";
    private String dfexecId = "null";
    private Boolean dfreportIsSuccess = false;
    private String dfreportMsg = "no";
    private String reportUrl = "http://cloud.gogobdp.com:9095/api/job/report";
    private String dfsex="null";
    private String dfclassname="未匹配";


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        this.myThread = new MyThread();
        this.myThread.start();
        super.onCreate();
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
                Log.i("testlog", "向服务端发送请求");
                if(getRunningJobThreadId().equals("null")){
                    dftaskName="空闲"; //访问心跳接口前确认任务是否执行
                }
               Log.i("testlog", "检查uiautomator是否卡死");
                //   com.immomo.momo:id/citycard_close
              if(getRunningJobStatus().equals("pipe_wait")){
                    Log.i("testlog", "uiautomator进程卡死,正在重启");
                    try {
                        Runtime.getRuntime().exec(new String[]{"su", "-c", "kill", "-9", getRunningJobThreadId()}).waitFor();
                        Log.i("testlog", "uiautomator已杀死，3秒后重启");
                        Thread.sleep(3000);
                        Runtime.getRuntime().exec(new String[]{"su", "-c", "uiautomator", "runtest", "/sdcard/jartmp/momo-1.jar","-e","sex",dfsex,"-e","type",dftype,"-e","executionId",dfexecId,"-e","deviceId",getPhoneNumber(),"-e","speakId",dfspeakId, "-c","com.xpspeed.uiautomator.task."+dfclassname}).waitFor();
                        Log.i("testlog", "uiautomator重启成功");
                    }catch (IOException | InterruptedException e){
                        Log.i("testlog", "uiautomator停止失败："+e.getMessage());
                    }

                }else {
                    Log.i("testlog", "uiautomator进程正常运行");
                }
                try {
                    // 每个10秒向服务端heartbeat接口发送一次请求
                    Thread.sleep(7000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                String heartbeatUrl = "http://cloud.gogobdp.com:9095/api/heartbeat";
                heartbeat(heartbeatUrl + "?deviceId=" + getPhoneNumber() + "&name=" + dftaskName + "&configVersion=" + dfconfigVersion);
                Log.i("testlog", "deviceid为:" + getPhoneNumber());
                Log.i("testlog", "正在执行的uiautomator进程为" + getRunningJobThreadId());
            }
        }
    }

    private static String getSerialNumber() {
        String serial = null;
        try {  //设备序列号获取，已经弃用
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class);
            serial = (String) get.invoke(c, "ro.serialno");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return serial;
    }

    private static String getPhoneNumber() {
        try { //生成文本记录自定义设备号
            File urlFile = new File("/sdcard/cloudset/phonenumber.txt");
            if (!urlFile.exists()) {
                return "can not get phoneNumber";
            }
            InputStreamReader isr = new InputStreamReader(new FileInputStream(urlFile), "UTF-8");
            BufferedReader br = new BufferedReader(isr);
            String str = "";
            String mimeTypeLine = null;
            while ((mimeTypeLine = br.readLine()) != null) {
                str = str + mimeTypeLine;
            }
            return str;
        } catch (IOException e) {
            e.getMessage();
            Log.i("testlog", "can not get phoneNumber");
            return "can not get phoneNumber";
        }
    }

    private static String getRunningJobThreadId() { //查询正在执行的uiautomator任务进程id
        String cmdout = Getcmdinfo.getCmdResult("ps | grep uiautomator");
        //root      5440  5439  1912432 42232 futex_wait 0000000000 S uiautomator
        String pid = "null";
        if (cmdout.split("\\s{1,}").length > 8) {
            pid = cmdout.split("\\s{1,}")[9];
        } else {
            pid = "null";
        }
        return pid;
    }

    private static String getRunningJobStatus() { //查询正在执行的uiautomator任务进程id
        String cmdout = Getcmdinfo.getCmdResult("ps | grep uiautomator");
        //root      5440  5439  1912432 42232 futex_wait 0000000000 S uiautomator
        String status = "null";
        if (cmdout.split("\\s{1,}").length > 8) {
            status = cmdout.split("\\s{1,}")[13];
        } else {
            status = "null";
        }
        return status;
    }

    // 在子线程发起网络请求
    private void heartbeat(String url) {
        // 创建请求客户端

        OkHttpClient okHttpClient = new OkHttpClient();

        // 创建请求参数
        Request request = new Request.Builder().url(url).build();

        // 创建请求对象
        Call call = okHttpClient.newCall(request);

        // 发起异步的请求
        call.enqueue(new Callback() {
            @Override
            // 请求发生异常
            public void onFailure(Call call, IOException e) {
                Log.i("testlog", "服务端无响应");
            }

            @Override
            // 获取到服务器数据。注意：即使是 404 等错误状态也是获取到服务器数据
            public void onResponse(Call call, Response response) throws IOException {

                Log.i("testlog", "response");

                if (response.isSuccessful()) {
                    String result = response.body().string();
                    if (result != null) {
                        Log.i("testlog", "成功向服务端上报数据");
                        processCommand(result);
                    }

                } else {
                    Log.i("testlog", "服务端无响应");
                    String result = response.body().string();
                    processCommand(result);
                }
            }
        });
    }

    // 通过访问speak接口 返回uiautomator参数
    private void speakId(String url) {
        // 创建请求客户端
        Log.i("testlog", "正在访问speakId");

        OkHttpClient okHttpClient = new OkHttpClient();

        // 创建请求参数
        Request request = new Request.Builder().url(url).build();

        // 创建请求对象
        Call call = okHttpClient.newCall(request);

        // 发起异步的请求
        call.enqueue(new Callback() {
            @Override
            // 请求发生异常
            public void onFailure(Call call, IOException e) {
                Log.i("testlog", "speakid服务端无响应");
            }

            @Override
            // 获取到服务器数据。注意：即使是 404 等错误状态也是获取到服务器数据
            public void onResponse(Call call, Response response) throws IOException {

                Log.i("testlog", "speakid response");

                if (response.isSuccessful()) {
                    String result = response.body().string();
                    if (result != null) {
                        Log.i("testlog", "成功向speakid接口上报数据");
                        JSONObject jsonObj = JSON.parseObject(result);

                        if (jsonObj == null) {
                            Log.i("testlog", "speakid无返回数据json");
                            return;
                        }

                        String type = jsonObj.getString("type");
                        Log.i("testlog", type);
                        String speak = jsonObj.getString("speak");
                        Log.i("testlog", speak);

                        dfspeak = speak;
                        dftype = type;
                        Log.i("testlog", result);
                    }

                } else {
                    Log.i("testlog", "speakid 服务端无响应");
                    String result = response.body().string();
                    JSONObject jsonObj = JSON.parseObject(result);

                    if (jsonObj == null) {
                        Log.i("testlog", "speakid无返回数据json");
                        return;
                    }
                    String type = jsonObj.getString("type");
                    String speak = jsonObj.getString("speak");

                    dfspeak = speak;
                    dftype = type;
                    Log.i("testlog", result);
                }
            }
        });
    }


    void processCommand(String result) {   //解析heartbeat接口返回的参数
        // {"id":"123","platform":"momo","version":"1.0","task":"task","command":"start","SpeakId":null}
        Log.i("testlog", "processs command start");

        JSONObject jsonObj = JSON.parseObject(result);
        JSONObject configObj = jsonObj.getJSONObject("config");
        Log.i("testlog", "获取heartbeat参数");
        if (configObj != null) {
            String version = configObj.getString("configVersion");
            dfconfigVersion = (version);
        }
        JSONObject commandObj = jsonObj.getJSONObject("commandContent");
        String command = "null";
        if (commandObj != null) {
            command = commandObj.getString("command");
            String speakId = commandObj.getString("speakId");
            String taskName = commandObj.getString("name");
            String executionId = commandObj.getString("execId");
            String className = commandObj.getString("task");
            JSONObject paramObj = commandObj.getJSONObject("params");

            if(paramObj!=null){
                JSONArray conditionArray =paramObj.getJSONArray("condition");
                if (conditionArray!=null){
                    for (int i = 0; i < conditionArray.size(); i++) {
                        JSONObject obj = conditionArray.getJSONObject(i);
                        switch (obj.getString("key")){
                            case "男":
                                dfsex="男";
                                break;
                            case "女":
                                dfsex="女";
                                break;
                        }
                    }
                }else {
                    dfsex="null";
                }
            }else {
                dfsex="null";
            }

            dfclassname=className; //根据包名启动对应的任务
            dftaskName = taskName;
            dfspeakId = speakId;
            dfexecId = executionId;
            Log.i("testlog", "获取speakId参数:--" + speakId + "taskname:--" + taskName + "executionId:--" + executionId+"speakSex:"+dfsex);
        }

        switch (command) {
            case "start":
                try {
                    if (getRunningJobThreadId().equals("null")) {

                        speakId("http://cloud.gogobdp.com:9095/api/job/speak" + "?speakId=" + dfspeakId);
                        Log.i("testlog", "speakid为" + dfspeakId + "type为" + dftype + "speak为" + dfspeak);

                        while (dfspeak.equals("你好")) {
                            try {
                                Thread.sleep(5000); //异步返回需要等待
                                Log.i("testlog", "等待speak返回数据");
                                speakId("http://cloud.gogobdp.com:9095/api/job/speak" + "?speakId=" + dfspeakId);
                                Log.i("testlog", "speakid为" + dfspeakId + "type为" + dftype + "speak为" + dfspeak);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        postToReoprtThread(reportUrl, "start", "任务:" + dftaskName + "已启动");
                        Log.i("testlog", "任务启动前上报report");
                        while (!dfreportMsg.equals("ok")) {
                            try {
                                Thread.sleep(2000); //异步返回需要等待
                                postToReoprtThread(reportUrl, "start", "任务:" + dftaskName + "已启动");
                                Log.i("testlog", "等待report返回数据");
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                                Log.i("testlog", "任务启动前上报report失败");
                            }
                        }
                        Log.i("testlog", "任务启动前上报report成功");
                         //"-e","type",dftype,"-e","executionId",dfexecId,"-e","speak",dfspeak,"-e","deviceId",getPhoneNumber(),"-e","speakId",dfspeakId,

                        Log.i("testlog","exeId:"+dfexecId+"speak:"+dfspeak+"type:"+dftype+"deviceId:"+getPhoneNumber()+"speakId:"+dfspeakId);
                        //"-e","sex",dfsex,"-e","type",dftype,"-e","executionId",dfexecId,"-e","deviceId",getPhoneNumber(),"-e","speakId",dfspeakId
                        if(dfclassname.equals("未匹配")){
                            Log.i("testlog", "任务类名匹配失败或未匹配");
                        }else {
                          final  Process process =  Runtime.getRuntime().exec(new String[]{"su", "-c", "uiautomator", "runtest", "/sdcard/jartmp/momo-1.jar","-e","sex",dfsex,"-e","type",dftype,"-e","executionId",dfexecId,"-e","deviceId",getPhoneNumber(),"-e","speakId",dfspeakId, "-c","com.xpspeed.uiautomator.task."+dfclassname});
                            Log.i("testlog", "start to run cmd");
                            //处理InputStream的线程
                            new Thread()
                            {
                                @Override
                                public void run()
                                {
                                    BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
                                    String line = null;

                                    try
                                    {
                                        while((line = in.readLine()) != null)
                                        {
                                            System.out.println("output: " + line);
                                        }
                                    }
                                    catch (IOException e)
                                    {
                                        e.printStackTrace();
                                    }
                                    finally
                                    {
                                        try
                                        {
                                            in.close();
                                        }
                                        catch (IOException e)
                                        {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }.start();

                            new Thread()
                            {
                                @Override
                                public void run()
                                {
                                    BufferedReader err = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                                    String line = null;

                                    try
                                    {
                                        while((line = err.readLine()) != null)
                                        {
                                            System.out.println("err: " + line);
                                        }
                                    }
                                    catch (IOException e)
                                    {
                                        e.printStackTrace();
                                    }
                                    finally
                                    {
                                        try
                                        {
                                            err.close();
                                        }
                                        catch (IOException e)
                                        {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }.start();
                            process.waitFor();
                            Log.i("testlog", "job" + dftaskName + "start");
                        }
                    }
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                    Log.i("testlog", "execution false");
                }

                break;
            case "stop":
                try {
                    if (!getRunningJobThreadId().equals("null")) {
                        Runtime.getRuntime().exec(new String[]{"su", "-c", "kill", "-9", getRunningJobThreadId()}).waitFor();
                        Log.i("testlog", "job" + dftaskName + "stop");
                        postToReoprtThread(reportUrl, "stop", "任务:" + dftaskName + "已被手动停止");
                        dftaskName="空闲";
                    }else {
                        dftaskName="空闲";
                        Log.i("testlog", "job" + dftaskName + "stop");
                        postToReoprtThread(reportUrl, "stop", "任务:" + dftaskName + "已被手动停止");
                    }
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                    Log.i("testlog", "execution stop false");
                }

                break;

            default:
                break;
        }


        Log.i("testlog", result);
    }

     private void postToReoprtThread(String reportUrl,String status,String msg){ //report接口上报
         StringBuilder sb = new StringBuilder();
         sb.append("{");
         sb.append("\"execId\"").append(":\"").append(dfexecId).append("\",");
         sb.append("\"deviceId\"").append(":\"").append(getPhoneNumber()).append("\",");
         sb.append("\"status\"").append(":\"").append(status).append("\",");
         sb.append("\"msg\"").append(":\"").append(msg).append("\"}");
         String poresutl = HttpRequest.sendPost(reportUrl, sb.toString());
         Log.i("testlog", poresutl);
         JSONObject rejson=JSON.parseObject(poresutl);
         if(rejson!=null){
             dfreportMsg=rejson.getString("msg");
             dfreportIsSuccess=rejson.getBoolean("success");
             Log.i("testlog", "report返回结果"+"msg:"+dfreportMsg+"success:"+dfreportIsSuccess);
         }
     }

    /**
     * 下载远程文件并保存到本地
     *
     * @param remoteFilePath 远程文件路径
     * @param localFilePath  本地文件路径
     */
    public void downloadFile(String remoteFilePath, String localFilePath) { //任务启动前若需要下载文件到本地，则调用此方法
        URL urlfile;
        urlfile = null;
        HttpURLConnection httpUrl = null;
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        File f = new File(localFilePath);
        try {
            urlfile = new URL(remoteFilePath);
            httpUrl = (HttpURLConnection) urlfile.openConnection();
            httpUrl.connect();
            bis = new BufferedInputStream(httpUrl.getInputStream());
            bos = new BufferedOutputStream(new FileOutputStream(f));
            int len = 2048;
            byte[] b = new byte[len];
            while ((len = bis.read(b)) != -1) {
                bos.write(b, 0, len);
            }
            bos.flush();
            bis.close();
            httpUrl.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                bis.close();
                bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}