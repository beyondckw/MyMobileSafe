package com.ckw.mymobilesafe.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.telephony.TelephonyManager;
import android.widget.Toast;

/**
 * 启动手机时，判断SIM卡是否改变
 * @author Administrator
 *
 */
public class BootCompleteReceiver extends BroadcastReceiver {

	private SharedPreferences sp;
	private TelephonyManager tm;
	@Override
	public void onReceive(Context context, Intent intent) {

		//读取之前保存的sim卡信息
		sp = context.getSharedPreferences("config", Context.MODE_PRIVATE);
		String saveSim = sp.getString("sim", "");
		
		//读取当前的sim卡信息
		tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		String realSim = tm.getSimSerialNumber();
		
		//比较是否一样
		if(saveSim.equals(realSim)){
			
		}else{ 
			//sim卡已变更
			System.out.println("hhh");
			Toast.makeText(context, "haha", 1).show();
		}
	}

}
