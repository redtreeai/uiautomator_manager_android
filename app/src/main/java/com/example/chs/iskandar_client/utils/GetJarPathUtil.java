package com.example.chs.iskandar_client.utils;

import android.annotation.SuppressLint;
import android.os.Environment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by redtree on 17-3-9.  //获取本地路径下的图片
 */
public class GetJarPathUtil {

    public List<String> getJarPaths() {

        List<String> jarPathList = new ArrayList<String>();
        String filePath = Environment.getExternalStorageDirectory().toString() + File.separator+ "jartmp";
        //String filePath = "/data/local/tmp";
        File fileAll = new File(filePath);
        File[] files = fileAll.listFiles();
        // 将所有的文件存入ArrayList中,并过滤其他格式的文件

        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            if (checkIsJarFile(file.getPath())) {
                jarPathList.add(file.getPath());
            }
        }
        // 返回得到的jar列表
        return jarPathList;
    }

    /**
     * 检查扩展名，得到jar格式的文件
     *
     * @param fName 文件名
     * @return
     */
    @SuppressLint("DefaultLocale")
    public boolean checkIsJarFile(String fName) {
        boolean isJarFile = false;
        // 获取扩展名
        String FileEnd = fName.substring(fName.lastIndexOf(".") + 1,
                fName.length()).toLowerCase();
        if (FileEnd.equals("jar")) {
            isJarFile = true;
        } else {
            isJarFile = false;
        }
        return isJarFile;
    }


}
