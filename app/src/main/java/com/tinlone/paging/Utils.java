package com.tinlone.paging;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LifecycleOwner;
import androidx.work.Data;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public final class Utils {

    public static final String TAG = "tin_lone_log";

    private Utils() {
        throw new IllegalStateException("Utils 不可被实例化");
    }

    private static class SingletonHolder {
        public static final Gson gson = new GsonBuilder().create();

        public static final ThreadPoolExecutor executor = new ThreadPoolExecutor(
                5, 5,
                0, TimeUnit.MILLISECONDS,
                new LinkedBlockingDeque<Runnable>()
        );
    }

    public static Gson gson() {
        return SingletonHolder.gson;
    }

    public static ThreadPoolExecutor executor(){
        return SingletonHolder.executor;
    }

    public static <T> T safety(T source, T another) {
        return source == null ? another : source;
    }

    public static void log(Object object) {
        log(null, object);
    }

    public static void log(String tag, Object object) {
        if (BuildConfig.DEBUG) {
            Log.i(safety(tag, TAG), valueOf(object));
        }
    }

    /**
     * 添加简单监听
     *
     * @param context     上下文及生命周期
     * @param workRequest worker
     * @param <T>         上下文且生命周期
     */
    public static <T extends Context & LifecycleOwner> void simpleWatch(T context, WorkRequest workRequest) {
        WorkManager.getInstance(context)
                .getWorkInfoByIdLiveData(workRequest.getId())
                .observe(context, workInfo -> {
                    if (workInfo==null){
                        Utils.log("有一个空的WorkInfo被监测到");
                        return;
                    }
                    Utils.log(workInfo);
//                    if (workInfo.getState().isFinished()) {
//                        Utils.log(workInfo.getOutputData());
//                    }
                    if (workInfo!=null){
                        if (workInfo.getState().isFinished()) {
                            String message = workInfo.getOutputData().getString("message");
                        }
                    }

                });
    }


    public static String valueOf(Object object) {
        if (object == null) {
            return "null";
        }
        String logText = "";
        try {
            if (object instanceof Throwable) {
                logText = getStackTraceString((Throwable) object);
            } else if (object instanceof String) {
                logText = String.valueOf(object);
            } else if (object instanceof Object[]) {
                logText = Arrays.toString((Object[]) object);
            } else {
                logText = gson().toJson(object);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return logText;
    }

    private static String getStackTraceString(Throwable tr) {
        if (tr == null) {
            return "";
        }
        Throwable t = tr;
        while (t != null) {
            if (t instanceof UnknownHostException) {
                return "";
            }
            t = t.getCause();
        }

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        tr.printStackTrace(pw);
        pw.flush();
        return sw.toString();
    }
}
