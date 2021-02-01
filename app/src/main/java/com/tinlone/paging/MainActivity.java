package com.tinlone.paging;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.work.BackoffPolicy;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkContinuation;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import androidx.work.WorkQuery;

import com.google.common.util.concurrent.ListenableFuture;
import com.tinlone.paging.workers.RetryWorker;
import com.tinlone.paging.workers.SharedWorker;
import com.tinlone.paging.workers.SimpleWorker;
import com.tinlone.paging.workers.SimpleWorker2;
import com.tinlone.paging.workers.SimpleWorker3;
import com.tinlone.paging.workers.SimpleWorker4;
import com.tinlone.paging.workers.SimpleWorker5;
import com.tinlone.paging.workers.Task4DataWorker;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements LifecycleOwner, SharedPreferences.OnSharedPreferenceChangeListener {

    private Button bgTaskButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bgTaskButton = findViewById(R.id.button6);
        SharedPreferences sp = getApplicationContext().getSharedPreferences(SharedWorker.SHARED_KEY, MODE_PRIVATE);
        sp.registerOnSharedPreferenceChangeListener(this);
        postDataToUI();
    }

    /**
     * 简单任务
     *
     * @param view 按键
     */
    public void simpleTask(View view) {
        // 单次执行任务
        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(SimpleWorker.class)
                .addTag("OneTimeWorkRequest_simpleTask").build();
        // 单次执行任务2
        OneTimeWorkRequest workRequest1 = OneTimeWorkRequest.from(SimpleWorker.class);

        // 轮询任务
        WorkManager.getInstance(this).enqueue(workRequest);

//        ListenableFuture<WorkInfo> future = WorkManager.getInstance(this).getWorkInfoById(workRequest.getId());
//        future.addListener(() -> Utils.log(future), Runnable::run);
    }

    /**
     * 任务传递数据
     *
     * @param view 按键
     */
    public void transferData(View view) {
        Data data = new Data.Builder().putString("message", "MainActivity").build();

        // 单次执行任务
        OneTimeWorkRequest request1 = new OneTimeWorkRequest.Builder(Task4DataWorker.class)
                .setInputData(data).build();
        // 监听worker
        Utils.simpleWatch(this, request1);
        // 轮询任务
        WorkManager.getInstance(this).enqueue(request1);

    }

    /**
     * 多任务
     *
     * @param view 按键
     */
    public void multiTask(View view) {
        OneTimeWorkRequest workRequest2 = new OneTimeWorkRequest.Builder(SimpleWorker2.class).build();
        OneTimeWorkRequest workRequest3 = new OneTimeWorkRequest.Builder(SimpleWorker3.class).build();
        OneTimeWorkRequest workRequest4 = new OneTimeWorkRequest.Builder(SimpleWorker4.class).build();

        WorkManager.getInstance(this).beginWith(workRequest3)
                .then(workRequest2)
                .then(workRequest4)
                .enqueue();

    }

    /**
     * 多任务 - 任务链
     *
     * @param view 按键
     */
    @SuppressLint("EnqueueWork")
    public void workContinuation(View view) {
        Utils.log("workContinuation start");
        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(SimpleWorker.class).build();
        OneTimeWorkRequest workRequest2 = new OneTimeWorkRequest.Builder(SimpleWorker2.class).build();
        OneTimeWorkRequest workRequest3 = new OneTimeWorkRequest.Builder(SimpleWorker3.class).build();
        OneTimeWorkRequest workRequest4 = new OneTimeWorkRequest.Builder(SimpleWorker4.class).build();

        WorkContinuation continuation = WorkManager.getInstance(this)
                .beginWith(workRequest3)
                .then(workRequest2)
                .then(workRequest4);

        continuation.then(workRequest).enqueue();

        Utils.log("workContinuation end");
    }

    /**
     * 多任务-任务唯一性
     *
     * @param view 按键
     */
    public void workUnique(View view) {
        Utils.log("workUnique start");
        OneTimeWorkRequest workRequest5 = new OneTimeWorkRequest.Builder(SimpleWorker5.class)
                .addTag("workUnique").build();
        OneTimeWorkRequest workRequest2 = new OneTimeWorkRequest.Builder(SimpleWorker2.class)
                .addTag("OneTimeWorkRequest").build();

        Utils.simpleWatch(this, workRequest5);

        WorkManager.getInstance(this).beginUniqueWork("SimpleWorker",
//                ExistingWorkPolicy.REPLACE, workRequest5)
//                ExistingWorkPolicy.APPEND_OR_REPLACE, workRequest5)
//                ExistingWorkPolicy.APPEND, workRequest5)
                ExistingWorkPolicy.KEEP, workRequest5)
                .then(workRequest2)
                .enqueue();

        Utils.log("workUnique end");
    }


    public void cancelUnique(View view) {
        Utils.log("cancelUnique");
        WorkManager.getInstance(this).cancelAllWorkByTag("workUnique2_simpleTask");
//        WorkManager.getInstance(this).cancelAllWorkByTag("OneTimeWorkRequest");
//        WorkManager.getInstance(this).cancelAllWorkByTag("request3");
    }

    public void workUnique2(View view) {
        Utils.log("workUnique2 start");
        // 单次执行任务
        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(SimpleWorker.class)
                .addTag("workUnique2_simpleTask").build();
        OneTimeWorkRequest request3 = new OneTimeWorkRequest.Builder(SimpleWorker3.class)
                .addTag("request3").build();

        Utils.simpleWatch(this, workRequest);
        // 轮询任务
        WorkManager.getInstance(this).beginUniqueWork("SimpleWorker",
                ExistingWorkPolicy.REPLACE, workRequest).then(request3).enqueue();
        Utils.log("workUnique2 end");
    }

    /**
     * 重复周期任务
     * - 时间间隔小于 15分钟， 则强制被调整为15分钟
     * - 时间间隔大于15分钟，则以设置值为准
     *
     * @param view 按键
     */
    public void repeatTask(View view) {
        PeriodicWorkRequest request = new PeriodicWorkRequest
                .Builder(SimpleWorker.class, 16, TimeUnit.MINUTES)
                .addTag("PeriodicWorkRequest_16_MINUTES")
                .addTag("abc")
                .build();

        WorkManager.getInstance(this).enqueue(request);

        WorkQuery workQuery = WorkQuery.Builder
                .fromTags(Collections.singletonList("PeriodicWorkRequest_16_MINUTES"))
                .addStates(Arrays.asList(WorkInfo.State.ENQUEUED,
                        WorkInfo.State.FAILED,
                        WorkInfo.State.SUCCEEDED,
                        WorkInfo.State.CANCELLED))
                .build();

        Utils.executor().execute(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    ListenableFuture<List<WorkInfo>> workInfos = WorkManager.getInstance(MainActivity.this)
                            .getWorkInfos(workQuery);
                    try {
                        List<WorkInfo> infos = workInfos.get();
                        Utils.log(infos);
                        for (WorkInfo info : infos) {
                            if (info.getState() == WorkInfo.State.SUCCEEDED ||
                                    info.getState() == WorkInfo.State.CANCELLED) {
                                return;
                            }
                        }
                        Thread.sleep(1000L);
                    } catch (Exception e) {
                        Utils.log(e);
                    }
                }
            }
        });

    }

    /**
     * 测试约束
     *
     * @param view 按键
     */
    public void taskConstraint(View view) {
        Utils.log("taskConstraint start");
        // 任务约束
        Constraints networkConstraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.METERED) // 计量网络
                .build();
        // 单次执行任务
        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(SimpleWorker.class)
                .setConstraints(networkConstraints).build();
        // 监听worker
        Utils.simpleWatch(this, workRequest);
        // 轮询任务
        WorkManager.getInstance(this).enqueue(workRequest);
        Utils.log("taskConstraint end");
    }

    /**
     * 退避政策
     *
     * @param view 按键
     */
    public void retryTask(View view) {
        Constraints networkConstraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.METERED) // 计量网络
                .build();

        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(RetryWorker.class)
                .setBackoffCriteria(BackoffPolicy.LINEAR,
                        OneTimeWorkRequest.MIN_BACKOFF_MILLIS,
                        TimeUnit.MILLISECONDS)
                .setConstraints(networkConstraints)
                .build();
        WorkManager.getInstance(this).enqueue(workRequest);
    }

    public void backgroundTask(View view) {
        Utils.log("backgroundTask start");
        // 任务约束
        Constraints networkConstraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.METERED) // 计量网络
                .build();
        // 单次执行任务
        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(SharedWorker.class)
                .setConstraints(networkConstraints).build();
        // 监听worker
        Utils.simpleWatch(this, workRequest);
        // 轮询任务
        WorkManager.getInstance(this).enqueue(workRequest);
        Utils.log("backgroundTask end");
    }


    private void postDataToUI() {
        SharedPreferences sp = getApplicationContext().getSharedPreferences(SharedWorker.SHARED_KEY, MODE_PRIVATE);
        int count = sp.getInt(SharedWorker.COUNT, 0);
        bgTaskButton.setText("后台任务 - " + count);
    }

    public void jumpToB(View view) {
        startActivityForResult(new Intent(this, BActivity.class), 100);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (100 == requestCode && 100 == resultCode) {
            if (data != null) {
                String message = data.getStringExtra("message");
                Utils.log("onActivityResult :: " + message);
            }
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        postDataToUI();
    }


}