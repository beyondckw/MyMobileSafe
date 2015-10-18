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
	private Method getPackageSizeInfoMethod = null;  //API���صķ���
	private String cleanPackgename ; //Ҫ������İ���
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
	 * ɨ���ֻ���������Ӧ�ó���Ļ�����Ϣ
	 */
	private void scanCache() {
		pm = getPackageManager();
		new Thread(){
			public void run() {
				
				//1.���ҵ���ȡ����ķ�������������Ǳ�API���������ģ�����Ҫ�Ȼ�ȡ����ֽ��룬�ٷ��䣩
				
				Method[] methods = PackageManager.class.getMethods();
				for(Method method : methods){
					if("getPackageSizeInfo".equals(method.getName())){
						getPackageSizeInfoMethod = method;
						break;
					}
				}
				
				//2.��ÿһ��Ӧ�ó�����ʹ�ø÷�����ȡ���еĻ����ļ�
				List<PackageInfo> packInfos = pm.getInstalledPackages(0);
				
				pb.setMax(packInfos.size());
			    int progress = 0;
				
				for(PackageInfo packInfo : packInfos){
					try {
						getPackageSizeInfoMethod.invoke(pm, packInfo.packageName, new MyDataObserver()); //���߳���ִ��
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
							
							tv_scan_status.setText("ɨ�����...");
						}
					});
				}
				//ȫ��ɨ�����û�з���һ������
				if(flag == false){
					runOnUiThread( new Runnable() {
						public void run() {
							
							String text =  "��ϲ���ֻ��ܸɾ���û�л�����Ҫ����";
							showToast(text);
							tv_scan_status.setText("ɨ����ϣ�û���ֻ���");
						}
					});
				}
				
			};
		}.start();
		
	}
	
	//��ע�⣬�������ķ����������߳���ִ�еģ�����Ҫ����UI����Ļ���Ҫ�����߳�
	private class MyDataObserver extends IPackageStatsObserver.Stub{

		@Override
		public void onGetStatsCompleted(final PackageStats pStats, boolean succeeded)
				throws RemoteException {
				final long cache = pStats.cacheSize;
				
				final ApplicationInfo appInfo;
				try {
					appInfo = pm.getApplicationInfo(pStats.packageName, 0);
					
					//���½���
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							tv_scan_status.setText("����ɨ��"+appInfo.loadLabel(pm).toString());
							if(cache > 0){  //�л�����Ϣ��Ӧ��
								flag = true;//���ڻ����ļ�
								totalCleanSize += cache;
								
								
								final View view = View.inflate(getApplicationContext(), R.layout.list_item_cacheinfo, null);
								
								TextView tv_cache_size = (TextView) view.findViewById(R.id.tv_cache_size);
								tv_cache_size.setText("�����С: "+ 
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
	
	//��ע�⣬�������ķ����������߳���ִ�еģ�����Ҫ����UI����Ļ���Ҫ�����߳�
	private class MyDataObserver2 extends IPackageStatsObserver.Stub{

		@Override
		public void onGetStatsCompleted(final PackageStats pStats, boolean succeeded)
				throws RemoteException {
			isExitsCache = false;
			System.out.println(pStats.cacheSize);
			if(pStats.cacheSize > 0){   //�����ڻ���
				isExitsCache = true;
			}
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		//���²�ѯһ�ο��Ƿ񻺴��ѱ�����
		try {
			getPackageSizeInfoMethod.invoke(pm, cleanPackgename, new MyDataObserver2());  //���߳���ִ��
			Thread.sleep(200);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if(isExitsCache){
			
		}else{   //�˻����Ѿ��������ˣ����½���
			String text =  "�ͷ���"+Formatter.formatFileSize(getApplicationContext(), tempCache)+"���ڴ�ռ�";
			showToast(text);
			ll_container.removeView(romoveview);
		}
		
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	//��ע�⣬�������ķ����������߳���ִ�еģ�����Ҫ����UI����Ļ���Ҫ�����߳�
	private class MypackDataObserver extends IPackageDataObserver.Stub{

		@Override
		public void onRemoveCompleted(String packageName, boolean succeeded)
				throws RemoteException {
			
			runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					ll_container.removeAllViews();
					String text = "�ͷ���"+ Formatter.formatFileSize(getApplicationContext(), totalCleanSize)+
							"���ڴ�ռ�\n��ϲ���ֻ��ܸɾ���û�л�����Ҫ����";
					showToast(text);
					totalCleanSize = 0;
					tv_scan_status.setText("�����������");
				}
			});
		}
		
	}
	
	/**
	 * �����ֻ���ȫ������
	 * freeStorageAndNotify() Ϊϵͳ���ص�API������Ҫ�÷�������ҳ���
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

