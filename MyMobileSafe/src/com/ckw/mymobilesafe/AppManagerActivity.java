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
	private List<AppInfo> appInfos;     //���е�Ӧ�ó���
	private AppManagerAdapter adapter;
	private List<AppInfo> userAppInfos;      //�û�Ӧ�ó���
	private List<AppInfo> systemAppInfos;      //ϵͳӦ�ó���
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
		tv_avail_sd.setText("SD�����ÿռ䣺"+Formatter.formatFileSize(this, sdsize));
		tv_avail_rom.setText("�ڴ���ÿռ䣺"+Formatter.formatFileSize(this, romsize));
		
		//��ȡ����
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
						tv_status.setText("ϵͳ����"+systemAppInfos.size()+"��");
					}else{
						tv_status.setText("�û�����"+userAppInfos.size()+"��");
					}
				}
			}
		});
		
		lv_app_manager.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {

				//���������
				if(position == 0){  //��λ����һ��textview��ǩ,������Ӧ
					return ;
				}else if(position == (userAppInfos.size()+1)){    //��λ����һ��textview��ǩ
					return ;
				}else if(position <= userAppInfos.size()){   //�û������λ��
					int newposition = position - 1;  //ǰ�����һ��  "�û�����"  ��textview��ǩ
					appInfo = userAppInfos.get(newposition);
				}else{ //ϵͳ�����λ��
					int newposition = position - 1 - userAppInfos.size() -1;  //ǰ�����һ��  "�û�����"  ��textview��ǩ
					appInfo = systemAppInfos.get(newposition);
				}
				
				//�Ѿɵĵ�������رյ�
				dismissPopupWindow();
				
				//���õ�����ݴ���
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
				
				// �ڴ����������õĿ��ֵ ��������dip, ����ҪתΪ�ֱ��ʲ�����Ӧ��ͬ�ֻ���
				int width = 150;
				int hight = 80;
				int px = DensityUtil.dip2px(getApplicationContext(), width);
				int py = DensityUtil.dip2px(getApplicationContext(), hight);
				
				popupWindow = new PopupWindow(contentView, px, py);//-2����ViewGroup.LayoutParams.WRAP_CONTENT
				//����Ч���Ĳ��ű���Ҫ�����б�����ɫ
				//͸��Ҳ��һ����ɫ
				popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
				
				int []location = new int[2];
				view.getLocationInWindow(location);
				popupWindow.showAtLocation(parent, Gravity.LEFT | Gravity.TOP, location[0], location[1]-view.getHeight());
			
				//���ö���
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
		
		//������Ŀ�ĳ�����¼����ɵ�������������
		lv_app_manager.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View view,
					int position, long arg3) {
				//���������
				if(position == 0){  //��λ����һ��textview��ǩ,������Ӧ
					return true;
				}else if(position == (userAppInfos.size()+1)){    //��λ����һ��textview��ǩ
					return true;
				}else if(position <= userAppInfos.size()){   //�û������λ��
					int newposition = position - 1;  //ǰ�����һ��  "�û�����"  ��textview��ǩ
					appInfo = userAppInfos.get(newposition);
				}else{ //ϵͳ�����λ��
					int newposition = position - 1 - userAppInfos.size() -1;  //ǰ�����һ��  "�û�����"  ��textview��ǩ
					appInfo = systemAppInfos.get(newposition);
				}
				
				//�ж��Ƿ��Ѿ���������������
				boolean isWatchDogServiceRunning = ServiceUtils.isServiceRunning(
						AppManagerActivity.this,
						"com.ckw.mymobilesafe.service.WatchDogService");
				if(isWatchDogServiceRunning == false){
					//��û��������������
					Toast.makeText(AppManagerActivity.this, "���ȵ��ֻ���ʿ�������Ŀ�������������", 1).show();
					return true;
				}else{
				
					//�ж��Ƿ��ڳ��������ݿ���
					ViewHolder holder = (ViewHolder) view.getTag();
					if(dao.find(appInfo.getPackname())){
						//��Ӧ�ó����Ѿ������ݿ����ˣ�����ͻ�������ͬʱҪ����С��״̬��ͼ��
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
				
				//����listview������������
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

		//����listview�ж��ٸ���Ŀ
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
			/*if(position < userAppInfos.size()){  //��Щλ���������û�������ʾ��
				appInfo = userAppInfos.get(position);
			}else{    //��Щλ��������ϵͳ�����
				int newposition = position-userAppInfos.size();
				appInfo = systemAppInfos.get(newposition);
			}*/
			//���������
			if(position == 0){  //��λ����һ��textview��ǩ
				TextView tv = new TextView(getApplicationContext());
				tv.setTextColor(Color.WHITE);
				tv.setText("�û�����" + userAppInfos.size() + "��");
				tv.setBackgroundColor(Color.GRAY);
				return tv;
			}else if(position == (userAppInfos.size()+1)){    //��λ����һ��textview��ǩ
				TextView tv = new TextView(getApplicationContext());
				tv.setTextColor(Color.WHITE);
				tv.setText("ϵͳ����" + systemAppInfos.size() + "��");
				tv.setBackgroundColor(Color.GRAY);
				return tv;
			}else if(position <= userAppInfos.size()){   //�û������λ��
				int newposition = position - 1;  //ǰ�����һ��  "�û�����"  ��textview��ǩ
				appInfo = userAppInfos.get(newposition);
			}else{ //ϵͳ�����λ��
				int newposition = position - 1 - userAppInfos.size() -1;  //ǰ�����һ��  "�û�����"  ��textview��ǩ
				appInfo = systemAppInfos.get(newposition);
			}
			

			//���õ�ʱ��Ҫע�⣬�����view����Ҫ��R.layout.list_item_appinfo����view�����ǲſ��Ը��ã�
			//����Ҫ������
			if(convertView != null && convertView instanceof RelativeLayout){
				view = convertView;
				holder = (ViewHolder) view.getTag();
			}else {
				view = View.inflate(getApplicationContext(), R.layout.list_item_appinfo, null);
				
				//��һ�μ��ص�ʱ��Ͱ����Ǵ�����
				holder = new ViewHolder();
				holder.tv_name = (TextView) view.findViewById(R.id.tv_app_name);
				holder.tv_location = (TextView) view.findViewById(R.id.tv_app_location);
				holder.iv_icon = (ImageView) view.findViewById(R.id.iv_app_icon);
				holder.iv_status = (ImageView) view.findViewById(R.id.iv_status);
				
				//��ŵ����ؼ��У�Ҳ����view��
				view.setTag(holder);
			}
			
			holder.tv_name.setText(appInfo.getName());
			holder.iv_icon.setImageDrawable(appInfo.getIcon());
			if(appInfo.isInRom()){
				holder.tv_location.setText("�ֻ��ڴ�");
			}else{
				holder.tv_location.setText("�ⲿ�洢");
			}
			
			if(dao.find(appInfo.getPackname())){  //���ݿ��д��ڣ�����һ����Ҫ��������Ӧ�ó���
				
				boolean isWatchDogServiceRunning = ServiceUtils.isServiceRunning(
						AppManagerActivity.this,
						"com.ckw.mymobilesafe.service.WatchDogService");
				if(isWatchDogServiceRunning){   //ͬʱ�����������Ѿ�����
					holder.iv_status.setImageResource(R.drawable.lock);
				}else{              //����û�п�����������Ҳû�У�����Ҫ�������ݿ�
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
			Log.i(TAG, "����" + appInfo.getName());
			shareApplication();
			break;
		case R.id.ll_start:
			Log.i(TAG, "������" + appInfo.getName());
			startApplication();
			break;
		case R.id.ll_uninstall:
			if (appInfo.isUserApp()) {
				Log.i(TAG, "ж�أ�" + appInfo.getName());
				uninstallAppliation();
			}else{
				Toast.makeText(this, "ϵͳӦ��ֻ�л�ȡrootȨ�޲ſ���ж��", 0).show();
				//Runtime.getRuntime().exec("");
			}
			break;
		}
	}
	
	/**
	 * ����һ��Ӧ�ó���
	 */
	private void shareApplication() {
		// Intent { act=android.intent.action.SEND typ=text/plain flg=0x3000000 cmp=com.android.mms/.ui.ComposeMessageActivity (has extras) } from pid 256
		Intent intent = new Intent();
		intent.setAction("android.intent.action.SEND");
		intent.addCategory(Intent.CATEGORY_DEFAULT);
		intent.setType("text/plain");
		intent.putExtra(Intent.EXTRA_TEXT, "�Ƽ���ʹ��һ�����,���ƽУ�"+appInfo.getName());
		startActivity(intent);
	}

	/**
	 * ж��Ӧ��
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
		// ˢ�½��档
		fillData();
		super.onActivityResult(requestCode, resultCode, data);
	}

	/**
	 * ����һ��Ӧ�ó���
	 */
	private void startApplication() {
		// ��ѯ���Ӧ�ó�������activity�� ��������������
		PackageManager pm = getPackageManager();
		// Intent intent = new Intent();
		// intent.setAction("android.intent.action.MAIN");
		// intent.addCategory("android.intent.category.LAUNCHER");
		// //��ѯ���������е��ֻ��Ͼ�������������activity��
		// List<ResolveInfo> infos = pm.queryIntentActivities(intent,
		// PackageManager.GET_INTENT_FILTERS);
		Intent intent = pm.getLaunchIntentForPackage(appInfo.getPackname());
		if (intent != null) {
			startActivity(intent);
		} else {
			Toast.makeText(this, "����������ǰӦ��", 0).show();
		}
	}
}
