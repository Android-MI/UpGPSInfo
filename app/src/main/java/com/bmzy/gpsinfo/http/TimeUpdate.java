package com.bmzy.gpsinfo.http;

import android.content.Context;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import com.bmzy.gpsinfo.bean.Constants;
import com.bmzy.gpsinfo.json.JSONObject;

public class TimeUpdate extends Thread {

    boolean exittask = false;

    Context mContext = null;

    Handler mHandler = new Handler();

    public TimeUpdate(Context context) {
        this.mContext = context;
    }

    public void exit() {
        exittask = true;
    }

    public void isleep(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        long timestamp = 0;

        while (!exittask) {
            try {
                String result = get(Constants.API_TIME_SYNC);
                if (result != null) {
                    JSONObject json = new JSONObject(result);
                    timestamp = json.getLong("dateTime");
                    exittask = true;
                    SystemClock.setCurrentTimeMillis(timestamp);
                }
            } catch (Exception e) {
                Log.i("info", "IOException:" + e.toString());
                isleep(5000);
            }
        }

        final long time = timestamp;

        Runnable r = new Runnable() {
            @Override
            public void run() {
                if (time > 0) {
                    Toast.makeText(mContext, "同步时间戳成功", Toast.LENGTH_LONG).show();
                }
            }
        };

        mHandler.post(r);
    }

    public String get(String url) throws Exception {
        String result = null;

        for (int i = 0; i < 3; i++) {
            result = HttpSubtask.execute_get(url);
            if (result != null) {
                break;
            }
        }

        if (result == null) {
            Log.i("info", "连接服务器失败" + url);
        }

        return result;
    }
}
