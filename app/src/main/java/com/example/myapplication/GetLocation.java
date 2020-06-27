package com.example.myapplication;

import android.os.Handler;
import android.os.Looper;

public class GetLocation extends Thread {

    public Handler handler;
    @Override
    public void run() {
        Looper.prepare();
        handler = new Handler();
        Looper.loop();
    }
}
