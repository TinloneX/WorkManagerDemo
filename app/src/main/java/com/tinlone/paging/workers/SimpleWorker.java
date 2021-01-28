package com.tinlone.paging.workers;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.tinlone.paging.Utils;

public class SimpleWorker extends Worker {


    public SimpleWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
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
        Utils.log("doWork end: " + getClass().getSimpleName());
        return Result.success();
    }
}
