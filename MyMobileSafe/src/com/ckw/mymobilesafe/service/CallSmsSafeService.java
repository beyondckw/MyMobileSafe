package com.ckw.mymobilesafe.service;

import java.lang.reflect.Method;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.android.internal.telephony.ITelephony;
import com.ckw.mymobilesafe.db.dao.BlackNumberDao;

public class CallSmsSafeService extends Service {
	public static final String TAG = "CallSmsSafeService";
	private InnerSmsReceiver receiver;
	private BlackNumberDao dao;
	private TelephonyManager tm;
	private MyListener listener;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	private class InnerSmsReceiver extends BroadcastReceiver{
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.i(TAG,"内部广播接受者， 短信到来了");
			//检查发件人是否是黑名单号码，检查设置是否是短信拦截或全部拦截。
			Object[] objs = (Object[]) intent.getExtras().get("pdus");
			for(Object obj:objs){
				SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) obj);
				//得到短信发件人
				String sender = smsMessage.getOriginatingAddress();
				String result = dao.findMode(sender);
				if("2".equals(result)||"3".equals(result)){
					Log.i(TAG,"拦截短信");
					abortBroadcast();
				}
				//演示代码。
				String body = smsMessage.getMessageBody();
				if(body.contains("fapiao")){
					//你的头发票亮的很  语言分词技术。
					Log.i(TAG,"拦截发票短信");
					abortBroadcast();
				}
			}
		}
	}
	
	@Override
	public void onCreate() {
		dao = new BlackNumberDao(this);
		tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		listener = new MyListener();
		tm.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);
		receiver = new InnerSmsReceiver();
		IntentFilter filter =  new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
		filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
		registerReceiver(receiver,filter);
		super.onCreate();
	}
	
	@Override
	public void onDestroy() {
		unregisterReceiver(receiver);
		receiver = null;
		tm.listen(listener, PhoneStateListener.LISTEN_NONE);
		super.onDestroy();
	}
	
	private class MyListener extends PhoneStateListener{

		@Override
		public void onCallStateChanged(int state, String incomingNumber) {
			switch (state) {
			case TelephonyManager.CALL_STATE_RINGING://零响状态。
				String result = dao.findMode(incomingNumber);
				if("1".equals(result)||"3".equals(result)){
					Log.i(TAG,"挂断电话。。。。");
					
					//挂断电话
					endCall();
					//清除来电记录
					//观察呼叫记录数据库内容的变化，以便删除
					Uri url = Uri.parse("content://call_log/calls");
					getContentResolver().registerContentObserver(url, true, new CallLogObserver(incomingNumber, new Handler()));
					
				}
				break;
			}
			super.onCallStateChanged(state, incomingNumber);
		}
	}
	
	//内容观察者
	private class CallLogObserver extends ContentObserver{

		private String incomingNumber;
		
		public CallLogObserver(String incomingNumber, Handler handler) {
			super(handler);
			this.incomingNumber = incomingNumber;
		}

		@Override
		public void onChange(boolean selfChange) {
			deleteLog(incomingNumber);
			System.out.println("删除通话记录");
			getContentResolver().unregisterContentObserver(this);
			super.onChange(selfChange);
		}
		
	}

	public void endCall() {
		
		/*
		 * 这个方法被Android系统给隐藏起来了，所以要用反射把它找出来，先找类，再找方法
		 * IBinder iBinder = ServiceManager.getService(TELEPHONY_SERVICE);
		 */
		
		try {
			Class clazz = null;
			
			/*try {
				clazz =  Class.forName("android.os.ServiceManager");
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}*/
			
			//加载servicemanager的字节码
			clazz =CallSmsSafeService.this.getClassLoader().loadClass("android.os.ServiceManager");
			Method method = clazz.getDeclaredMethod("getService", String.class);
			IBinder ibinder = (IBinder) method.invoke(null, TELEPHONY_SERVICE);
			ITelephony.Stub.asInterface(ibinder).endCall();//另外一个进程里面运行的远程服务方法
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * 利用ContentProvider得到系统的联系人应用的数据库（打进来的记录放在这个数据库里面）
	 */
	public void deleteLog(String incomingNumber) {

		ContentResolver resolver = getContentResolver();
		//呼叫记录的Uri，包括主机名和数据库名
		Uri url = Uri.parse("content://call_log/calls");
		resolver.delete(url, "number=?", new String[] {incomingNumber});
	}
}
