package com.example.huiqian.myapplication;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static android.os.Build.VERSION.SDK;

public class MainActivity extends AppCompatActivity {

    public LocationClient mLocationClient;

    private TextView positionText;

    private MapView mapView;

    private BaiduMap baiduMap;

    private boolean isFirstLocate=true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//首先创建了一个LocationClient实例，接收一个Context参数
        mLocationClient=new LocationClient(getApplicationContext());
//调用LocationClient的registerLocationListener的方法注册一个定位监听器，当获取到位置信息的时候，就会回调这个定位监听器
        mLocationClient.registerLocationListener(new MyLocationListener());
//进行初始化操作，initialize接收一个Context参数，初始化操作一定要在setContentView之前调用
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        mapView=(MapView)findViewById(R.id.bmapView);
        baiduMap=mapView.getMap();
        baiduMap.setMyLocationEnabled(true);

        positionText=(TextView)findViewById(R.id.position_text_view);
        //先创建一个空的list集合，依次判断这三个权限有没有被授权，如果没被授权就增加到list的组合里，最后将list转换成数组，再调用ActivityCompat.requestPermissions进行申请
        List<String> permissionList=new ArrayList<>();
        if(ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if(ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.READ_PHONE_STATE)!=PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if(ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.WRITE_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if(!permissionList.isEmpty()){
            String[] permissions=permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(MainActivity.this,permissions,1);
        }
        else {
            requestLocation();
        }
    }
        //调用LocationClient的start方法就能开始定位了，定位的结果会回调到我们前面注册的监听器中，也就是MyLocationListener
        private void requestLocation(){
        initLocation();
        mLocationClient.start();
        }

        private void initLocation(){
            LocationClientOption option=new LocationClientOption();
            option.setScanSpan(5000);
            //使用GPS定位
        option.setLocationMode(LocationClientOption.LocationMode.Device_Sensors);
            option.setIsNeedAddress(true);
            mLocationClient.setLocOption(option);
        }

        //通过一个循环，将申请的每个权限都进行了判断，如果有任何一个权限被拒绝，就直接调用finish方法关闭当前程序
    //只有当所有权限都被用户同意了，才会调用requestLocation()方法开始地理位置定位
        @Override
        public void onRequestPermissionsResult(int requestCode, String[] permissions,int[] grantResults){
        switch (requestCode){
            case 1:
                if(grantResults.length>0){
                    for(int result:grantResults){
                        if(result!=PackageManager.PERMISSION_GRANTED){
                            Toast.makeText(this,"必须同意所有权限才能使用本程序",Toast.LENGTH_SHORT);
                            finish();
                            return;
                        }
                    }
                    requestLocation();
                }else{
                    Toast.makeText(this,"发生未知错误",Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
        }
        }

        private void navigateTo(BDLocation location){
        if(isFirstLocate){
            LatLng ll=new LatLng(location.getLatitude(),location.getLongitude());
            MapStatusUpdate update=MapStatusUpdateFactory.newLatLng(ll);
            baiduMap.animateMapStatus(update);
            update=MapStatusUpdateFactory.zoomTo(16f);
            baiduMap.animateMapStatus(update);
            isFirstLocate=false;
        }
            MyLocationData.Builder locationBuilder=new MyLocationData.Builder();
        locationBuilder.latitude(location.getLatitude());
        locationBuilder.longitude(location.getLongitude());
        MyLocationData locationData=locationBuilder.build();
        baiduMap.setMyLocationData(locationData);
        }

        public class MyLocationListener implements BDLocationListener{
        @Override
            public void onReceiveLocation(final BDLocation location){
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    StringBuilder currentPosition = new StringBuilder();
//                    //通过BDLocation的getLatitude()方法来获取当前位置的纬度
//                    //通过getLongitude()方法获取当前位置的经度
//                    //通过getLocType方法获取当前的定位方式
//                    currentPosition.append("纬度：").append(location.getLatitude()).append("\n");
//                    currentPosition.append("经线: ").append(location.getLongitude()).append("\n");
//                    currentPosition.append("国家：").append(location.getCountry()).append("\n");
//                    currentPosition.append("省：").append(location.getProvince()).append("\n");
//                    currentPosition.append("市：").append(location.getCity()).append("\n");
//                    currentPosition.append("区：").append(location.getDistrict()).append("\n");
//                    currentPosition.append("街道：").append(location.getStreet()).append("\n");
//
//                    currentPosition.append("定位方式:");
//                    if (location.getLocType() == BDLocation.TypeGpsLocation) {
//                        currentPosition.append("GPS");
//                    } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {
//                        currentPosition.append("网络");
//                    }
//                    positionText.setText(currentPosition);
//
//                }
//            });

            if(location.getLocType()==BDLocation.TypeGpsLocation||location.getLocType()==BDLocation.TypeNetWorkLocation){
                navigateTo(location);
            }
        }

            public void onConnectHotSpotMessage(String s,int i){

        }
        }
        @Override
        protected void onResume(){
        super.onResume();
        mapView.onResume();
        }
        @Override
        protected void onPause(){
        super.onPause();
        mapView.onPause();
        }

    protected void onDestory(){
        super.onDestroy();
        mLocationClient.stop();
        mapView.onDestroy();
        baiduMap.setMyLocationEnabled(false);
        }
}
