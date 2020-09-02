package com.bmzy.gpsinfo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.bmzy.gpsinfo.bean.Constants;
import com.bmzy.gpsinfo.http.GPSUpdate;
import com.bmzy.gpsinfo.json.JSONObject;
import com.bmzy.gpsinfo.service.LocationService;
import com.bmzy.gpsinfo.util.MacUtil;
import com.bmzy.gpsinfo.util.NotificationUtils;
import com.hdl.myhttputils.MyHttpUtils;
import com.hdl.myhttputils.bean.StringCallBack;

import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class GPSInfoActivity extends AppCompatActivity {

    /**
     * 服务器时间
     */
    TextView tvServerTime;
    /**
     * 经纬度信息
     */
    TextView tvLongAndLat;
    TextView tvLocStatus;
    /**
     * 开始-结束按钮
     */
    Button btnStart;

    Context mContext = null;

    long timestamp = 0L;
    int btn_state = 0;        //0表示未开始，1表示已开始

    private GPSUpdate gpsUpdate = null;
    private WakeLock mWakeLock = null;

    private LocationService locationService;
    private NotificationUtils mNotificationUtils;
    private Notification notification;
    private boolean isEnableLocInForeground = false;

    // 上传参数信息
    private String mac;
    private String gpsUrl = Constants.API_DOMAIN;
    private double latitude;
    private double longitude;

    private static boolean isExit = false;


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gps_info_activity);
        Toolbar toolbar = findViewById(R.id.toolBar);
        toolbar.setTitle("GpsInfo");
        setSupportActionBar(toolbar);
