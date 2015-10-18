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
		
		// ���ն��ŵĴ���
		Object []objs = (Object[]) intent.getExtras().get("pdus");
		for(Object b : objs){
			//�����ĳһ������
			SmsMessage sms = SmsMessage.createFromPdu((byte[]) b);
			String sender = sms.getOriginatingAddress();
			String body = sms.getMessageBody();
			
	//		if(phone.equals(sender)){    //��ȫ���뷢��������Ϣ
				if("#*location*#".equals(body)){
					//�õ��ֻ���GPS
					System.out.println("�õ��ֻ���GPS");
					
					//��������
					Intent i = new Intent(context, GPSService.class);
					context.startService(i);
					String lastlocation = sp.getString("lastlocation", null);
					if(TextUtils.isEmpty(lastlocation)){
						//λ��û�еõ�
						SmsManager.getDefault().sendTextMessage(sender, null, "getting location...", null, null);
					}else{
						SmsManager.getDefault().sendTextMessage(sender, null, lastlocation, null, null);
					}
					
					//�ѹ㲥��ֹ��,ֻ�����ڱ��������(���ش���)
					abortBroadcast();
				}else if("#*alarm*#".equals(body)){
					//���ű�������
					System.out.println("���ű�������");
					MediaPlayer player = MediaPlayer.create(context, R.raw.ylzs);
					//player.setLooping(true);
					player.setVolume(1.0f, 1.0f);
					player.start();
					
					//�ѹ㲥��ֹ��,ֻ�����ڱ��������(���ش���)
					abortBroadcast();
				}else if("#*wipedata*#".equals(body)){
					//Զ���������
					System.out.println("Զ���������");
					//�ѹ㲥��ֹ��,ֻ�����ڱ��������(���ش���)
					abortBroadcast();
				}else if("#*lockscreen*#".equals(body)){
					//Զ������
					System.out.println("Զ������");
					//�ѹ㲥��ֹ��,ֻ�����ڱ��������(���ش���)
					abortBroadcast();
				}
	//		}
		}
		
	}

}
