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
		
		//������������
		imageView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(HomeActivity.this, SettingActivity.class);
				startActivity(intent);
			}
		});
		
		//�����ֻ�����ҳ��
		linearLayout_1.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				showLostFindDialog();
			}
		});
		
		//����ͨѶ��ʿ
		linearLayout_2.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(HomeActivity.this, CallSmsSafeActivity.class);
				startActivity(intent);
			}
		});
		
		//�������������
		linearLayout_3.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(HomeActivity.this, AppManagerActivity.class);
				startActivity(intent);
			}
		});
		//������̹�����
		linearLayout_4.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(HomeActivity.this, TaskManagerActivity.class);
				startActivity(intent);
			}
		});
		
		//�����ֻ�ɱ��
		linearLayout_5.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(HomeActivity.this, AntiVirusActivity.class);
				startActivity(intent);
			}
		});
		
		//��������ͳ��
		linearLayout_6.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(HomeActivity.this, TrafficManagerActivity.class);
				startActivity(intent);
			}
		});
		
		//���뻺������
		linearLayout_7.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(HomeActivity.this, CleanCacheActivity.class);
				startActivity(intent);
			}
		});
		
		//����߼�����ҳ��
		linearLayout_8.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(HomeActivity.this, AtoolsActivity.class);
				startActivity(intent);
			}
		});
	}

	protected void showLostFindDialog() {

		//�ж��Ƿ����ù�����
		if(isSetupPwd()){
			//�Ѿ����������ˣ�����������������Ի���
			showEnterDialog();
		}else{
			//û���������룬����������������Ի���
			showSetupPwdDialog();
		}
	}

	/**
	 * ��������Ի���
	 */
	private void showSetupPwdDialog() {

		AlertDialog.Builder builder = new Builder(HomeActivity.this);
		View view = View.inflate(HomeActivity.this, R.layout.dialog_setup_password, null);
		
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
					Toast.makeText(HomeActivity.this, "����Ϊ��", 0).show();
					return ;
				}
				//�ж��Ƿ�һ�²�ȥ����
				if(password.equals(password_confirm)){
					//һ�µĻ����ͱ������룬�ѶԻ�����������Ҫ�����ֻ�����ҳ��
					Editor editor = sp.edit();
					editor.putString("password", MD5Utils.md5Password(password));//������ܺ������
					editor.commit();
					dialog.dismiss();
					Intent intent = new Intent(HomeActivity.this,LostFindActivity.class);
					startActivity(intent);
				}else{
					Toast.makeText(HomeActivity.this, "���벻һ��", 0).show();
					return ;
				}
			}
		});
	}
	
	/**
	 * ��������Ի���
	 */
	private void showEnterDialog() {

		AlertDialog.Builder builder = new Builder(HomeActivity.this);
		// �Զ���һ�������ļ�
		View view = View.inflate(HomeActivity.this, R.layout.dialog_enter_password, null);
		et_setup_pwd = (EditText) view.findViewById(R.id.et_setup_pwd);
		ok = (Button) view.findViewById(R.id.ok);
		cancel = (Button) view.findViewById(R.id.cancel);
		cancel.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//������Ի���ȡ����
				dialog.dismiss();
			}
		});
		ok.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//  ȡ������
				String password = et_setup_pwd.getText().toString().trim();
				String savePassword = sp.getString("password", "");//ȡ�����ܺ��
				if(TextUtils.isEmpty(password)){
					Toast.makeText(HomeActivity.this, "����Ϊ��", 1).show();
					return;
				}
				
				if((MD5Utils.md5Password(password).equals(savePassword))){
					//�������������֮ǰ���õ�����
					//�ѶԻ���������������ҳ�棻
					dialog.dismiss();
				//	Log.i(TAG, "�ѶԻ��������������ֻ�����ҳ��");
					Intent intent = new Intent(HomeActivity.this,LostFindActivity.class);
					startActivity(intent);
					
				}else{
					Toast.makeText(HomeActivity.this, "�������", 1).show();
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
	 * �ж��Ƿ����ù�����
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
