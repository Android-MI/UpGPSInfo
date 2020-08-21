package com.bmzy.gpsinfo.bean;

import android.os.Environment;

public class Constants {

    public static final boolean IS_DEBUG = false;

    public static final String CONF_FILE = Environment.getExternalStorageDirectory() + "/com.bmzy.gpsinfo/constant.txt";

    // 青岛测试
    public static String API_DOMAIN = "wxtest.qd-metro.com:10017";
    // 青岛生产
    //public static String API_DOMAIN = "wx.qd-metro.com:10022";

    //本地测试
    //public static String API_DOMAIN = "192.168.15.107:8080/aems-pda-service";
    /**
     * 时间同步接口
     * 服务器返回一个Long类型的时间戳，变量名为dateTime
     */
    public static final String API_TIME_SYNC = "http://" + API_DOMAIN + "/wx/sys/getServiceDateTime";

    public static final String getServerTime(String ipAddress) {
        return "http://" + ipAddress + "/wx/sys/getServiceDateTime";
    }


    /**
     * 发送GPS信息接口
     * pdaMac:mac地址
     * longitude:经度
     * latitude:纬度
     * dateTime:时间（yyyy-MM-dd HH:mm:ss）
     * executorUserId 操作员id
     */
    public static final String API_GPS_INFO = "http://" + API_DOMAIN + "/wx/sys/getGPSInfo?pdaMac={0}&longitude={1}&latitude={2}&dateTime={3}&executorUserId={4}";
    
    public static final String getGpsInfo(String ipAddress) {
        return "http://" + ipAddress + "/wx/sys/getGPSInfo?pdaMac={0}&longitude={1}&latitude={2}&dateTime={3}&executorUserId={4}";
    }

    public static final String getUserList(String ipAddress) {
        return "http://" + ipAddress + "/wx/sys/getUserInfo";
    }
}
