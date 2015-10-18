package com.ckw.mymobilesafe.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.telephony.TelephonyManager;
import android.widget.Toast;

/**
 * �����ֻ�ʱ���ж�SIM���Ƿ�ı�
 * @author Administrator
 *
 */
public class BootCompleteReceiver extends BroadcastReceiver {

	private SharedPreferences sp;
	private TelephonyManager tm;
	@Override
	public void onReceive(Context context, Intent intent) {

		//��ȡ֮ǰ�����sim����Ϣ
		sp = context.getSharedPreferences("config", Context.MODE_PRIVATE);
		String saveSim = sp.getString("sim", "");
		
		//��ȡ��ǰ��sim����Ϣ
		tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		String realSim = tm.getSimSerialNumber();
		
		//�Ƚ��Ƿ�һ��
		if(saveSim.equals(realSim)){
			
		}else{ 
			//sim���ѱ��
			System.out.println("hhh");
			Toast.makeText(context, "haha", 1).show();
		}
	}

}
