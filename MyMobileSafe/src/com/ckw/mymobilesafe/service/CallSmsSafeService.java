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
			Log.i(TAG,"�ڲ��㲥�����ߣ� ���ŵ�����");
			//��鷢�����Ƿ��Ǻ��������룬��������Ƿ��Ƕ������ػ�ȫ�����ء�
			Object[] objs = (Object[]) intent.getExtras().get("pdus");
			for(Object obj:objs){
				SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) obj);
				//�õ����ŷ�����
				String sender = smsMessage.getOriginatingAddress();
				String result = dao.findMode(sender);
				if("2".equals(result)||"3".equals(result)){
					Log.i(TAG,"���ض���");
					abortBroadcast();
				}
				//��ʾ���롣
				String body = smsMessage.getMessageBody();
				if(body.contains("fapiao")){
					//���ͷ��Ʊ���ĺ�  ���Էִʼ�����
					Log.i(TAG,"���ط�Ʊ����");
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
			case TelephonyManager.CALL_STATE_RINGING://����״̬��
				String result = dao.findMode(incomingNumber);
				if("1".equals(result)||"3".equals(result)){
					Log.i(TAG,"�Ҷϵ绰��������");
					
					//�Ҷϵ绰
					endCall();
					//��������¼
					//�۲���м�¼���ݿ����ݵı仯���Ա�ɾ��
					Uri url = Uri.parse("content://call_log/calls");
					getContentResolver().registerContentObserver(url, true, new CallLogObserver(incomingNumber, new Handler()));
					
				}
				break;
			}
			super.onCallStateChanged(state, incomingNumber);
		}
	}
	
	//���ݹ۲���
	private class CallLogObserver extends ContentObserver{

		private String incomingNumber;
		
		public CallLogObserver(String incomingNumber, Handler handler) {
			super(handler);
			this.incomingNumber = incomingNumber;
		}

		@Override
		public void onChange(boolean selfChange) {
			deleteLog(incomingNumber);
			System.out.println("ɾ��ͨ����¼");
			getContentResolver().unregisterContentObserver(this);
			super.onChange(selfChange);
		}
		
	}

	public void endCall() {
		
		/*
		 * ���������Androidϵͳ�����������ˣ�����Ҫ�÷�������ҳ����������࣬���ҷ���
		 * IBinder iBinder = ServiceManager.getService(TELEPHONY_SERVICE);
		 */
		
		try {
			Class clazz = null;
			
			/*try {
				clazz =  Class.forName("android.os.ServiceManager");
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}*/
			
			//����servicemanager���ֽ���
			clazz =CallSmsSafeService.this.getClassLoader().loadClass("android.os.ServiceManager");
			Method method = clazz.getDeclaredMethod("getService", String.class);
			IBinder ibinder = (IBinder) method.invoke(null, TELEPHONY_SERVICE);
			ITelephony.Stub.asInterface(ibinder).endCall();//����һ�������������е�Զ�̷��񷽷�
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * ����ContentProvider�õ�ϵͳ����ϵ��Ӧ�õ����ݿ⣨������ļ�¼����������ݿ����棩
	 */
	public void deleteLog(String incomingNumber) {

		ContentResolver resolver = getContentResolver();
		//���м�¼��Uri�����������������ݿ���
		Uri url = Uri.parse("content://call_log/calls");
		resolver.delete(url, "number=?", new String[] {incomingNumber});
	}
}
