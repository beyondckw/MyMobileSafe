package com.ckw.mymobilesafe.receiver;

import com.ckw.mymobilesafe.R;
import com.ckw.mymobilesafe.service.GPSService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.text.TextUtils;

public class SMSReceiver extends BroadcastReceiver{

	protected SharedPreferences sp;
	private String phone;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		
		sp = context.getSharedPreferences("config", Context.MODE_PRIVATE);
		phone = sp.getString("phone", "");
		
		// 接收短信的代码
		Object []objs = (Object[]) intent.getExtras().get("pdus");
		for(Object b : objs){
			//具体的某一条短信
			SmsMessage sms = SmsMessage.createFromPdu((byte[]) b);
			String sender = sms.getOriginatingAddress();
			String body = sms.getMessageBody();
			
	//		if(phone.equals(sender)){    //安全号码发过来的信息
				if("#*location*#".equals(body)){
					//得到手机的GPS
					System.out.println("得到手机的GPS");
					
					//启动服务
					Intent i = new Intent(context, GPSService.class);
					context.startService(i);
					String lastlocation = sp.getString("lastlocation", null);
					if(TextUtils.isEmpty(lastlocation)){
						//位置没有得到
						SmsManager.getDefault().sendTextMessage(sender, null, "getting location...", null, null);
					}else{
						SmsManager.getDefault().sendTextMessage(sender, null, lastlocation, null, null);
					}
					
					//把广播终止掉,只允许在本程序接收(拦截处理)
					abortBroadcast();
				}else if("#*alarm*#".equals(body)){
					//播放报警音乐
					System.out.println("播放报警音乐");
					MediaPlayer player = MediaPlayer.create(context, R.raw.ylzs);
					//player.setLooping(true);
					player.setVolume(1.0f, 1.0f);
					player.start();
					
					//把广播终止掉,只允许在本程序接收(拦截处理)
					abortBroadcast();
				}else if("#*wipedata*#".equals(body)){
					//远程清除数据
					System.out.println("远程清除数据");
					//把广播终止掉,只允许在本程序接收(拦截处理)
					abortBroadcast();
				}else if("#*lockscreen*#".equals(body)){
					//远程锁屏
					System.out.println("远程锁屏");
					//把广播终止掉,只允许在本程序接收(拦截处理)
					abortBroadcast();
				}
	//		}
		}
		
	}

}
