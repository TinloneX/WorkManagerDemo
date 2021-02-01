package com.tinlone.paging.workers;

import android.annotation.SuppressLint;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.tinlone.paging.Utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class RetryWorker extends Worker {

    SimpleDateFormat sdf;
    static int count = 0;
    static long lastMills = 0;

    @SuppressLint("SimpleDateFormat")
    public RetryWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        sdf = new SimpleDateFormat("yyyyMMdd HHmmssSSS");
    }

    private String data() {
        String text = "";
        long time = new Date().getTime();
        if (lastMills != 0) {
            text = "间隔时间(ms): " +  (time - lastMills) ;
        }

        lastMills = time;
        return sdf.format(lastMills)+" _ " + text;
    }

    @NonNull
    @Override
    public Result doWork() {
        Utils.log(data() + " _  doWork: " + getClass().getSimpleName());
        if (count++ < 5) {
            return Result.retry();
        } else {
            return Result.success();
        }
    }
}
