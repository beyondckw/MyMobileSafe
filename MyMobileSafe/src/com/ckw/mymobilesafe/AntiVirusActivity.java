package com.ckw.mymobilesafe;

import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ckw.mymobilesafe.db.dao.AntivirusDao;

public class AntiVirusActivity extends Activity {
	
	protected static final int SCANING = 0;
	protected static final int FINISHED = 1;
	protected static final int REFRESH = 2;
	private ImageView iv_scan;
	private ProgressBar progressBar1;
	private PackageManager pm;
	private TextView tv_scan_status;
	private LinearLayout ll_container;
	private List<String> packname = new ArrayList<String>();
	private Map<String, View> removeviews = new HashMap<String, View>();  //��Ҫɾ����Ӧ�ó���ӽ������Ƴ���
	
	private int count = 0; //��¼�����ĸ���
	private Toast toast;
	
	
	private Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case SCANING:
				ScanInfo scanInfo = (ScanInfo)msg.obj;
				tv_scan_status.setText("����ɨ�裺"+scanInfo.name);
				
				TextView tv = new TextView(getApplicationContext());
				if(scanInfo.isvirus){
					tv.setTextColor(Color.RED);
					tv.setText("���ֲ�����" + scanInfo.name);
					removeviews.put(scanInfo.packname, tv);
				}else{
					tv.setTextColor(Color.BLACK);
					tv.setText("ɨ�谲ȫ��" + scanInfo.name);
				}
				ll_container.addView(tv, 0);
				break;

			case FINISHED:
				//ɨ�����
				tv_scan_status.setText("ɨ�����");
				iv_scan.clearAnimation();
				
				if(count > 0){
					
					AlertDialog.Builder builder = new Builder(AntiVirusActivity.this);
					builder.setTitle("����" + count +"�����в�����Ӧ�ó���");
					builder.setMessage("������в���Ӧ�ó���");
					builder.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							for(int i=0; i<packname.size(); i++){
								uninstallAppliation(packname.get(i), i);
							}
						}
					});
					builder.setNegativeButton("ȡ��", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							
							showToast("�����ֻ��л�����"+count+"�����в�����Ӧ�ó���\nǿ�ҽ�������ɨ��һ����г������");
						}
					});
					builder.show();
					
				}else{
					showToast("��ϲ�������ļ���ɨ�谲ȫ");
				}
				break;
				
			case REFRESH:
				
				if(count == 0){
					showToast("��ϲ�����в�������ɱ�ɹ�");
					//Toast.makeText(getApplicationContext(), "��ϲ�����в�������ɱ�ɹ�", 0).show();
				}else{
					showToast("�����ֻ��л�����"+count+"�����в�����Ӧ�ó���\nǿ�ҽ�������ɨ��һ����г������");
					//Toast.makeText(getApplicationContext(), "�����ֻ��л�����"+count+"�����в�����Ӧ�ó���\nǿ�ҽ�������ɨ��һ����г������", 0).show();
				}
				break;
			}
		};
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_anti_virus);
		
		ll_container = (LinearLayout) findViewById(R.id.ll_container);
		tv_scan_status = (TextView) findViewById(R.id.tv_scan_status);
		iv_scan = (ImageView) findViewById(R.id.iv_scan);
		
		//���ö���
		RotateAnimation ra = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		ra.setDuration(3000);
		ra.setRepeatCount(Animation.INFINITE);
		iv_scan.startAnimation(ra);
		
		progressBar1 = (ProgressBar) findViewById(R.id.progressBar1);
		scanVirus();
		
		
	}

	/*
	 * ɨ�財��
	 */
	private void scanVirus() {
		pm = getPackageManager();
		tv_scan_status.setText("���ڳ�ʼ��8��ɱ������");
		
		new Thread(){
			public void run() {
				
				List<PackageInfo> infos = pm.getInstalledPackages(0);
				progressBar1.setMax(infos.size());
				int progress = 0;
				for(PackageInfo info : infos){
					//apk�ļ�������·����Android�ֻ��Ĳ��������apk����ʽ���ڣ�����Ҫɨ��apk��
					String sourcedir = info.applicationInfo.sourceDir;
					String md5 = getFileMd5(sourcedir);
					
					ScanInfo scaninfo = new ScanInfo();
					scaninfo.name = info.applicationInfo.loadLabel(pm).toString();
					scaninfo.packname = info.packageName;
					System.out.println(scaninfo.name + "---------"+md5);
					//��ѯMD5��Ϣ�Ƿ��ڲ���������
					boolean result = AntivirusDao.isVirus(md5);
					if(result){
						//�����ļ�
						scaninfo.isvirus = true;
						count++;
						packname.add(scaninfo.packname);
					}else{
						//�ǲ����ļ�
						scaninfo.isvirus = false;
					}
					
					Message msg = Message.obtain();
					msg.obj = scaninfo;
					msg.what = SCANING;
					handler.sendMessage(msg);
					
					progress++;
					progressBar1.setProgress(progress);
				}

				Message msg = Message.obtain();
				msg.what = FINISHED;
				handler.sendMessage(msg);
			};
		}.start();
		
	}
	
	/**
	 * 
	 *ɨ����Ϣ���ڲ���
	 */
	class ScanInfo{
		String packname;
		String name;
		boolean isvirus;
	}
	
	/*
	 * ��ȡ�ļ���MD5ֵ
	 */
	private String getFileMd5(String path){
		
		File file = new File(path);
		StringBuffer sb = null;
		try {
			MessageDigest digest = MessageDigest.getInstance("md5");
			FileInputStream fis = new FileInputStream(file);
			byte []buffer = new byte[1024];
			int len = -1;
			while((len = fis.read(buffer)) != -1){
				digest.update(buffer, 0, len);
			}
			byte []result = digest.digest();
			sb = new StringBuffer();
			for(byte b : result){
				int number = b & 0xff;
				String str = Integer.toHexString(number);
				if(str.length() == 1){
					sb.append("0");
				}
				sb.append(str);
			}
			fis.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
		return sb.toString();
	}
	
	/**
	 * �����ǲ���
	 * ж��Ӧ��
	 */
	private void uninstallAppliation(String packname, int i) {
		
		Intent intent = new Intent();
		intent.setAction("android.intent.action.VIEW");
		intent.setAction("android.intent.action.DELETE");
		intent.addCategory("android.intent.category.DEFAULT");
		intent.setData(Uri.parse("package:" + packname));
		
		//intent.getDataString()
		startActivityForResult(intent, i);
	}
	
	@Override
	protected void onActivityResult(final int requestCode, int resultCode, Intent data) {
		
		new Thread(){
			public void run() {
			    boolean flag = false;      //ж�سɹ�
				pm = getPackageManager();
				
				List<PackageInfo> infos = pm.getInstalledPackages(0);
				for(PackageInfo info : infos){
					if(packname.get(requestCode).equals(info.packageName)){ //��ϵͳ�İ�װ�����л��������������˵��ж�ز��ɹ�
						flag = true;
						break;
					}
				}
				
				if(flag == false){   
					count--;
					runOnUiThread(new Runnable() {
						
						@Override
						public void run() {      // ˢ�½��档
							ll_container.removeView(removeviews.get(packname.get(requestCode)));
						}
					});
				}
				Message msg = Message.obtain();
				msg.what = REFRESH;
				handler.sendMessage(msg);
			};
		}.start();
	
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void showToast(String text) {
		if(toast == null){
			toast = Toast.makeText(getApplicationContext(), text, 0);
		}else{
			toast.setText(text);
			toast.setDuration(0);
		}
		toast.show();
	}
	
}




