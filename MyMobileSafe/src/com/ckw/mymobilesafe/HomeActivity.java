package com.ckw.mymobilesafe;

import com.ckw.mymobilesafe.utils.MD5Utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

public class HomeActivity extends Activity {
	
	private ImageView imageView;
	private LinearLayout linearLayout_1, linearLayout_2, linearLayout_3, 
						linearLayout_4, linearLayout_5, linearLayout_6,
						linearLayout_7, linearLayout_8;
	
	private SharedPreferences sp;
	private EditText et_setup_pwd;
	private EditText et_setup_confirm;
	private Button ok;
	private Button cancel;
	private AlertDialog dialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		
		imageView = (ImageView) findViewById(R.id.main_iv);
		linearLayout_1 = (LinearLayout) findViewById(R.id.main_ll_1);
		linearLayout_2 = (LinearLayout) findViewById(R.id.main_ll_2);
		linearLayout_3 = (LinearLayout) findViewById(R.id.main_ll_3);
		linearLayout_4 = (LinearLayout) findViewById(R.id.main_ll_4);
		linearLayout_5 = (LinearLayout) findViewById(R.id.main_ll_5);
		linearLayout_6 = (LinearLayout) findViewById(R.id.main_ll_6);
		linearLayout_7 = (LinearLayout) findViewById(R.id.main_ll_7);
		linearLayout_8 = (LinearLayout) findViewById(R.id.main_ll_8);
		
		sp = getSharedPreferences("config", MODE_PRIVATE);
		
		//进入设置中心
		imageView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(HomeActivity.this, SettingActivity.class);
				startActivity(intent);
			}
		});
		
		//进入手机防盗页面
		linearLayout_1.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				showLostFindDialog();
			}
		});
		
		//进入通讯卫士
		linearLayout_2.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(HomeActivity.this, CallSmsSafeActivity.class);
				startActivity(intent);
			}
		});
		
		//进入软件管理器
		linearLayout_3.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(HomeActivity.this, AppManagerActivity.class);
				startActivity(intent);
			}
		});
		//进入进程管理器
		linearLayout_4.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(HomeActivity.this, TaskManagerActivity.class);
				startActivity(intent);
			}
		});
		
		//进入手机杀毒
		linearLayout_5.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(HomeActivity.this, AntiVirusActivity.class);
				startActivity(intent);
			}
		});
		
		//进入流量统计
		linearLayout_6.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(HomeActivity.this, TrafficManagerActivity.class);
				startActivity(intent);
			}
		});
		
		//进入缓存清理
		linearLayout_7.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(HomeActivity.this, CleanCacheActivity.class);
				startActivity(intent);
			}
		});
		
		//进入高级工具页面
		linearLayout_8.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(HomeActivity.this, AtoolsActivity.class);
				startActivity(intent);
			}
		});
	}

	protected void showLostFindDialog() {

		//判断是否设置过密码
		if(isSetupPwd()){
			//已经设置密码了，弹出的是输入密码对话框
			showEnterDialog();
		}else{
			//没有设置密码，弹出的是设置密码对话框
			showSetupPwdDialog();
		}
	}

	/**
	 * 设置密码对话框
	 */
	private void showSetupPwdDialog() {

		AlertDialog.Builder builder = new Builder(HomeActivity.this);
		View view = View.inflate(HomeActivity.this, R.layout.dialog_setup_password, null);
		
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
					Toast.makeText(HomeActivity.this, "密码为空", 0).show();
					return ;
				}
				//判断是否一致才去保存
				if(password.equals(password_confirm)){
					//一致的话，就保存密码，把对话框消掉，还要进入手机防盗页面
					Editor editor = sp.edit();
					editor.putString("password", MD5Utils.md5Password(password));//保存加密后的密码
					editor.commit();
					dialog.dismiss();
					Intent intent = new Intent(HomeActivity.this,LostFindActivity.class);
					startActivity(intent);
				}else{
					Toast.makeText(HomeActivity.this, "密码不一致", 0).show();
					return ;
				}
			}
		});
	}
	
	/**
	 * 输入密码对话框
	 */
	private void showEnterDialog() {

		AlertDialog.Builder builder = new Builder(HomeActivity.this);
		// 自定义一个布局文件
		View view = View.inflate(HomeActivity.this, R.layout.dialog_enter_password, null);
		et_setup_pwd = (EditText) view.findViewById(R.id.et_setup_pwd);
		ok = (Button) view.findViewById(R.id.ok);
		cancel = (Button) view.findViewById(R.id.cancel);
		cancel.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//把这个对话框取消掉
				dialog.dismiss();
			}
		});
		ok.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//  取出密码
				String password = et_setup_pwd.getText().toString().trim();
				String savePassword = sp.getString("password", "");//取出加密后的
				if(TextUtils.isEmpty(password)){
					Toast.makeText(HomeActivity.this, "密码为空", 1).show();
					return;
				}
				
				if((MD5Utils.md5Password(password).equals(savePassword))){
					//输入的密码是我之前设置的密码
					//把对话框消掉，进入主页面；
					dialog.dismiss();
				//	Log.i(TAG, "把对话框消掉，进入手机防盗页面");
					Intent intent = new Intent(HomeActivity.this,LostFindActivity.class);
					startActivity(intent);
					
				}else{
					Toast.makeText(HomeActivity.this, "密码错误", 1).show();
					et_setup_pwd.setText("");
					return;
				}
			}
		});
		dialog = builder.create();
		dialog.setView(view, 0, 0, 0, 0);
		dialog.show();
	}

	/**
	 * 判断是否设置过密码
	 * @return
	 */
	private boolean isSetupPwd() {
		String password = sp.getString("password", null);
		if(TextUtils.isEmpty(password))
			return false;
		else
			return true;
	}	
}
