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
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkContinuation;
import androidx.work.WorkManager;

import com.tinlone.paging.workers.SharedWorker;
import com.tinlone.paging.workers.SimpleWorker;
import com.tinlone.paging.workers.SimpleWorker2;
import com.tinlone.paging.workers.SimpleWorker3;
import com.tinlone.paging.workers.SimpleWorker4;
import com.tinlone.paging.workers.Task4DataWorker;

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
        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(SimpleWorker.class).build();
        OneTimeWorkRequest workRequest1 = OneTimeWorkRequest.from(SimpleWorker.class);

        // 监听worker
        Utils.simpleWatch(this, workRequest);
        // 轮询任务
        WorkManager.getInstance(this).enqueue(workRequest);
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

        WorkManager.getInstance(this).beginWith(workRequest2)
                .then(workRequest3)
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
        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(SimpleWorker.class).build();
        OneTimeWorkRequest workRequest2 = new OneTimeWorkRequest.Builder(SimpleWorker2.class).build();

        WorkManager.getInstance(this).beginUniqueWork("SimpleWorker",
                ExistingWorkPolicy.REPLACE, workRequest)
                .then(workRequest2)
                .enqueue();

        Utils.log("workUnique end");
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
                .Builder(SimpleWorker.class, 16, TimeUnit.MINUTES).build();

        WorkManager.getInstance(this).enqueue(request);
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
                .setRequiredNetworkType(NetworkType.METERED) // 网络通
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

    public void backgroundTask(View view) {
        Utils.log("backgroundTask start");
        // 任务约束
        Constraints networkConstraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.METERED) // 网络通
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


    private void postDataToUI(){
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