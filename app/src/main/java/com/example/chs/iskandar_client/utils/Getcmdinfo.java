package com.example.chs.iskandar_client.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Created by chs on 17-7-5.
 */

public class Getcmdinfo {

    public static String getCmdResult(String cmd) {
        // CMD 执行命令
        String result =null;
        try {
            // 执行 CMD 命令
            Process process = Runtime.getRuntime().exec(cmd);

            // 从输入流中读取文本
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            // 构造一个写出流并指定输出文件保存路径
            //  FileWriter fw = new FileWriter(new File("/home/chs/CmdInfo.txt"));

            String line = null;

            // 循环读取
            while ((line = reader.readLine()) != null) {
                // 循环写入
                if(!line.isEmpty()){
                    result=result+line+"\n";
                }
            }
            System.out.println("程序执行完毕!");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

}


