package com.tinlone.paging.workers;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.tinlone.paging.Utils;

public class SharedWorker extends Worker {
    public static final String SHARED_KEY = "com.tinlone.paging";
    public static final String COUNT = "share_count";

    public SharedWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {

        Utils.log("doWork start: " + getClass().getSimpleName());
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
            return Result.failure();
        }

        SharedPreferences sp = getApplicationContext().getSharedPreferences(SHARED_KEY, Context.MODE_PRIVATE);

        int count = sp.getInt(COUNT, 0);
        sp.edit().putInt(COUNT, ++count > 10 ? 0 : count).apply();

        Utils.log("doWork end: " + getClass().getSimpleName());

        return Result.success();
    }
}
