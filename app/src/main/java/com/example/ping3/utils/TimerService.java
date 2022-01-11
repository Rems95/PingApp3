package com.example.ping3.utils;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

import java.util.Timer;
import java.util.TimerTask;

public class TimerService extends Service {

    private Timer timer = null;
    private TimerTask timerTask = null;
    private int i = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        //start to count when be created
        if(timer==null&&timerTask==null){
            timer = new Timer();
            timerTask = new TimerTask() {
                @Override
                public void run() {
                    i++;
                    //broadcast every 1 second
                    Intent intent = new Intent();
                    intent.putExtra("time",i);
                    intent.setAction("com.demo.timer");
                    sendBroadcast(intent);
                }
            };
            timer.schedule(timerTask,0,1000);
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if(timerTask!=null){
            timerTask.cancel();
            timerTask=null;
        }
        if(timer!=null){
            timer.cancel();
            timer=null;
        }
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