//        if (getSupportActionBar() != null) {
//            getSupportActionBar().setDisplayHomeAsUpEnabled(true);//这2行是设置返回按钮的
//            getSupportActionBar().setDisplayShowHomeEnabled(true);
//        }
        mContext = getApplication().getApplicationContext();
        mac = MacUtil.getMac(mContext);

        initViews();
        initLocation();
        getPersimmions();

    }

    private void initViews() {

        tvServerTime = findViewById(R.id.servicetime);
        tvLongAndLat = findViewById(R.id.tv_long_lat);
        tvLocStatus = findViewById(R.id.tv_lbs_status);

        btnStart = (Button) findViewById(R.id.btn_start);
        btnStart.setOnClickListener(v -> {
            uploadLocInfo();
        });
    }

    private void initLocation() {

        locationService = ((MyApplication) getApplication()).locationService;
        if (locationService != null && locationService.isStart()) {
            locationService.unregisterListener(mListener); //注销掉监听
            locationService.stop(); //停止定位服务
        }
        locationService.registerListener(mListener);
        locationService.setLocationOption(locationService.getDefaultLocationClientOption());

        //设置后台定位
        //android8.0及以上使用NotificationUtils
        if (Build.VERSION.SDK_INT >= 26) {
            mNotificationUtils = new NotificationUtils(this);
            Notification.Builder builder2 = mNotificationUtils.getAndroidChannelNotification("GpsInfo后台定位", "正在后台定位");
            notification = builder2.build();
        } else {
            //获取一个Notification构造器
            Notification.Builder builder = new Notification.Builder(GPSInfoActivity.this);
            Intent nfIntent = new Intent(GPSInfoActivity.this, GPSInfoActivity.class);

            builder.setContentIntent(PendingIntent.
                    getActivity(GPSInfoActivity.this, 0, nfIntent, 0)) // 设置PendingIntent
                    .setContentTitle("GpsInfo定位中") // 设置下拉列表里的标题
                    .setSmallIcon(R.mipmap.ic_launcher) // 设置状态栏内的小图标
                    .setContentText("正在定位") // 设置上下文内容
                    .setWhen(System.currentTimeMillis()); // 设置该通知发生的时间

            notification = builder.build(); // 获取构建好的Notification
        }
        notification.defaults = Notification.DEFAULT_SOUND; //设置为默认的声音
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        super.onNewIntent(intent);
    }

    @Override
    protected void onDestroy() {
        locationService.unregisterListener(mListener); //注销掉监听
        locationService.stop(); //停止定位服务
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(false);
//            exit();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(false);
        super.onBackPressed();
    }

    private void exit() {
        if (!isExit) {
            isExit = true;
            Toast.makeText(this, "在按一次退出程序", Toast.LENGTH_SHORT).show();
            new Timer().schedule(new TimerTask() {

                @Override
                public void run() {
                    isExit = false;
                }
            }, 2000);
        } else {
            MyApplication.getApplication().resetUserId();
            finish();
        }
    }


    /**
     * 上传定位信息
     */
    private void uploadLocInfo() {

        if (btn_state == 0) {
            locationService.start();

            acquireWakeLock();

            gpsUrl = Constants.getGpsInfo(Constants.API_DOMAIN);

            btnStart.setText("停止");
            btn_state = 1;

            //开启后台定位
            locationService.getClient().enableLocInForeground(1, notification);
            isEnableLocInForeground = true;

        } else if (btn_state == 1) {
            locationService.stop();
            releaseWakeLock();
            //gpsUpdate.exit();
            btnStart.setText("开始");
            btn_state = 0;

            //关闭后台定位（true：通知栏消失；false：通知栏可手动划除）
            locationService.getClient().disableLocInForeground(true);
            isEnableLocInForeground = false;
            goLoginActivity();
        }
    }

    private void goLoginActivity() {
        MyApplication.getApplication().resetUserId();
        Intent intent = new Intent(GPSInfoActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    //申请设备电源锁
    @SuppressLint("InvalidWakeLockTag")
    private void acquireWakeLock() {
        if (null == mWakeLock) {
            PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, "MyWakelockTag");
            if (null != mWakeLock) {
                mWakeLock.acquire();
            }
        }
    }

    //释放设备电源锁
    private void releaseWakeLock() {
        if (null != mWakeLock) {
            mWakeLock.release();
            mWakeLock = null;
        }
    }


    /*****
     *
     * 定位结果回调
     */
    private BDAbstractLocationListener mListener = new BDAbstractLocationListener() {

        /**
         * 定位请求回调函数
         * @param location 定位结果
         */
        @Override
        public void onReceiveLocation(BDLocation location) {

            if (null != location && location.getLocType() != BDLocation.TypeServerError) {
                int tag = 1;

                longitude = location.getLongitude();
                latitude = location.getLatitude();

                String locationTime = location.getTime();

                StringBuffer sb = new StringBuffer(256);
                sb.append("time : ");
                /**
                 * location.getTime() 是指服务端出本次结果的时间，如果位置不发生变化，则时间不变
                 */
                sb.append(location.getTime());
                sb.append("\nlocType : ");// 定位类型
                sb.append(location.getLocType());
                sb.append("\nlocType description : ");// *****对应的定位类型说明*****
                sb.append(location.getLocTypeDescription());
                sb.append("\nlatitude : ");// 纬度
                sb.append(location.getLatitude());
                sb.append("\nlongtitude : ");// 经度
                sb.append(location.getLongitude());
                sb.append("\nCountry : ");// 国家名称
                sb.append(location.getCountry());
                sb.append("\nProvince : ");// 获取省份
                sb.append(location.getProvince());
                sb.append("\ncity : ");// 城市
                sb.append(location.getCity());
                sb.append("\nDistrict : ");// 区
                sb.append(location.getDistrict());
                sb.append("\nTown : ");// 获取镇信息
                sb.append(location.getTown());
                sb.append("\nStreet : ");// 街道
                sb.append(location.getStreet());
                sb.append("\naddr : ");// 地址信息
                sb.append(location.getAddrStr());
                sb.append("\nlocationdescribe: ");
                sb.append(location.getLocationDescribe());// 位置语义化信息
                sb.append("\nversion: " + locationService.getSDKVersion()); // 获取SDK版本
                if (location.getLocType() == BDLocation.TypeGpsLocation) {// GPS定位结果
                    sb.append("\nspeed : ");
                    sb.append(location.getSpeed());// 速度 单位：km/h
                    sb.append("\nsatellite : ");
                    sb.append(location.getSatelliteNumber());// 卫星数目
                    sb.append("\nheight : ");
                    sb.append(location.getAltitude());// 海拔高度 单位：米
                    sb.append("\ngps status : ");
                    sb.append(location.getGpsAccuracyStatus());// *****gps质量判断*****
                    sb.append("\ndescribe : ");
                    sb.append("gps定位成功");
                } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {// 网络定位结果
                    // 运营商信息
                    if (location.hasAltitude()) {// *****如果有海拔高度*****
                        sb.append("\nheight : ");
                        sb.append(location.getAltitude());// 单位：米
                    }
                    sb.append("\noperationers : ");// 运营商信息
                    sb.append(location.getOperators());
                    sb.append("\ndescribe : ");
                    sb.append("网络定位成功");
                } else if (location.getLocType() == BDLocation.TypeOffLineLocation) {// 离线定位结果
                    sb.append("\ndescribe : ");
                    sb.append("离线定位成功，离线定位结果也是有效的");
                } else if (location.getLocType() == BDLocation.TypeServerError) {
                    sb.append("\ndescribe : ");
                    sb.append("服务端网络定位失败，可以反馈IMEI号和大体定位时间到loc-bugs@baidu.com，会有人追查原因");
                } else if (location.getLocType() == BDLocation.TypeNetWorkException) {
                    sb.append("\ndescribe : ");
                    sb.append("网络不同导致定位失败，请检查网络是否通畅");
                } else if (location.getLocType() == BDLocation.TypeCriteriaException) {
                    sb.append("\ndescribe : ");
                    sb.append("无法获取有效定位依据导致定位失败，一般是由于手机的原因，处于飞行模式下一般会造成这种结果，可以试着重启手机");
                }
                logMsg(sb.toString(), tag);


                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date date;
                try {
                    date = format.parse(locationTime);
                    timestamp = date.getTime();
                } catch (ParseException e) {
                    timestamp = System.currentTimeMillis();
                }
                Log.e("定位时间：", timestamp + "");
                uploadLbsInfo();
            }
        }

        @Override
        public void onConnectHotSpotMessage(String s, int i) {
            super.onConnectHotSpotMessage(s, i);
        }

        /**
         * 回调定位诊断信息，开发者可以根据相关信息解决定位遇到的一些问题
         * @param locType 当前定位类型
         * @param diagnosticType 诊断类型（1~9）
         * @param diagnosticMessage 具体的诊断信息释义
         */
        @Override
        public void onLocDiagnosticMessage(int locType, int diagnosticType, String diagnosticMessage) {
            super.onLocDiagnosticMessage(locType, diagnosticType, diagnosticMessage);
            int tag = 2;
            StringBuffer sb = new StringBuffer(256);
            sb.append("诊断结果: ");
            if (locType == BDLocation.TypeNetWorkLocation) {
                if (diagnosticType == 1) {
                    sb.append("网络定位成功，没有开启GPS，建议打开GPS会更好");
                    sb.append("\n" + diagnosticMessage);
                } else if (diagnosticType == 2) {
                    sb.append("网络定位成功，没有开启Wi-Fi，建议打开Wi-Fi会更好");
                    sb.append("\n" + diagnosticMessage);
                }
            } else if (locType == BDLocation.TypeOffLineLocationFail) {
                if (diagnosticType == 3) {
                    sb.append("定位失败，请您检查您的网络状态");
                    sb.append("\n" + diagnosticMessage);
                }
            } else if (locType == BDLocation.TypeCriteriaException) {
                if (diagnosticType == 4) {
                    sb.append("定位失败，无法获取任何有效定位依据");
                    sb.append("\n" + diagnosticMessage);
                } else if (diagnosticType == 5) {
                    sb.append("定位失败，无法获取有效定位依据，请检查运营商网络或者Wi-Fi网络是否正常开启，尝试重新请求定位");
                    sb.append(diagnosticMessage);
                } else if (diagnosticType == 6) {
                    sb.append("定位失败，无法获取有效定位依据，请尝试插入一张sim卡或打开Wi-Fi重试");
                    sb.append("\n" + diagnosticMessage);
                } else if (diagnosticType == 7) {
                    sb.append("定位失败，飞行模式下无法获取有效定位依据，请关闭飞行模式重试");
                    sb.append("\n" + diagnosticMessage);
                } else if (diagnosticType == 9) {
                    sb.append("定位失败，无法获取任何有效定位依据");
                    sb.append("\n" + diagnosticMessage);
                }
            } else if (locType == BDLocation.TypeServerError) {
                if (diagnosticType == 8) {
                    sb.append("定位失败，请确认您定位的开关打开状态，是否赋予APP定位权限");
                    sb.append("\n" + diagnosticMessage);
                }
            }
            logMsg(sb.toString(), tag);
        }
    };

    public void logMsg(final String str, final int tag) {

        try {
            if (tvLongAndLat != null) {
                new Thread(() -> tvLongAndLat.post(() -> {
                    if (tag == 1) {
                        tvLongAndLat.setText(str);
                    } else if (tag == 2) {
                        tvLocStatus.setText(str);
                    }
                })).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getServerTime() {

        String timeURL = Constants.getServerTime(Constants.API_DOMAIN);

        MyHttpUtils.build()
                .url(timeURL)
                .onExecute(new StringCallBack() {
                    @Override
                    public void onSucceed(String result) {
                        JSONObject json = new JSONObject(result);
                        timestamp = json.getLong("dateTime");
//                SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//                Date date = new Date(timestamp);
//                Log.e("time=",MessageFormat.format("服务器时间：{0}", sf.format(date)));
                    }

                    @Override
                    public void onFailed(Throwable throwable) {
                        timestamp = System.currentTimeMillis();
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }

    private void uploadLbsInfo() {
        String uploadInfoUrl = MessageFormat.format(gpsUrl, mac, longitude, latitude, new Long(timestamp).toString(), MyApplication.EXECUTOR_USER_ID);
        Log.e("上传url=", uploadInfoUrl);
        MyHttpUtils.build()
                .url(uploadInfoUrl)
                .onExecute(new StringCallBack() {
                    @Override
                    public void onSucceed(String result) {
                        tvLocStatus.setText(MessageFormat.format("上传结果：{0}", result));
                    }

                    @Override
                    public void onFailed(Throwable throwable) {
                        tvLocStatus.setText(MessageFormat.format("上传失败：{0}", throwable.getMessage()));

                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }

    private String permissionInfo;
    private final int SDK_PERMISSION_REQUEST = 127;

    @TargetApi(23)
    private void getPersimmions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ArrayList<String> permissions = new ArrayList<String>();
            /***
             * 定位权限为必须权限，用户如果禁止，则每次进入都会申请
             */
            // 定位精确位置
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
            }
            if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
            }
            /*
             * 读写权限和电话状态权限非必要权限(建议授予)只会申请一次，用户同意或者禁止，只会弹一次
             */
            // 读写权限
            if (addPermission(permissions, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                permissionInfo += "Manifest.permission.WRITE_EXTERNAL_STORAGE Deny \n";
            }
            if (permissions.size() > 0) {
                requestPermissions(permissions.toArray(new String[permissions.size()]), SDK_PERMISSION_REQUEST);
            }
        }
    }

    @TargetApi(23)
    private boolean addPermission(ArrayList<String> permissionsList, String permission) {
        // 如果应用没有获得对应权限,则添加到列表中,准备批量申请
        if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
            if (shouldShowRequestPermissionRationale(permission)) {
                return true;
            } else {
                permissionsList.add(permission);
                return false;
            }
        } else {
            return true;
        }
    }

    @TargetApi(23)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
