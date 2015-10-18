package com.ckw.mymobilesafe;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageDataObserver;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.PackageStats;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.text.format.Formatter;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class CleanCacheActivity extends Activity {
	
	private TextView tv_scan_status;
	private ProgressBar pb;
	private PackageManager pm;
	private LinearLayout ll_container;
	private boolean flag = false;
	private long totalCleanSize = 0;
	private long tempCache;
	private View romoveview;
	private boolean isExitsCache = true;
	private Method getPackageSizeInfoMethod = null;  //API隐藏的方法
	private String cleanPackgename ; //要清理缓存的包名
	private Toast toast;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_clean_cache);
		
		tv_scan_status = (TextView) findViewById(R.id.tv_scan_status);
		pb = (ProgressBar) findViewById(R.id.pb);
		ll_container = (LinearLayout) findViewById(R.id.ll_container);
		
		scanCache();
	}


	/**
	 * 扫描手机里面所有应用程序的缓存信息
	 */
	private void scanCache() {
		pm = getPackageManager();
		new Thread(){
			public void run() {
				
				//1.先找到获取缓存的方法（这个方法是被API隐藏起来的，所以要先获取类的字节码，再反射）
				
				Method[] methods = PackageManager.class.getMethods();
				for(Method method : methods){
					if("getPackageSizeInfo".equals(method.getName())){
						getPackageSizeInfoMethod = method;
						break;
					}
				}
				
				//2.在每一个应用程序中使用该方法获取所有的缓存文件
				List<PackageInfo> packInfos = pm.getInstalledPackages(0);
				
				pb.setMax(packInfos.size());
			    int progress = 0;
				
				for(PackageInfo packInfo : packInfos){
					try {
						getPackageSizeInfoMethod.invoke(pm, packInfo.packageName, new MyDataObserver()); //子线程中执行
						Thread.sleep(200);
						progress++;
						pb.setProgress(progress);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				if(progress >= packInfos.size()){
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							
							tv_scan_status.setText("扫描完毕...");
						}
					});
				}
				//全部扫描完毕没有发现一个缓存
				if(flag == false){
					runOnUiThread( new Runnable() {
						public void run() {
							
							String text =  "恭喜您手机很干净，没有缓存需要清理";
							showToast(text);
							tv_scan_status.setText("扫描完毕，没发现缓存");
						}
					});
				}
				
			};
		}.start();
		
	}
	
	//请注意，这个父类的方法是在子线程中执行的，所以要更新UI界面的话，要在主线程
	private class MyDataObserver extends IPackageStatsObserver.Stub{

		@Override
		public void onGetStatsCompleted(final PackageStats pStats, boolean succeeded)
				throws RemoteException {
				final long cache = pStats.cacheSize;
				
				final ApplicationInfo appInfo;
				try {
					appInfo = pm.getApplicationInfo(pStats.packageName, 0);
					
					//更新界面
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							tv_scan_status.setText("正在扫描"+appInfo.loadLabel(pm).toString());
							if(cache > 0){  //有缓存信息的应用
								flag = true;//存在缓存文件
								totalCleanSize += cache;
								
								
								final View view = View.inflate(getApplicationContext(), R.layout.list_item_cacheinfo, null);
								
								TextView tv_cache_size = (TextView) view.findViewById(R.id.tv_cache_size);
								tv_cache_size.setText("缓存大小: "+ 
										Formatter.formatFileSize(getApplicationContext(), cache));
								TextView tv_name = (TextView) view.findViewById(R.id.tv_app_name);
								tv_name.setText(appInfo.loadLabel(pm).toString());
								ImageView iv_icon = (ImageView) view.findViewById(R.id.iv_app_icon);
								iv_icon.setImageDrawable(appInfo.loadIcon(pm));
								ImageView iv_clean = (ImageView) view.findViewById(R.id.iv_clean);
								iv_clean.setOnClickListener(new OnClickListener() {
									@Override
									public void onClick(View v) {
										tempCache = cache;
										cleanPackgename = appInfo.packageName;
										romoveview = view;
										Intent intent = new Intent();
										intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
										intent.addCategory("android.intent.category.DEFAULT");
										intent.setData(Uri.parse("package:" + cleanPackgename));
										
										startActivityForResult(intent, 0);
										
									}
								});
							
								ll_container.addView(view, 0);
							}
						}
					});
					
				} catch (NameNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		
	}
	
	//请注意，这个父类的方法是在子线程中执行的，所以要更新UI界面的话，要在主线程
	private class MyDataObserver2 extends IPackageStatsObserver.Stub{

		@Override
		public void onGetStatsCompleted(final PackageStats pStats, boolean succeeded)
				throws RemoteException {
			isExitsCache = false;
			System.out.println(pStats.cacheSize);
			if(pStats.cacheSize > 0){   //还存在缓存
				isExitsCache = true;
			}
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		//重新查询一次看是否缓存已被清理
		try {
			getPackageSizeInfoMethod.invoke(pm, cleanPackgename, new MyDataObserver2());  //子线程中执行
			Thread.sleep(200);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if(isExitsCache){
			
		}else{   //此缓存已经不存在了，更新界面
			String text =  "释放了"+Formatter.formatFileSize(getApplicationContext(), tempCache)+"的内存空间";
			showToast(text);
			ll_container.removeView(romoveview);
		}
		
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	//请注意，这个父类的方法是在子线程中执行的，所以要更新UI界面的话，要在主线程
	private class MypackDataObserver extends IPackageDataObserver.Stub{

		@Override
		public void onRemoveCompleted(String packageName, boolean succeeded)
				throws RemoteException {
			
			runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					ll_container.removeAllViews();
					String text = "释放了"+ Formatter.formatFileSize(getApplicationContext(), totalCleanSize)+
							"的内存空间\n恭喜您手机很干净，没有缓存需要清理";
					showToast(text);
					totalCleanSize = 0;
					tv_scan_status.setText("缓存清理完毕");
				}
			});
		}
		
	}
	
	/**
	 * 清理手机的全部缓存
	 * freeStorageAndNotify() 为系统隐藏的API，所以要用反射把它找出来
	 * @param view
	 */
	public void cleanAll(View view){
		
		Method[] methods = PackageManager.class.getMethods();
		for(Method method : methods){
			if("freeStorageAndNotify".equals(method.getName())){
				try {
					method.invoke(pm, Integer.MAX_VALUE, new MypackDataObserver());
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private void showToast(String text) {
		if(toast == null){
			toast = Toast.makeText(getApplicationContext(), text, 0);
		}else{
			toast.setText(text);
			toast.setDuration(0);
		}
		toast.show();
	}
	
}

