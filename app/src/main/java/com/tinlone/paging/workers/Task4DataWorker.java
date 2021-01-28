package com.tinlone.paging.workers;

import android.annotation.SuppressLint;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.tinlone.paging.Utils;

public class Task4DataWorker extends Worker {

    public Task4DataWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @SuppressLint("RestrictedApi")
    @NonNull
    @Override
    public Result doWork() {
        String message = getInputData().getString("message");
        Utils.log("1111111111 Task2TaskWorker1 GET message: " + message);
        Data myMsg = new Data.Builder().putString("message", "11111111111 This is a message from Task2TaskWorker1").build();
        return new Result.Success(myMsg);
    }
}
