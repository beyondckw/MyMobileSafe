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
	
	//�����Ƿ��Զ�����
	private SettingItemView siv_update;
	//�����Ƿ��������������ʾ
	private SettingItemView siv_show_address;
	private Intent showAddressIntent;
	
	private SharedPreferences sp;
	
	//��������������
	private SettingItemView siv_callsms_safe;
	private Intent callSmsSafeIntent;
	
	//���ù�������ʾ�򱳾�
	private SettingClickView scv_changebg;
	
	//����������
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
		 * �����Ƿ��Զ�����
		 */
		siv_update = (SettingItemView) findViewById(R.id.siv_update);
		
		//��һ��������ʱ���SharedPreferences�аѲ���updateȡ����
		boolean update = sp.getBoolean("update", false);
		if(update){
			//�Զ������Ѿ�����
			siv_update.setChecked(true);
		}else{
			//�Զ������Ѿ��ر�
			siv_update.setChecked(false);
		}
		siv_update.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {

				Editor editor = sp.edit();
				//�ж��Ƿ���ѡ��
				//�Ѿ����Զ�������
				if(siv_update.isChecked()){
					siv_update.setChecked(false);
					editor.putBoolean("update", false);
				}else{
					//û�д��Զ�����
					siv_update.setChecked(true);
					editor.putBoolean("update", true);
				}
				editor.commit();
			}
			
		});
		
		/*
		 * �����Ƿ��������������ʾ
		 */
		siv_show_address = (SettingItemView) findViewById(R.id.siv_show_address);
		showAddressIntent = new Intent(this, AddressService.class);
		
		boolean isServiceRunning = ServiceUtils.isServiceRunning(
				SettingActivity.this,
				"com.ckw.mymobilesafe.service.AddressService");
		if(isServiceRunning){
			//��������ķ����ǿ�����
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
		
		//��������������
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
					// ��Ϊ��ѡ��״̬
					siv_callsms_safe.setChecked(false);
					stopService(callSmsSafeIntent);
				} else {
					// ѡ��״̬
					siv_callsms_safe.setChecked(true);
					startService(callSmsSafeIntent);
				}

			}
		});
		
		//����������
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
					// ��Ϊ��ѡ��״̬
					siv_watchdog.setChecked(false);
					stopService(watchDogIntent);
				} else {
					// ѡ��״̬
					//������������ĶԻ���
					showSetupPwdDialog();
					
					siv_watchdog.setChecked(true);
					startService(watchDogIntent);
				}

			}
		});
	
		//���ú�������صı���
		scv_changebg = (SettingClickView)findViewById(R.id.scv_changebg);
		scv_changebg.setTitle("��������ʾ����");
		final String[] items = {"��͸��","������","��ʿ��","������","ƻ����"};
		int which = sp.getInt("which", 0);
		scv_changebg.setDesc(items[which]);
		scv_changebg.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				int which = sp.getInt("which", 0);  //���ϴα����ȡ������Ĭ��ֵ
				AlertDialog.Builder builder = new Builder(SettingActivity.this);
				builder.setTitle("��������ʾ����");
				builder.setSingleChoiceItems(items, which, new  DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						//����ѡ�����
						Editor editor = sp.edit();
						editor.putInt("which", which);
						editor.commit();
						
						scv_changebg.setDesc(items[which]);
						
						//ȡ���Ի���
						dialog.dismiss();
					}
				});
				builder.setNegativeButton("ȡ��", null);
				builder.show();
			}
		});
	}

	
	/**
	 * ��������Ի���
	 */
	private void showSetupPwdDialog() {

		AlertDialog.Builder builder = new Builder(SettingActivity.this);
		View view = View.inflate(SettingActivity.this, R.layout.dialog_setup_password, null);
		
		dialog = builder.create();
		dialog.setView(view, 0, 0, 0, 0);		//ȥ���߿�
		dialog.show();
		
		et_setup_pwd = (EditText) view.findViewById(R.id.et_setup_pwd);
		et_setup_confirm = (EditText) view.findViewById(R.id.et_setup_confirm);
		ok = (Button) view.findViewById(R.id.ok);
		cancel = (Button) view.findViewById(R.id.cancel);
		
		cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//�ѶԻ�������
				dialog.dismiss();
			}
		});
		
		ok.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//ȡ������
				String password = et_setup_pwd.getText().toString().trim();
				String password_confirm = et_setup_confirm.getText().toString().trim();
				
				if(TextUtils.isEmpty(password)||TextUtils.isEmpty(password_confirm)){
					Toast.makeText(SettingActivity.this, "����Ϊ��", 0).show();
					return ;
				}
				//�ж��Ƿ�һ�²�ȥ����
				if(password.equals(password_confirm)){
					//һ�µĻ����ͱ������룬�ѶԻ�����������Ҫ�����ֻ�����ҳ��
					Editor editor = sp.edit();
					editor.putString("appLockPassword", MD5Utils.md5Password(password));//������ܺ������
					editor.commit();
					dialog.dismiss();
					//����Ҫ�����Ľ���
					Intent intent = new Intent(SettingActivity.this,AppManagerActivity.class);
					startActivity(intent);
				}else{
					Toast.makeText(SettingActivity.this, "���벻һ��", 0).show();
					return ;
				}
			}
		});
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		//�жϷ����Ƿ���������
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
