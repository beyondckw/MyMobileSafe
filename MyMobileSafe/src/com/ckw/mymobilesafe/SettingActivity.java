package com.ckw.mymobilesafe;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.ckw.mymobilesafe.service.AddressService;
import com.ckw.mymobilesafe.service.CallSmsSafeService;
import com.ckw.mymobilesafe.service.WatchDogService;
import com.ckw.mymobilesafe.ui.SettingClickView;
import com.ckw.mymobilesafe.ui.SettingItemView;
import com.ckw.mymobilesafe.utils.MD5Utils;
import com.ckw.mymobilesafe.utils.ServiceUtils;

public class SettingActivity extends Activity {
	
	//设置是否自动更新
	private SettingItemView siv_update;
	//设置是否开启来电归属地显示
	private SettingItemView siv_show_address;
	private Intent showAddressIntent;
	
	private SharedPreferences sp;
	
	//黑名单拦截设置
	private SettingItemView siv_callsms_safe;
	private Intent callSmsSafeIntent;
	
	//设置归属地显示框背景
	private SettingClickView scv_changebg;
	
	//程序锁设置
	private SettingItemView siv_watchdog;
	private Intent watchDogIntent;
	
	private EditText et_setup_pwd;
	private EditText et_setup_confirm;
	private Button ok;
	private Button cancel;
	private AlertDialog dialog;
	
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);
		
		sp = getSharedPreferences("config", MODE_PRIVATE);
		
		/*
		 * 设置是否自动升级
		 */
		siv_update = (SettingItemView) findViewById(R.id.siv_update);
		
		//下一次启动的时候从SharedPreferences中把参数update取出来
		boolean update = sp.getBoolean("update", false);
		if(update){
			//自动升级已经开启
			siv_update.setChecked(true);
		}else{
			//自动升级已经关闭
			siv_update.setChecked(false);
		}
		siv_update.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {

				Editor editor = sp.edit();
				//判断是否有选中
				//已经打开自动升级了
				if(siv_update.isChecked()){
					siv_update.setChecked(false);
					editor.putBoolean("update", false);
				}else{
					//没有打开自动升级
					siv_update.setChecked(true);
					editor.putBoolean("update", true);
				}
				editor.commit();
			}
			
		});
		
		/*
		 * 设置是否开启来电归属地显示
		 */
		siv_show_address = (SettingItemView) findViewById(R.id.siv_show_address);
		showAddressIntent = new Intent(this, AddressService.class);
		
		boolean isServiceRunning = ServiceUtils.isServiceRunning(
				SettingActivity.this,
				"com.ckw.mymobilesafe.service.AddressService");
		if(isServiceRunning){
			//监听来电的服务是开启的
			siv_show_address.setChecked(true);
		}else{
			siv_show_address.setChecked(false);
		}
		
		siv_show_address.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(siv_show_address.isChecked()){
					stopService(showAddressIntent);
					siv_show_address.setChecked(false);
				}else{
					startService(showAddressIntent);
					siv_show_address.setChecked(true);
				}
			}
		});
		
		//黑名单拦截设置
		siv_callsms_safe = (SettingItemView) findViewById(R.id.siv_callsms_safe);
		callSmsSafeIntent = new Intent(this, CallSmsSafeService.class);
		
		boolean iscallSmsServiceRunning = ServiceUtils.isServiceRunning(
				SettingActivity.this,
				"com.ckw.mymobilesafe.service.CallSmsSafeService");
		if(iscallSmsServiceRunning){
			siv_callsms_safe.setChecked(true);
		}else{
			siv_callsms_safe.setChecked(false);
		}
		
		siv_callsms_safe.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (siv_callsms_safe.isChecked()) {
					// 变为非选中状态
					siv_callsms_safe.setChecked(false);
					stopService(callSmsSafeIntent);
				} else {
					// 选择状态
					siv_callsms_safe.setChecked(true);
					startService(callSmsSafeIntent);
				}

			}
		});
		
		//程序锁设置
		siv_watchdog = (SettingItemView) findViewById(R.id.siv_watchdog);
		watchDogIntent = new Intent(this, WatchDogService.class);
		
		boolean isWatchDogServiceRunning = ServiceUtils.isServiceRunning(
				SettingActivity.this,
				"com.ckw.mymobilesafe.service.WatchDogService");
		if(isWatchDogServiceRunning){
			siv_watchdog.setChecked(true);
		}else{
			siv_watchdog.setChecked(false);
		}
		
		siv_watchdog.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (siv_watchdog.isChecked()) {
					// 变为非选中状态
					siv_watchdog.setChecked(false);
					stopService(watchDogIntent);
				} else {
					// 选择状态
					//弹出设置密码的对话框
					showSetupPwdDialog();
					
					siv_watchdog.setChecked(true);
					startService(watchDogIntent);
				}

			}
		});
	
		//设置号码归属地的背景
		scv_changebg = (SettingClickView)findViewById(R.id.scv_changebg);
		scv_changebg.setTitle("归属地提示框风格");
		final String[] items = {"半透明","活力橙","卫士蓝","金属灰","苹果绿"};
		int which = sp.getInt("which", 0);
		scv_changebg.setDesc(items[which]);
		scv_changebg.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				int which = sp.getInt("which", 0);  //把上次保存的取出来当默认值
				AlertDialog.Builder builder = new Builder(SettingActivity.this);
				builder.setTitle("归属地提示框风格");
				builder.setSingleChoiceItems(items, which, new  DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						//保存选择参数
						Editor editor = sp.edit();
						editor.putInt("which", which);
						editor.commit();
						
						scv_changebg.setDesc(items[which]);
						
						//取消对话框
						dialog.dismiss();
					}
				});
				builder.setNegativeButton("取消", null);
				builder.show();
			}
		});
	}

	
	/**
	 * 设置密码对话框
	 */
	private void showSetupPwdDialog() {

		AlertDialog.Builder builder = new Builder(SettingActivity.this);
		View view = View.inflate(SettingActivity.this, R.layout.dialog_setup_password, null);
		
		dialog = builder.create();
		dialog.setView(view, 0, 0, 0, 0);		//去除边框
		dialog.show();
		
		et_setup_pwd = (EditText) view.findViewById(R.id.et_setup_pwd);
		et_setup_confirm = (EditText) view.findViewById(R.id.et_setup_confirm);
		ok = (Button) view.findViewById(R.id.ok);
		cancel = (Button) view.findViewById(R.id.cancel);
		
		cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//把对话框消除
				dialog.dismiss();
			}
		});
		
		ok.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//取出密码
				String password = et_setup_pwd.getText().toString().trim();
				String password_confirm = et_setup_confirm.getText().toString().trim();
				
				if(TextUtils.isEmpty(password)||TextUtils.isEmpty(password_confirm)){
					Toast.makeText(SettingActivity.this, "密码为空", 0).show();
					return ;
				}
				//判断是否一致才去保存
				if(password.equals(password_confirm)){
					//一致的话，就保存密码，把对话框消掉，还要进入手机防盗页面
					Editor editor = sp.edit();
					editor.putString("appLockPassword", MD5Utils.md5Password(password));//保存加密后的密码
					editor.commit();
					dialog.dismiss();
					//跳到要加锁的界面
					Intent intent = new Intent(SettingActivity.this,AppManagerActivity.class);
					startActivity(intent);
				}else{
					Toast.makeText(SettingActivity.this, "密码不一致", 0).show();
					return ;
				}
			}
		});
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		//判断服务是否正在运行
		boolean isRunning = ServiceUtils.isServiceRunning(this,
				"com.ckw.mymobilesafe.service.AddressService");
		if(isRunning){
			siv_show_address.setChecked(true);
		}else{
			siv_show_address.setChecked(false);
		}
		boolean iscallSmsServiceRunning = ServiceUtils.isServiceRunning(
				SettingActivity.this,
				"com.ckw.mymobilesafe.service.CallSmsSafeService");
		siv_callsms_safe.setChecked(iscallSmsServiceRunning);
		
		boolean isWatchDogServiceRunning = ServiceUtils.isServiceRunning(
				SettingActivity.this,
				"com.ckw.mymobilesafe.service.WatchDogService");
		if(isWatchDogServiceRunning){
			siv_watchdog.setChecked(true);
		}else{
			siv_watchdog.setChecked(false);
		}
	}
}
