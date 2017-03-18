package com.amap.recordpath3d;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationClientOption.AMapLocationMode;
import com.amap.api.mapcore.util.co;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.database.DbAdapter;
import com.amap.record.PathRecord;
import com.example.recordpath3d.R;

public class MainActivity extends Activity implements LocationSource,
		AMapLocationListener {
	private MapView mMapView;
	private AMap mAMap;
	private OnLocationChangedListener mListener;
	private AMapLocationClient mLocationClient;
	private AMapLocationClientOption mLocationOption;
	private PolylineOptions mPolyoptions;
	private PathRecord record;
	private long mStartTime;
	private long mEndTime;
	private ToggleButton btn;
	private Button bt1;
	private DbAdapter DbHepler;
	private static LatLng preLocation;
	private LatLng cLocation;
	private static double energy = 0;
	private static double preV;

	public double getMET(double v){
		if (v == 0) return 1.8;
		if (v < 3.2*1000/3600) return 2.0;
		if (v <= 67/60) return 3.0;
		if (v <= 81/60) return 3.5;
		if (v <= 94/60) return 4.0;
		if (v <= 107/60) return 5.0;
		if (v <= 134/60) return 8.0;
		if (v <= 8*1000/3600) return 9.0;
		if (v <= 161/60) return 10.0;
		if (v <= 10.8*1000/3600) return 11.0;
		if (v <= 11.3*1000/3600) return 11.5;
		if (v <= 12.1*1000/3600) return 12.5;
		if (v <= 12.9*1000/3600) return 13.5;
		if (v <= 13.7*1000/3600) return 14.0;
		if (v <= 14.5*1000/3600) return 15.0;
		if (v <= 16.1*1000/3600) return 16.0;
		if (v <= 17.5*1000/3600) return 18.0;
		return 0;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.basicmap_activity);
		mMapView = (MapView) findViewById(R.id.map);
		mMapView.onCreate(savedInstanceState);// 此方法必须重写
		init();
		initpolyline();
	}

	/**
	 * 初始化AMap对象
	 */
	
	
	private void init() {
		if (mAMap == null) {
			mAMap = mMapView.getMap();
			setUpMap();
		}
		btn = (ToggleButton) findViewById(R.id.locationbtn);
		btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (btn.isChecked()) {
					Log.i("MY", "isChecked");

					mAMap.clear(true);
					if (record != null) {
						record = null;
					}
					record = new PathRecord();
					mStartTime = System.currentTimeMillis();
					record.setDate(getcueDate(mStartTime));
					energy = 0;
					preLocation = new LatLng(0, 0);
				} else {
					energy += getMET(preV) * 3.5 * 60 * 2 /60;
					mEndTime = System.currentTimeMillis();
					saveRecord(record.getPathline(), record.getDate(), record.getmPathRecordPoints());
					
				}
			}
		});
		
		bt1 = (Button) findViewById(R.id.button1);
		bt1.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Toast.makeText(MainActivity.this, "记录点点击", Toast.LENGTH_SHORT)
				.show();
				if (btn.isChecked() && record != null){
					//连续定位设置的是两秒一次，利用最新的位置信息取代当前定位
					List <AMapLocation> temp = record.getPathline();
					if (temp == null || temp.size() == 0){
						Toast.makeText(MainActivity.this, "定位程序正在初始化 ", Toast.LENGTH_SHORT)
						.show();
					}
					else{
						record.addRecordPoint(temp.get(temp.size()-1));
					}
					
				}
				else{
					Toast.makeText(MainActivity.this, "没有记录到记录点", Toast.LENGTH_SHORT)
					.show();
				}
				
			}
		});
	}

	protected void saveRecord(List<AMapLocation> list, String time, List<AMapLocation> recordsList) {
		if (list != null && list.size() > 0) {
			DbHepler = new DbAdapter(this);
			DbHepler.open();
			String duration = getDuration();
			float distance = getDistance(list);
			String average = getAverage(distance);
			String pathlineSring = getPathLineString(list);
			String records = getPathLineString(recordsList);
			
			
			AMapLocation firstLocaiton = list.get(0);
			AMapLocation lastLocaiton = list.get(list.size() - 1);
			String stratpoint = amapLocationToString(firstLocaiton);
			String endpoint = amapLocationToString(lastLocaiton);
			Toast.makeText(MainActivity.this, records, Toast.LENGTH_SHORT)
			.show();
			Toast.makeText(MainActivity.this, pathlineSring, Toast.LENGTH_SHORT)
			.show();
			long temp = DbHepler.createrecord(String.valueOf(distance), duration, average,
					pathlineSring, records, stratpoint, endpoint, time);
			
			Toast.makeText(MainActivity.this, temp+"", Toast.LENGTH_SHORT)
			.show();
			DbHepler.close();
		} else {
			Toast.makeText(MainActivity.this, "没有记录到路径", Toast.LENGTH_SHORT)
					.show();
		}
	}

	private String getDuration() {
		return String.valueOf((mEndTime - mStartTime) / 1000f);
	}

	private String getAverage(float distance) {
		return String.valueOf(distance / (float) (mEndTime - mStartTime));
	}

	private float getDistance(List<AMapLocation> list) {
		float distance = 0;
		if (list == null || list.size() == 0) {
			return distance;
		}
		for (int i = 0; i < list.size() - 1; i++) {
			AMapLocation firstpoint = list.get(i);
			AMapLocation secondpoint = list.get(i + 1);
			LatLng firstLatLng = new LatLng(firstpoint.getLatitude(),
					firstpoint.getLongitude());
			LatLng secondLatLng = new LatLng(secondpoint.getLatitude(),
					secondpoint.getLongitude());
			double betweenDis = AMapUtils.calculateLineDistance(firstLatLng,
					secondLatLng);
			distance = (float) (distance + betweenDis);
		}
		return distance;
	}

	private String getPathLineString(List<AMapLocation> list) {
		if (list == null || list.size() == 0) {
			return "";
		}
		StringBuffer pathline = new StringBuffer();
		for (int i = 0; i < list.size(); i++) {
			AMapLocation location = list.get(i);
			String locString = amapLocationToString(location);
			pathline.append(locString).append(";");
		}
		String pathLineString = pathline.toString();
		pathLineString = pathLineString.substring(0,
				pathLineString.length() - 1);
		return pathLineString;
	}

	private String amapLocationToString(AMapLocation location) {
		StringBuffer locString = new StringBuffer();
		locString.append(location.getLatitude()).append(",");
		locString.append(location.getLongitude()).append(",");
		locString.append(location.getProvider()).append(",");
		locString.append(location.getTime()).append(",");
		locString.append(location.getSpeed()).append(",");
		locString.append(location.getBearing());
		return locString.toString();
	}

	private void initpolyline() {
		mPolyoptions = new PolylineOptions();
		mPolyoptions.width(10f);
		mPolyoptions.color(Color.BLUE);
	}

	/**
	 * 设置一些amap的属性
	 */
	private void setUpMap() {
		mAMap.setLocationSource(this);// 设置定位监听
		mAMap.getUiSettings().setMyLocationButtonEnabled(true);// 设置默认定位按钮是否显示
		mAMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
		// 设置定位的类型为定位模式 ，可以由定位、跟随或地图根据面向方向旋转几种
		mAMap.setMyLocationType(AMap.LOCATION_TYPE_LOCATE);
	}

	/**
	 * 方法必须重写
	 */
	@Override
	protected void onResume() {
		super.onResume();
		mMapView.onResume();
	}

	/**
	 * 方法必须重写
	 */
	@Override
	protected void onPause() {
		super.onPause();
		mMapView.onPause();
	}

	/**
	 * 方法必须重写
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mMapView.onSaveInstanceState(outState);
	}

	/**
	 * 方法必须重写
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
		mMapView.onDestroy();
	}

	@Override
	public void activate(OnLocationChangedListener listener) {
		mListener = listener;
		startlocation();
	}

	@Override
	public void deactivate() {
		mListener = null;
		if (mLocationClient != null) {
			mLocationClient.stopLocation();
			mLocationClient.onDestroy();

		}
		mLocationClient = null;
	}

	@Override
	public void onLocationChanged(AMapLocation amapLocation) {
		if (mListener != null && amapLocation != null) {
			if (amapLocation != null && amapLocation.getErrorCode() == 0) {
				mListener.onLocationChanged(amapLocation);// 显示系统小蓝点
				LatLng mylocation = new LatLng(amapLocation.getLatitude(),
						amapLocation.getLongitude());
				mAMap.moveCamera(CameraUpdateFactory.changeLatLng(mylocation));
				if (btn.isChecked()) {
					record.addpoint(amapLocation);
					mPolyoptions.add(mylocation);
					redrawline();
				}
				preV = AMapUtils.calculateLineDistance(preLocation, mylocation) / 2;
				energy += getMET(preV) * 3.5 * 60 * 2 /60;
				preLocation = mylocation;
				
			} else {
				String errText = "定位失败," + amapLocation.getErrorCode() + ": "
						+ amapLocation.getErrorInfo();
				Log.e("AmapErr", errText);
			}
		}
	}
	
	
	//连续定位
	private void startlocation() {
		if (mLocationClient == null) {
			mLocationClient = new AMapLocationClient(this);
			mLocationOption = new AMapLocationClientOption();
			// 设置定位监听
			mLocationClient.setLocationListener(this);
			// 设置为高精度定位模式
			mLocationOption.setLocationMode(AMapLocationMode.Hight_Accuracy);

			mLocationOption.setInterval(2000);
			// 设置定位参数
			mLocationClient.setLocationOption(mLocationOption);
			// 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
			// 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用stopLocation()方法来取消定位请求
			// 在定位结束后，在合适的生命周期调用onDestroy()方法
			// 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
			mLocationClient.startLocation();

		}
	}

	private void redrawline() {
		if (mPolyoptions.getPoints().size() > 0) {
			mAMap.clear(true);
			mAMap.addPolyline(mPolyoptions);
		}
	}

	@SuppressLint("SimpleDateFormat")
	private String getcueDate(long time) {
		SimpleDateFormat formatter = new SimpleDateFormat(
				"yyyy-MM-dd  HH:mm:ss ");
		Date curDate = new Date(time);
		String date = formatter.format(curDate);
		return date;
	}

	public void record(View view) {
		Intent intent = new Intent(MainActivity.this, RecordActivity.class);
		startActivity(intent);
	}
}
