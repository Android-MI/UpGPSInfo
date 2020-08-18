package com.bmzy.gpsinfo.http;

import android.os.Handler;
import android.util.Log;

import com.bmzy.gpsinfo.http.HttpRequest.ResultListener;
import com.bmzy.gpsinfo.json.JSONObject;
import com.bmzy.gpsinfo.util.StringUtils;

import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

public class HttpSubtask {
    final static int TYPE_GET = 0x01;
    final static int TYPE_POST = 0X02;
    final static int TYPE_FILE = 0X03;

    boolean cancled;

    ResultListener listener;

    String url;

    int type;

    JSONObject params;

    Map<String, String> fields;

    Map<String, File> files;

    Handler mHandler;

    HttpSubtask(int type, String url, JSONObject params, ResultListener listener) {
        cancled = false;
        this.type = type;
        this.url = url;
        this.params = params;
        this.listener = listener;
    }

    HttpSubtask(String url, Map<String, String> fields, Map<String, File> files, ResultListener listener) {
        this.url = url;
        this.type = TYPE_FILE;
        this.fields = fields;
        this.files = files;
        this.listener = listener;
    }

    public void cancle() {
        cancled = true;
        listener = null;
    }

    public static String execute_get(String url) {

        HttpURLConnection connect = null;
        String result = null;

        try {
            String replaceUrl = url.replace(" ", "+");
            URL mUrl = new URL(replaceUrl);
            connect = (HttpURLConnection) mUrl.openConnection();
            connect.setConnectTimeout(20 * 1000);
            connect.setReadTimeout(20 * 1000);
            connect.connect();

            int response = connect.getResponseCode();

            if (response == 200) {
                InputStream is = connect.getInputStream();
                result = StringUtils.stringFromStream(is);
                is.close();
            }

        } catch (Exception e) {
            Log.i("info", "IOException:" + e.toString());

            e.printStackTrace();
            result = null;
        }

        if (connect != null) {
            connect.disconnect();
        }

        return result;
    }
}
