package com.ckw.mymobilesafe;

import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.ckw.mymobilesafe.ui.SettingItemView;

public class Setup2Activity extends BaseSetupActivity {
	
	private SettingItemView siv_setup2_sim;
	private TelephonyManager tm; //��ȡ�ֻ�sim������Ϣ
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setup2);
		
		tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		siv_setup2_sim = (SettingItemView) findViewById(R.id.siv_setup2_sim);
		
		String sim = sp.getString("sim", null);
		if(TextUtils.isEmpty(sim)){
			//û�а�
			siv_setup2_sim.setDesc("sim��û�а�");
			siv_setup2_sim.setChecked(false);
		}else{
			//�Ѿ���
			siv_setup2_sim.setDesc("sim���Ѱ�");
			siv_setup2_sim.setChecked(true);
		}
		
		siv_setup2_sim.setOnClickListener(new OnClickListener() {
			Editor editor = sp.edit();
			@Override
			public void onClick(View v) {
				if(siv_setup2_sim.isChecked()){
					siv_setup2_sim.setChecked(false);
					siv_setup2_sim.setDesc("sim��û�а�");
					//����sim�������к�
					editor.putString("sim", null);
				}else{
					siv_setup2_sim.setChecked(true);
					siv_setup2_sim.setDesc("sim���Ѱ�");

					//����sim�������к�
					String sim = tm.getSimSerialNumber();
					editor.putString("sim", sim);
				}
				editor.commit();
			}
		});
	}
	
	@Override
	public void showNext() {
		String sim = sp.getString("sim", null);
		if(TextUtils.isEmpty(sim)){
			Toast.makeText(this, "�㻹û��SIM��", 0).show();
			return ;
		}
		Intent intent = new Intent(this,Setup3Activity.class);
		startActivity(intent);
		finish();
		overridePendingTransition(R.anim.tran_in, R.anim.tran_out);
		
	}

	@Override
	public void showPre() {
		Intent intent = new Intent(this,Setup1Activity.class);
		startActivity(intent);
		finish();
		overridePendingTransition(R.anim.tran_pre_in, R.anim.tran_pre_out);
		
		
	}

}
