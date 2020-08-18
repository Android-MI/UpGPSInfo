package com.bmzy.gpsinfo.http;

import android.os.Handler;
import android.os.Looper;

import com.bmzy.gpsinfo.json.JSONObject;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HttpRequest {

    public interface ResultListener {
        public void onRightResult(String response);

        public void onErrorResult(String errorMsg);
    }

    final Handler mHandler;

    final ExecutorService executor;

    static HttpRequest instance;

    private HttpRequest() {
        mHandler = new Handler(Looper.getMainLooper());
        executor = Executors.newFixedThreadPool(3);
    }

    public static HttpRequest getInstance() {
        if (instance == null) {
            instance = new HttpRequest();
        }

        return instance;
    }

    public HttpSubtask execute_get(String url, ResultListener listener) {
        HttpSubtask task = new HttpSubtask(HttpSubtask.TYPE_GET, url, null, listener);
        task.mHandler = mHandler;
        //executor.execute(task.runnable);
        return task;
    }

    public HttpSubtask executePost(String url, JSONObject params, ResultListener listener) {

        HttpSubtask task = new HttpSubtask(HttpSubtask.TYPE_POST, url, params, listener);
        task.mHandler = mHandler;
        //executor.execute(task.runnable);
        return task;
    }
}
