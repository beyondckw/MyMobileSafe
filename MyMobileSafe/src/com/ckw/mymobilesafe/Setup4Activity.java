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
			//û�п����ֻ�����
			cb.setChecked(false);
			cb.setText("��û�п�����������");
		}else{
			//�Ѿ��������ֻ�����
			cb.setChecked(true);
			cb.setText("���������Ѿ�����");
		}
		
		//ѡ���ǵ���OncheckChangeListenr()����
		cb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

				if(isChecked){
					//�Ѿ��������ֻ�����
					protecting = true;
					cb.setChecked(true);
					cb.setText("���������Ѿ�����");
				}
			}
		});
		
		//��ȡ��ѡ�о�OnClickListner()�� if(!((CheckBox) v).isChecked())
		cb.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(!((CheckBox)v).isChecked()){
					//û�п����ֻ�����
					protecting = false;
					cb.setChecked(false);
					cb.setText("��û�п�����������");
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
