package com.ckw.mymobilesafe;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.text.format.Formatter;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ckw.mymobilesafe.db.dao.ApplockDao;
import com.ckw.mymobilesafe.domain.AppInfo;
import com.ckw.mymobilesafe.engine.AppInfoProvider;
import com.ckw.mymobilesafe.utils.DensityUtil;
import com.ckw.mymobilesafe.utils.ServiceUtils;

public class AppManagerActivity extends Activity implements OnClickListener {

	private static final String TAG = "AppManagerActivity";
	private TextView tv_avail_sd;
	private TextView tv_avail_rom;
	private ListView lv_app_manager;
	private LinearLayout ll_loading;
	private List<AppInfo> appInfos;     //所有的应用程序
	private AppManagerAdapter adapter;
	private List<AppInfo> userAppInfos;      //用户应用程序
	private List<AppInfo> systemAppInfos;      //系统应用程序
	private TextView tv_status;
	PopupWindow popupWindow;
	View contentView;
	AppInfo appInfo;
	
	private LinearLayout ll_start;
	private LinearLayout ll_share;
	private LinearLayout ll_uninstall;
	
	private ApplockDao dao;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_app_manager);
		
		dao = new ApplockDao(this);
		
		tv_status = (TextView) findViewById(R.id.tv_status);
		lv_app_manager = (ListView) findViewById(R.id.lv_app_manager);
		ll_loading = (LinearLayout) findViewById(R.id.ll_loading);
		
		tv_avail_sd = (TextView) findViewById(R.id.tv_avail_sd);
		tv_avail_rom = (TextView) findViewById(R.id.tv_avail_rom);
		long sdsize = getAvailSpace(Environment.getExternalStorageDirectory().getAbsolutePath());
		long romsize = getAvailSpace(Environment.getDataDirectory().getAbsolutePath());
		tv_avail_sd.setText("SD卡可用空间："+Formatter.formatFileSize(this, sdsize));
		tv_avail_rom.setText("内存可用空间："+Formatter.formatFileSize(this, romsize));
		
		//获取数据
		fillData();
		
		lv_app_manager.setOnScrollListener(new OnScrollListener() {
			
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				
				
			}
			
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				dismissPopupWindow();
				if(userAppInfos != null && systemAppInfos != null){
					if(firstVisibleItem > userAppInfos.size()){
						tv_status.setText("系统程序："+systemAppInfos.size()+"个");
					}else{
						tv_status.setText("用户程序："+userAppInfos.size()+"个");
					}
				}
			}
		});
		
		lv_app_manager.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {

				//分情况讨论
				if(position == 0){  //此位置是一个textview标签,不做响应
					return ;
				}else if(position == (userAppInfos.size()+1)){    //此位置是一个textview标签
					return ;
				}else if(position <= userAppInfos.size()){   //用户程序的位置
					int newposition = position - 1;  //前面多了一个  "用户程序："  的textview标签
					appInfo = userAppInfos.get(newposition);
				}else{ //系统程序的位置
					int newposition = position - 1 - userAppInfos.size() -1;  //前面多了一个  "用户程序："  的textview标签
					appInfo = systemAppInfos.get(newposition);
				}
				
				//把旧的弹出窗体关闭掉
				dismissPopupWindow();
				
				//设置点击气泡窗体
				if(contentView == null){
					contentView = View.inflate(getApplicationContext(), R.layout.popup_app_item, null);
					ll_start = (LinearLayout) contentView
							.findViewById(R.id.ll_start);
					ll_share = (LinearLayout) contentView
							.findViewById(R.id.ll_share);
					ll_uninstall = (LinearLayout) contentView
							.findViewById(R.id.ll_uninstall);

				}
				ll_start.setOnClickListener(AppManagerActivity.this);
				ll_share.setOnClickListener(AppManagerActivity.this);
				ll_uninstall.setOnClickListener(AppManagerActivity.this);
				
				// 在代码里面设置的宽高值 都是像素dip, 所以要转为分辨率才能适应不同手机。
				int width = 150;
				int hight = 80;
				int px = DensityUtil.dip2px(getApplicationContext(), width);
				int py = DensityUtil.dip2px(getApplicationContext(), hight);
				
				popupWindow = new PopupWindow(contentView, px, py);//-2代表ViewGroup.LayoutParams.WRAP_CONTENT
				//动画效果的播放必须要求窗体有背景颜色
				//透明也是一种颜色
				popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
				
				int []location = new int[2];
				view.getLocationInWindow(location);
				popupWindow.showAtLocation(parent, Gravity.LEFT | Gravity.TOP, location[0], location[1]-view.getHeight());
			
				//设置动画
				ScaleAnimation sa = new ScaleAnimation(0.3f, 1.0f, 0.3f, 1.0f, 
						Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0.5f);
				sa.setDuration(300);
				AlphaAnimation aa = new AlphaAnimation(0.5f, 1.0f);
				aa.setDuration(300);
				AnimationSet set = new AnimationSet(false);
				set.addAnimation(aa);
				set.addAnimation(sa);
				contentView.startAnimation(set);
			}
		});
		
		//设置条目的长点击事件，可弹出程序锁功能
		lv_app_manager.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View view,
					int position, long arg3) {
				//分情况讨论
				if(position == 0){  //此位置是一个textview标签,不做响应
					return true;
				}else if(position == (userAppInfos.size()+1)){    //此位置是一个textview标签
					return true;
				}else if(position <= userAppInfos.size()){   //用户程序的位置
					int newposition = position - 1;  //前面多了一个  "用户程序："  的textview标签
					appInfo = userAppInfos.get(newposition);
				}else{ //系统程序的位置
					int newposition = position - 1 - userAppInfos.size() -1;  //前面多了一个  "用户程序："  的textview标签
					appInfo = systemAppInfos.get(newposition);
				}
				
				//判断是否已经开启程序锁服务
				boolean isWatchDogServiceRunning = ServiceUtils.isServiceRunning(
						AppManagerActivity.this,
						"com.ckw.mymobilesafe.service.WatchDogService");
				if(isWatchDogServiceRunning == false){
					//还没开启程序锁服务
					Toast.makeText(AppManagerActivity.this, "请先到手机卫士设置中心开启程序锁服务", 1).show();
					return true;
				}else{
				
					//判断是否在程序锁数据库中
					ViewHolder holder = (ViewHolder) view.getTag();
					if(dao.find(appInfo.getPackname())){
						//此应用程序已经在数据库中了，点击就会解除锁，同时要更新小锁状态的图标
						dao.delete(appInfo.getPackname());
						holder.iv_status.setImageResource(R.drawable.unlock);
						
					}else{
						dao.add(appInfo.getPackname());
						holder.iv_status.setImageResource(R.drawable.lock);
					}
				}
				
				return true;
			}
		});
	}

	private void fillData() {
		ll_loading.setVisibility(View.VISIBLE);
		new Thread(){
			public void run() {
				appInfos = AppInfoProvider.getAppInfos(AppManagerActivity.this);
				userAppInfos = new ArrayList<AppInfo>();
				systemAppInfos = new ArrayList<AppInfo>();
				for(AppInfo info : appInfos){
					if(info.isUserApp()){
						userAppInfos.add(info);
					}else{
						systemAppInfos.add(info);
					}
				}
				
				//加载listview的数据适配器
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if(adapter == null){
							adapter = new AppManagerAdapter();
							lv_app_manager.setAdapter(adapter);
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
			return userAppInfos.size()+1+ systemAppInfos.size()+1;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			View view = null;
			ViewHolder holder = null;
			
		//	AppInfo appInfo = appInfos.get(position);
			AppInfo appInfo;
			/*if(position < userAppInfos.size()){  //这些位置是留给用户程序显示的
				appInfo = userAppInfos.get(position);
			}else{    //这些位置是留给系统程序的
				int newposition = position-userAppInfos.size();
				appInfo = systemAppInfos.get(newposition);
			}*/
			//分情况讨论
			if(position == 0){  //此位置是一个textview标签
				TextView tv = new TextView(getApplicationContext());
				tv.setTextColor(Color.WHITE);
				tv.setText("用户程序：" + userAppInfos.size() + "个");
				tv.setBackgroundColor(Color.GRAY);
				return tv;
			}else if(position == (userAppInfos.size()+1)){    //此位置是一个textview标签
				TextView tv = new TextView(getApplicationContext());
				tv.setTextColor(Color.WHITE);
				tv.setText("系统程序：" + systemAppInfos.size() + "个");
				tv.setBackgroundColor(Color.GRAY);
				return tv;
			}else if(position <= userAppInfos.size()){   //用户程序的位置
				int newposition = position - 1;  //前面多了一个  "用户程序："  的textview标签
				appInfo = userAppInfos.get(newposition);
			}else{ //系统程序的位置
				int newposition = position - 1 - userAppInfos.size() -1;  //前面多了一个  "用户程序："  的textview标签
				appInfo = systemAppInfos.get(newposition);
			}
			

			//复用的时候要注意，缓存的view对象要是R.layout.list_item_appinfo这种view对象是才可以复用，
			//所以要加条件
			if(convertView != null && convertView instanceof RelativeLayout){
				view = convertView;
				holder = (ViewHolder) view.getTag();
			}else {
				view = View.inflate(getApplicationContext(), R.layout.list_item_appinfo, null);
				
				//第一次加载的时候就把它们存起来
				holder = new ViewHolder();
				holder.tv_name = (TextView) view.findViewById(R.id.tv_app_name);
				holder.tv_location = (TextView) view.findViewById(R.id.tv_app_location);
				holder.iv_icon = (ImageView) view.findViewById(R.id.iv_app_icon);
				holder.iv_status = (ImageView) view.findViewById(R.id.iv_status);
				
				//存放到父控件中，也就是view里
				view.setTag(holder);
			}
			
			holder.tv_name.setText(appInfo.getName());
			holder.iv_icon.setImageDrawable(appInfo.getIcon());
			if(appInfo.isInRom()){
				holder.tv_location.setText("手机内存");
			}else{
				holder.tv_location.setText("外部存储");
			}
			
			if(dao.find(appInfo.getPackname())){  //数据库中存在，这是一个需要被上锁的应用程序
				
				boolean isWatchDogServiceRunning = ServiceUtils.isServiceRunning(
						AppManagerActivity.this,
						"com.ckw.mymobilesafe.service.WatchDogService");
				if(isWatchDogServiceRunning){   //同时程序锁服务已经开启
					holder.iv_status.setImageResource(R.drawable.lock);
				}else{              //服务没有开启，上了锁也没有，所以要更新数据库
					dao.delete(appInfo.getPackname());
					holder.iv_status.setImageResource(R.drawable.unlock);
				}
				
			}else{
				holder.iv_status.setImageResource(R.drawable.unlock);
			}
			
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
	
	private long getAvailSpace(String path){
		StatFs statf = new StatFs(path);
		long count = statf.getAvailableBlocks();
		long size = statf.getBlockSize();
		return count*size;
	}
	
	private void dismissPopupWindow() {
		if(popupWindow!=null && popupWindow.isShowing()){
			popupWindow.dismiss();
			popupWindow = null;
		}
	}

	static class ViewHolder{
		TextView tv_name;
		TextView tv_location;
		ImageView iv_icon;
		ImageView iv_status;
	}
	
	@Override
	protected void onDestroy() {
		dismissPopupWindow();
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		dismissPopupWindow();
		switch (v.getId()) {
		case R.id.ll_share:
			Log.i(TAG, "分享：" + appInfo.getName());
			shareApplication();
			break;
		case R.id.ll_start:
			Log.i(TAG, "启动：" + appInfo.getName());
			startApplication();
			break;
		case R.id.ll_uninstall:
			if (appInfo.isUserApp()) {
				Log.i(TAG, "卸载：" + appInfo.getName());
				uninstallAppliation();
			}else{
				Toast.makeText(this, "系统应用只有获取root权限才可以卸载", 0).show();
				//Runtime.getRuntime().exec("");
			}
			break;
		}
	}
	
	/**
	 * 分享一个应用程序
	 */
	private void shareApplication() {
		// Intent { act=android.intent.action.SEND typ=text/plain flg=0x3000000 cmp=com.android.mms/.ui.ComposeMessageActivity (has extras) } from pid 256
		Intent intent = new Intent();
		intent.setAction("android.intent.action.SEND");
		intent.addCategory(Intent.CATEGORY_DEFAULT);
		intent.setType("text/plain");
		intent.putExtra(Intent.EXTRA_TEXT, "推荐您使用一款软件,名称叫："+appInfo.getName());
		startActivity(intent);
	}

	/**
	 * 卸载应用
	 */
	private void uninstallAppliation() {
		// <action android:name="android.intent.action.VIEW" />
		// <action android:name="android.intent.action.DELETE" />
		// <category android:name="android.intent.category.DEFAULT" />
		// <data android:scheme="package" />
		Intent intent = new Intent();
		intent.setAction("android.intent.action.VIEW");
		intent.setAction("android.intent.action.DELETE");
		intent.addCategory("android.intent.category.DEFAULT");
		intent.setData(Uri.parse("package:" + appInfo.getPackname()));
		startActivityForResult(intent, 0);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// 刷新界面。
		fillData();
		super.onActivityResult(requestCode, resultCode, data);
	}

	/**
	 * 开启一个应用程序
	 */
	private void startApplication() {
		// 查询这个应用程序的入口activity。 把他开启起来。
		PackageManager pm = getPackageManager();
		// Intent intent = new Intent();
		// intent.setAction("android.intent.action.MAIN");
		// intent.addCategory("android.intent.category.LAUNCHER");
		// //查询出来了所有的手机上具有启动能力的activity。
		// List<ResolveInfo> infos = pm.queryIntentActivities(intent,
		// PackageManager.GET_INTENT_FILTERS);
		Intent intent = pm.getLaunchIntentForPackage(appInfo.getPackname());
		if (intent != null) {
			startActivity(intent);
		} else {
			Toast.makeText(this, "不能启动当前应用", 0).show();
		}
	}
}
