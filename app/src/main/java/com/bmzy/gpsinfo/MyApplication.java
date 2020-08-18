package com.bmzy.gpsinfo;

import android.app.Application;
import android.app.Service;
import android.os.Build;
import android.os.Vibrator;

import com.baidu.mapapi.CoordType;
import com.baidu.mapapi.SDKInitializer;
import com.bmzy.gpsinfo.service.LocationService;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class MyApplication extends Application {

    private static MyApplication mApplication;
    public LocationService locationService;
    public Vibrator mVibrator;

    /**
     * 操作人员id
     */
    public static int EXECUTOR_USER_ID = 0;

    @Override
    public void onCreate() {
        super.onCreate();
//        disableAPIDialog();

        /***
         * 初始化定位sdk，
         */
        locationService = new LocationService(getApplicationContext());
        mVibrator = (Vibrator) getApplicationContext().getSystemService(Service.VIBRATOR_SERVICE);
        SDKInitializer.initialize(getApplicationContext());
        SDKInitializer.setCoordType(CoordType.BD09LL);
    }

    public static MyApplication getApplication() {
        if (mApplication == null) {
            mApplication = new MyApplication();
        }
        return mApplication;
    }

    /**
     * 反射 禁止弹窗
     */
    private void disableAPIDialog() {
        if (Build.VERSION.SDK_INT < 28) return;
        try {
            Class clazz = Class.forName("android.app.ActivityThread");
            Method currentActivityThread = clazz.getDeclaredMethod("currentActivityThread");
            currentActivityThread.setAccessible(true);
            Object activityThread = currentActivityThread.invoke(null);
            Field mHiddenApiWarningShown = clazz.getDeclaredField("mHiddenApiWarningShown");
            mHiddenApiWarningShown.setAccessible(true);
            mHiddenApiWarningShown.setBoolean(activityThread, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
