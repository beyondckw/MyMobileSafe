package com.ckw.mymobilesafe;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class Setup4Activity extends BaseSetupActivity {
	
	private SharedPreferences sp;
	private CheckBox cb;
	private Editor editor;
	protected boolean protecting;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setup4);
		
		cb = (CheckBox) findViewById(R.id.cb_setup4);
		sp = getSharedPreferences("config", MODE_PRIVATE);
		protecting = sp.getBoolean("protecting", false);
		if(protecting == false){
			//没有开启手机防盗
			cb.setChecked(false);
			cb.setText("您没有开启防盗保护");
		}else{
			//已经开启了手机防盗
			cb.setChecked(true);
			cb.setText("防盗保护已经开启");
		}
		
		//选中是调用OncheckChangeListenr()方法
		cb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

				if(isChecked){
					//已经开启了手机防盗
					protecting = true;
					cb.setChecked(true);
					cb.setText("防盗保护已经开启");
				}
			}
		});
		
		//而取消选中就OnClickListner()中 if(!((CheckBox) v).isChecked())
		cb.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(!((CheckBox)v).isChecked()){
					//没有开启手机防盗
					protecting = false;
					cb.setChecked(false);
					cb.setText("您没有开启防盗保护");
				}
				
			}
		});
	}
	

	@Override
	public void showNext() {
		editor = sp.edit();
		editor.putBoolean("protecting", protecting);
		editor.putBoolean("configed", true);
		editor.commit();
		Intent intent = new Intent(this,LostFindActivity.class);
		startActivity(intent);
		finish();
		overridePendingTransition(R.anim.tran_in, R.anim.tran_out);
		
	}

	@Override
	public void showPre() {
		Intent intent = new Intent(this,Setup3Activity.class);
		startActivity(intent);
		finish();
		overridePendingTransition(R.anim.tran_pre_in, R.anim.tran_pre_out);
		
	}

}
