package com.ckw.mymobilesafe;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ckw.mymobilesafe.utils.MD5Utils;

public class EnterPwdActivity extends Activity {
	private EditText et_password;
	private String packname;
	private TextView tv_name;
	private ImageView iv_icon;
	
	private SharedPreferences sp;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_enter_pwd);
		
		sp = getSharedPreferences("config", MODE_PRIVATE);
		
		et_password = (EditText) findViewById(R.id.et_password);
		Intent intent = getIntent();
		//��ǰҪ������Ӧ�ó������
		packname = intent.getStringExtra("packname");
		tv_name = (TextView) findViewById(R.id.tv_name);
		iv_icon = (ImageView) findViewById(R.id.iv_icon);
		
		//����ϵͳ�����ڸǽ�����²��ֿհ׵ط�
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
		
		/*InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE); 
		View view = View.inflate(getApplicationContext(), R.layout.activity_enter_pwd, null);
		imm.showSoftInput(view,InputMethodManager.SHOW_FORCED);  */
				
		PackageManager pm = getPackageManager();
		try {
			ApplicationInfo info = pm.getApplicationInfo(packname, 0);
			tv_name.setText(info.loadLabel(pm));
			iv_icon.setImageDrawable(info.loadIcon(pm));
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	@Override
	public void onBackPressed() {
		//�����档
//        <action android:name="android.intent.action.MAIN" />
//        <category android:name="android.intent.category.HOME" />
//        <category android:name="android.intent.category.DEFAULT" />
//        <category android:name="android.intent.category.MONKEY"/>
		Intent intent = new Intent();
		intent.setAction("android.intent.action.MAIN");
		intent.addCategory("android.intent.category.HOME");
		intent.addCategory("android.intent.category.DEFAULT");
		intent.addCategory("android.intent.category.MONKEY");
		startActivity(intent);
		//���е�activity��С�� ����ִ��ondestory ִֻ�� onstop������
		
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		finish();
	}
	
	public void click(View view){
		//ȡ������
		String savePassword = sp.getString("appLockPassword", "");//ȡ�����ܺ��
		String pwd = et_password.getText().toString().trim();
		
		if(TextUtils.isEmpty(pwd)){
			Toast.makeText(this, "���벻��Ϊ��", 0).show();
			return;
		}
		
		if((MD5Utils.md5Password(pwd).equals(savePassword))){
			//���߿��Ź������������������ȷ�ˡ� ������ʱ��ֹͣ������
			//�Զ���Ĺ㲥,��ʱֹͣ������
			Intent intent = new Intent();
			intent.setAction("com.ckw.mymobilesafe.tempstop");
			intent.putExtra("packname", packname);
			sendBroadcast(intent);
			finish();
		}else{
			Toast.makeText(this, "������󡣡�", 0).show();
		}

	}
}
