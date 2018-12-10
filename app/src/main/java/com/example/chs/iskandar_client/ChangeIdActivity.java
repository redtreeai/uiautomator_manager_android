package com.example.chs.iskandar_client;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.File;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;

public class ChangeIdActivity extends AppCompatActivity { //自定义设备ID的控制器

    private Button changeBtn;
    private Button backBtn;
    private EditText phonen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_changeid);

        changeBtn = (Button) findViewById(R.id.inputnumber);
        changeBtn.setOnClickListener(new changeIDListener());
        backBtn = (Button) findViewById(R.id.backtomain);
        backBtn.setOnClickListener(new exitListener());
        phonen = (EditText) findViewById(R.id.phonenumber);

    }



    class changeIDListener implements View.OnClickListener {
        public void onClick(View v) {
            String number = phonen.getText().toString();
            write(number);
            Log.i("testlog", "IDchange success"+number);
        }
    }


    class exitListener implements View.OnClickListener {
        public void onClick(View v) {
            //退出客户端
            ChangeIdActivity.this.finish();
        }
    }
    public void onBackPressed() {
        //code...... 绑定系统回退方法 同cancel 键
        ChangeIdActivity.this.finish();
    }

    public static void write(String content) {
        try {
            //判断实际是否有SD卡，且应用程序是否有读写SD卡的能力，有则返回true
            if (Environment.getExternalStorageState().equals(
                    Environment.MEDIA_MOUNTED)) {
                // 获取SD卡的目录
                String path = "/sdcard/cloudset/";
                File dir = new File(path);
                if (!dir.exists()) {
                    dir.mkdirs();
                }

                //使用RandomAccessFile是在原有的文件基础之上追加内容，
                //而使用outputstream则是要先清空内容再写入
                FileOutputStream file = new FileOutputStream(path+"phonenumber.txt");
                file.write(content.getBytes());
                file.flush();
                file.close();
                //File targetFile = new File(path+"phonenumber.txt");
                /*RandomAccessFile raf = new RandomAccessFile(targetFile, "rw");
                //光标移到原始文件最后，再执行写入

                raf.seek(targetFile.length());
                raf.write(content.getBytes());
                raf.close();*/
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
