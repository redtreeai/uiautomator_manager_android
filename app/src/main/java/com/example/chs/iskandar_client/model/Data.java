package com.example.chs.iskandar_client.model;

import android.app.Application;

/**
 * Created by chs on 17-8-4.
 * flag:控制后台live-service是否访问服务端.
 */

public class Data extends Application{
    private Boolean flag;

    public Boolean getFlag() {
        return flag;
    }

    public void setFlag(Boolean flag) {
        this.flag = flag;
    }

    @Override
    public void onCreate(){
        flag = true;
        super.onCreate();
    }
}

