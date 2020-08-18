package com.bmzy.gpsinfo.http;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.util.Log;

import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.bmzy.gpsinfo.util.MacUtil;
import com.bmzy.gpsinfo.util.MyLocationListener;
import com.bmzy.gpsinfo.util.NetException;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class GPSUpdate extends Thread{
	
	boolean	exittask	= false;
	
	Context	mContext	= null;
	
	Handler	mHandler	= new Handler();
	
	String gpsUrl		= "";

	public LocationClient mLocationClient = null;
	private MyLocationListener myListener = new MyLocationListener();
	//private Location location 	= null;
	
	public GPSUpdate(Context context, String gpsUrl)
	{
		this.mContext = context;
		this.gpsUrl = gpsUrl;
	}
	
	public void exit()
	{
		exittask = true;
		mLocationClient.stop();
	}
	public void isleep(long time)
	{
		try
		{
			Thread.sleep(time);
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}

	/*private void setLocation(Location location) {
		// TODO Auto-generated method stub
		this.location = location;
	}*/
	//发送GPS信息
	@Override
	public void run()
	{
		String mac = MacUtil.getMac(mContext);
		mLocationClient = new LocationClient(mContext);     
	    //声明LocationClient类
	    mLocationClient.registerLocationListener(myListener);    
	    //注册监听函数
		SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		LocationClientOption option = new LocationClientOption();

		option.setLocationMode(LocationMode.Hight_Accuracy);
		//可选，设置定位模式，默认高精度
		//LocationMode.Hight_Accuracy：高精度；
		//LocationMode. Battery_Saving：低功耗；
		//LocationMode. Device_Sensors：仅使用设备；
			
		option.setCoorType("bd09ll");
		//可选，设置返回经纬度坐标类型，默认GCJ02
		//GCJ02：国测局坐标；
		//BD09ll：百度经纬度坐标；
		//BD09：百度墨卡托坐标；
		//海外地区定位，无需设置坐标类型，统一返回WGS84类型坐标
		 	
		option.setScanSpan(5 * 1000);
		//可选，设置发起定位请求的间隔，int类型，单位ms
		//如果设置为0，则代表单次定位，即仅定位一次，默认为0
		//如果设置非0，需设置1000ms以上才有效
			
		option.setOpenGps(true);
		//可选，设置是否使用gps，默认false
		//使用高精度和仅用设备两种定位模式的，参数必须设置为true
			
		option.setLocationNotify(true);
		//可选，设置是否当GPS有效时按照1S/1次频率输出GPS结果，默认false
			
		option.setIgnoreKillProcess(true);
		//可选，定位SDK内部是一个service，并放到了独立进程。
		//设置是否在stop的时候杀死这个进程，默认（建议）不杀死，即setIgnoreKillProcess(true)
			
		option.SetIgnoreCacheException(false);
		//可选，设置是否收集Crash信息，默认收集，即参数为false

		option.setWifiCacheTimeOut(5*60*1000);
		//可选，V7.2版本新增能力
		//如果设置了该接口，首次启动定位时，会先判断当前Wi-Fi是否超出有效期，若超出有效期，会先重新扫描Wi-Fi，然后定位
			
		option.setEnableSimulateGps(false);
		//可选，设置是否需要过滤GPS仿真结果，默认需要，即参数为false
			
		mLocationClient.setLocOption(option);
		//mLocationClient为第二步初始化过的LocationClient对象
		//需将配置好的LocationClientOption对象，通过setLocOption方法传递给LocationClient对象使用
		//更多LocationClientOption的配置，请参照类参考中LocationClientOption类的详细说明
		mLocationClient.start();
		while (!exittask)
		{
			
			//mLocationClient为第二步初始化过的LocationClient对象
			mLocationClient.requestLocation(); 
			while (myListener.state != 1)
			{
				isleep( 1 * 1000 );
			}
			try {
				Date date = new Date();
				String url = MessageFormat.format(gpsUrl, mac, myListener.longitude, myListener.latitude, sf.format(date));
				Log.e("gpsUrl", url);
				String text = get(url);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			myListener.state = 0;
			
			//线程休眠
			isleep( 5 * 1000 );
		}
	}
	public String get(String url) throws Exception
	{
		String result = null;
		
		for (int i = 0; i < 3; i++)
		{
			result = HttpSubtask.execute_get(url);
			
			if (result != null)
			{
				break;
			}
		}
		
		if (result == null)
		{
			throw new NetException("network error for url:" + url);
		}
		
		return result;
	}
	//判断WIFI网络是否可用
	public boolean isWifiConnected(Context context) { 
		if (context != null) { 
			ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE); 
			NetworkInfo mWiFiNetworkInfo = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI); 
			if (mWiFiNetworkInfo != null) { 
				return mWiFiNetworkInfo.isAvailable(); 
			} 
		} 
		return false; 
	}
	//判断是否有网络连接
	public boolean isNetworkConnected(Context context) { 
		if (context != null) { 
			ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE); 
			NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo(); 
			if (mNetworkInfo != null) { 
				return mNetworkInfo.isAvailable(); 
			} 
		} 
		return false; 
	}
}
