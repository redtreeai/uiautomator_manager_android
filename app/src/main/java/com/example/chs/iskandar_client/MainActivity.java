package com.example.chs.iskandar_client;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.example.chs.iskandar_client.model.Data;
import com.example.chs.iskandar_client.service.GuardManager;
import com.example.chs.iskandar_client.service.TaskManager;
import com.example.chs.iskandar_client.utils.GetJarPathUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ListView manegeList;
    private Button scanBtn;
    private Button startBtn;
    private Button changeIDBtn;
    private Button exitBtn;
    private List<String> jarPaths = new ArrayList<>();


    public void getData() { //获取jar 包的方法
        GetJarPathUtil getJarPathUtil = new GetJarPathUtil();
        jarPaths = getJarPathUtil.getJarPaths();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);
        final Data app = (Data) getApplication();

        //第一次进入程序后立马启动taskmanamger service
        Intent inte = new Intent(this, TaskManager.class);
        Log.i("testlog", "心跳服务启动");
        startService(inte);

        manegeList = (ListView) this.findViewById(R.id.mlist);
        getData(); //获取jar包列表的方法
        exitBtn = (Button) findViewById(R.id.exit);
        exitBtn.setOnClickListener(new exitListener());
        scanBtn = (Button) findViewById(R.id.scan);
        scanBtn.setOnClickListener(new scanListener());
        startBtn = (Button) findViewById(R.id.start);
        startBtn.setOnClickListener(new startListener());
        changeIDBtn = (Button) findViewById(R.id.changeID);
        changeIDBtn.setOnClickListener(new changeIDListener());

        ArrayList<HashMap<String, Object>> mylist = new ArrayList<HashMap<String, Object>>();
        for (int i = 0; i < jarPaths.size(); i++) {
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("jarPaths", jarPaths.get(jarPaths.size() - 1 - i));
            String[] split = jarPaths.get(jarPaths.size() - 1 - i).split("/");
            String jarName = split[split.length - 1];
            map.put("jarNames", jarName);
            mylist.add(map);
        }
        //生成适配器，数组 ===》ListItem
        SimpleAdapter mSchedule = new SimpleAdapter(this, //上下文，就是这个Activity
                mylist,//数据来源
                R.layout.activity_item,//ListItem的XML实现
                new String[]{"jarPaths", "jarNames"},//动态数组与ListItem对应的子项
                new int[]{R.id.jarPath, R.id.jarName});//ListItem的XML文件里面的两个TextView ID
        manegeList.setAdapter(mSchedule);

        mSchedule.setViewBinder(new SimpleAdapter.ViewBinder() {
                                    @Override
                                    public boolean setViewValue(View view, Object data,
                                                                String textRepresentation) {
                                        // TODO Auto-generated method stub
                                        if (view instanceof ImageView && data instanceof Bitmap) {
                                            ImageView i = (ImageView) view;
                                            i.setImageBitmap((Bitmap) data);
                                            return true;
                                        }
                                        return false;
                                    }
                                }
        );

        manegeList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //启动uiautomator写在这里
                try {
                    String[] currentSplit = jarPaths.get(jarPaths.size() - position - 1).split("/");
                    String currentjarName = currentSplit[currentSplit.length - 1];
                      //列表启动任务是无参的，只能作为测试用
                    Runtime.getRuntime().exec(new String[]{"su", "-c", "uiautomator", "runtest", "/sdcard/jartmp/" + currentjarName, "-c", "com.xpspeed.uiautomator.task.MomoPrivateChatTask"}).waitFor();
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                    Log.i("testlog", "任务执行失败：" + e.getMessage());
                }
            }
        });
    }


    class scanListener implements View.OnClickListener { //添加了一个用以手动控制服务状态的按钮
        public void onClick(View v) {
            Data seviceflag = (Data) getApplication();
            if (seviceflag.getFlag()) {
                seviceflag.setFlag(false);
                Log.i("testlog", "关闭接口访问");
                scanBtn.setText("打开服务");
            }
            else if (!seviceflag.getFlag()) {
                seviceflag.setFlag(true);
                Log.i("testlog", "打开接口访问");
                scanBtn.setText("关闭服务");
            }
        }
    }

    class changeIDListener implements View.OnClickListener { //跳转到ID修改界面
        public void onClick(View v) {
            Log.i("testlog", "进入手机号修改模式");
            Intent intent = new Intent();
            intent.setClass(MainActivity.this, ChangeIdActivity.class);
            MainActivity.this.startActivity(intent);
        }
    }

    class startListener implements View.OnClickListener { //手动启动任务
        public void onClick(View v) {
            //启动列表里面的所有任务
            int position = 0;
            for (String jarpath : jarPaths) {
                try {
                    String[] currentSplit = jarPaths.get(jarPaths.size() - position - 1).split("/");
                    String currentjarName = currentSplit[currentSplit.length - 1];
                    Runtime.getRuntime().exec(new String[]{"su", "-c", "uiautomator", "runtest", "/sdcard/jartmp/" + currentjarName, "-c", "com.xpspeed.uiautomator.task.MomoPrivateChatTask"}).waitFor();
                    Log.i("testlog", "the" + position + ":task" + "complete");
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                    Log.i("testlog", "任务执行失败：" + e.getMessage());
                }
                position++;
            }

        }
    }

    class exitListener implements View.OnClickListener {
        public void onClick(View v) {
            //退出客户端
            MainActivity.this.finish();
        }
    }

    public void onBackPressed() {
        //code...... 绑定系统回退方法 同cancel 键
        MainActivity.this.finish();
    }


}
