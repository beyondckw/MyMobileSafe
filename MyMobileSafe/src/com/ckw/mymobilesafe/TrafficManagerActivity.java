package com.ckw.mymobilesafe;

import java.util.List;

import android.app.Activity;
import android.net.TrafficStats;
import android.os.Bundle;
import android.text.format.Formatter;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SlidingDrawer;
import android.widget.SlidingDrawer.OnDrawerOpenListener;
import android.widget.TextView;

import com.ckw.mymobilesafe.domain.AppInfo;
import com.ckw.mymobilesafe.engine.AppInfoProvider;

public class TrafficManagerActivity extends Activity implements OnClickListener {

	private static final String TAG = "TrafficManagerActivity";
	private ListView lv_traffic_manager;
	private LinearLayout ll_loading;
	private List<AppInfo> appInfos;     //所有的应用程序
	private AppManagerAdapter adapter;
	private AppInfo appInfo;
	private SlidingDrawer slidingDrawer;
	private TextView tv1;
	private TextView tv2;
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_traffic_manager);
		
		lv_traffic_manager = (ListView) findViewById(R.id.lv_traffic_manager);
		ll_loading = (LinearLayout) findViewById(R.id.ll_loading);
		slidingDrawer = (SlidingDrawer) findViewById(R.id.mydrawer);
		tv1 = (TextView) findViewById(R.id.tv1);
		tv2 = (TextView) findViewById(R.id.tv2);
		
		slidingDrawer.setOnDrawerOpenListener(new OnDrawerOpenListener() {
			
			@Override
			public void onDrawerOpened() {
				// TrafficStats.getMobileTxBytes(); //3g/2g网络上传的总流量
				// TrafficStats.getMobileRxBytes();//3g/2g网络下载的总流量
				// TrafficStats.getTotalRxBytes();// 手机卡 + wifi
				// TrafficStats.getTotalTxBytes();
				long mobileTraffic = TrafficStats.getMobileTxBytes() + TrafficStats.getMobileRxBytes();
				long totalTraffic = TrafficStats.getTotalRxBytes() + TrafficStats.getTotalTxBytes();
				if(mobileTraffic <= 0)
					mobileTraffic = 0;
				if(totalTraffic <= 0)
					totalTraffic = 0;
				
				tv1.setText("移动流量："+Formatter.formatFileSize(getApplicationContext(), mobileTraffic));
				tv2.setText("手机流量："+Formatter.formatFileSize(getApplicationContext(), totalTraffic));
			}
		});
		
		//获取数据
		fillData();
	}

	private void fillData() {
		ll_loading.setVisibility(View.VISIBLE);
		new Thread(){
			public void run() {
				appInfos = AppInfoProvider.getAppInfos(TrafficManagerActivity.this);
				
				//加载listview的数据适配器
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if(adapter == null){
							adapter = new AppManagerAdapter();
							lv_traffic_manager.setAdapter(adapter);
						}else{
							adapter.notifyDataSetChanged();
						}
						ll_loading.setVisibility(View.INVISIBLE);
					}
				});
			};
		}.start();
	}
	
	private class AppManagerAdapter extends BaseAdapter{

		//控制listview有多少个条目
		@Override
		public int getCount() {
			//return appInfos.size();
			return appInfos.size();
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			View view = null;
			ViewHolder holder = null;
			
			appInfo = appInfos.get(position);
			
			//复用的时候要注意，缓存的view对象要是R.layout.list_item_appinfo这种view对象是才可以复用，
			//所以要加条件
			if(convertView != null && convertView instanceof RelativeLayout){
				view = convertView;
				holder = (ViewHolder) view.getTag();
			}else {
				view = View.inflate(getApplicationContext(), R.layout.list_item_traffic, null);
				
				//第一次加载的时候就把它们存起来
				holder = new ViewHolder();
				holder.tv_name = (TextView) view.findViewById(R.id.tv_app_name);
				holder.iv_icon = (ImageView) view.findViewById(R.id.iv_app_icon);
				holder.tv_shangchuan = (TextView) view.findViewById(R.id.tv_shangchuan);
				holder.tv_xiazai = (TextView) view.findViewById(R.id.tv_xiazai);
				
				//存放到父控件中，也就是view里
				view.setTag(holder);
			}
			
			//流量包位于系统的这个文件下面proc/uid_stat/10087
			long rx = TrafficStats.getUidRxBytes(appInfo.getUid());//得到对应程序的下载流量
			long tx = TrafficStats.getUidTxBytes(appInfo.getUid());//得到对应程序的上传流量
			if(rx <= 0)
				rx = 0;
			if(tx <= 0)
				tx = 0;
			holder.tv_name.setText(appInfo.getName());
			holder.iv_icon.setImageDrawable(appInfo.getIcon());
			holder.tv_shangchuan.setText("上传流量："+Formatter.formatFileSize(getApplicationContext(), tx));
			holder.tv_xiazai.setText("下载流量："+Formatter.formatFileSize(getApplicationContext(), rx));
			
			return view;
		}
		
		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}
	}
	
	static class ViewHolder{
		TextView tv_name;
		private TextView tv_shangchuan;
		private TextView tv_xiazai;
		ImageView iv_icon;
		
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}
}
