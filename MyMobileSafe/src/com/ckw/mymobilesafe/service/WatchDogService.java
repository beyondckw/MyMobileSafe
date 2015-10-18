package com.ckw.mymobilesafe.service;

import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

import com.ckw.mymobilesafe.EnterPwdActivity;
import com.ckw.mymobilesafe.db.dao.ApplockDao;

/**
 * ���Ź����� ����ϵͳ���������״̬
 * 
 * @author Administrator
 * 
 */
public class WatchDogService extends Service {
	private ActivityManager am;
	private boolean flag;
	private ApplockDao dao;
	private InnerReceiver innerReceiver;
	private String tempStopProtectPackname;
	private ScreenOffReceiver offreceiver;
	private DataChangeReceiver dataChangeReceiver;
	
	private List<String> protectPacknames;
	private Intent intent;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	private class ScreenOffReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			tempStopProtectPackname = null;
		}
	}
	private class InnerReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			System.out.println("���յ�����ʱֹͣ�����Ĺ㲥�¼�");
			tempStopProtectPackname = intent.getStringExtra("packname");
		}
	}
	
	private class DataChangeReceiver extends BroadcastReceiver{
		@Override
		public void onReceive(Context context, Intent intent) {
			System.out.println("���ݿ�����ݱ仯�ˡ�����");
			protectPacknames = dao.findAll();
		}
	}

	@Override
	public void onCreate() {
		offreceiver = new ScreenOffReceiver();
		registerReceiver(offreceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
		innerReceiver = new InnerReceiver();
		registerReceiver(innerReceiver, new IntentFilter(
				"com.ckw.mymobilesafe.tempstop"));
		dataChangeReceiver = new DataChangeReceiver();
		registerReceiver(dataChangeReceiver, new IntentFilter("com.ckw.mymobilesafe.applockchange"));

		am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		dao = new ApplockDao(this);
		protectPacknames = dao.findAll();
		flag = true;
		intent = new Intent(getApplicationContext(),
				EnterPwdActivity.class);
		 
		// ������û������ջ��Ϣ�ģ��ڷ�����activity��Ҫָ�����activity���е�����ջ
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		
		new Thread() {
			public void run() {
				while (flag) {
					List<RunningTaskInfo> infos = am.getRunningTasks(1);
					//�õ���һ��ջ��ջ�������û����ڲ�����Activity
					String packname = infos.get(0).topActivity.getPackageName();
					//System.out.println("��ǰ�û�������Ӧ�ó���:" + packname);// ��ѵ���ˡ�
					//if (dao.find(packname)) {//��ѯ���ݿ�̫���ˣ�������Դ���ĳɲ�ѯ�ڴ�
					if(protectPacknames.contains(packname)){//��ѯ�ڴ�Ч�ʸߺܶ�
						// �ж����Ӧ�ó����Ƿ���Ҫ��ʱ��ֹͣ������
						if (packname.equals(tempStopProtectPackname)) {
							//����һ���Ѿ������ĳ���
						} else {
							// ��ǰӦ����Ҫ�������ĳ�����������һ����������Ľ��档
							// ����Ҫ��������İ���
							intent.putExtra("packname", packname);
							startActivity(intent);
						}
					}
					try {
						Thread.sleep(50);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			};
		}.start();
		super.onCreate();
	}

	@Override
	public void onDestroy() {
		flag = false;
		unregisterReceiver(innerReceiver);
		innerReceiver = null;
		unregisterReceiver(offreceiver);
		offreceiver = null;
		unregisterReceiver(dataChangeReceiver);
		dataChangeReceiver = null;
		super.onDestroy();
	}

}
