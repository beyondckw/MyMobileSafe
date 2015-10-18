package com.ckw.mymobilesafe;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxCallBack;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.animation.AlphaAnimation;
import android.widget.TextView;
import android.widget.Toast;

import com.ckw.mymobilesafe.utils.StreamTools;

public class SplashActivity extends Activity{

	protected static final int ENTER_HOME = 0;
	protected static final int SHOW_UPDATE_DIALOG = 1;
	protected static final int URL_ERROR = 2;
	protected static final int NETWORK_ERROR = 3;
	protected static final int JSON_ERROR = 4;
	private TextView tv_splash_version;
	protected String tag = "SplashActivity";
	protected String version;
	protected String description;
	protected String apkurl;
	private TextView tv_update_info;
	
	int code;  //�������緵�ص�״̬��
	HttpURLConnection conn;
	SharedPreferences sp;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);
		tv_splash_version = (TextView)findViewById(R.id.tv_splash_version);
		tv_splash_version.setText("�汾�ţ�" + getVersion());
		tv_update_info = (TextView)findViewById(R.id.tv_update_info);
		
		sp = getSharedPreferences("config", MODE_PRIVATE);
		//��ȡ������update�������Ƿ���Ҫ�Զ�����
		boolean update = sp.getBoolean("update", true);
		
		//����Ӧ�ó���Ŀ��ͼ��
		installShortCut();
		
		//�������ݿ����
		copyDB("address.db");
		copyDB("antivirus.db");
		
		if(update){
			checkUpdate();
		}else{
			handler.postDelayed(new Runnable() {
				
				@Override
				public void run() {
					//������ҳ��
					enterHome();
					
				}
			}, 2000);		//�ӳ����������ҳ��
		}
		
		//����
		AlphaAnimation aa = new AlphaAnimation(0.2f, 1.0f);
		aa.setDuration(2000);
		findViewById(R.id.rl_root_splash).startAnimation(aa);
		
	}
	
	private void installShortCut() {
		//�ж��Ƿ��Ѿ���װ����ݷ�ʽ��
		boolean shortcut = sp.getBoolean("shortcut", false);
		if(shortcut){
			return ;
		}
		
		//���͹㲥����ͼ
		Intent intent = new Intent();
		intent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");//����ϵͳ��ĳһ��Ӧ����������ݷ�ʽ
		//��ݷ�ʽ����Ҫ����������Ҫ����Ϣ��1������    2��ͼ��   3����ݷ�ʽҪ������Ӧ�ó���
		intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, "�ֻ���ʿ");
		Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
		intent.putExtra(Intent.EXTRA_SHORTCUT_ICON, bitmap);
		
		//������ͼ���Ӧ����ͼ
		Intent shortcutIntent = new Intent();
		shortcutIntent.setAction("android.intent.action.MAIN");
		shortcutIntent.addCategory("android.intent.category.LAUNCHER");
		shortcutIntent.setClassName(getPackageName(), "com.ckw.mymobilesafe.SplashActivity");
		intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
		
		Editor editor = sp.edit();
		editor.putBoolean("shortcut", true);
		editor.commit();
		
		//���͹㲥
		sendBroadcast(intent);
	}

	//��address.db������data/data/<����>/files/address.db
	//��Ϊ�����assets�µ��ļ�ֻҪͼƬ�ļ��ſ���ֱ����
	private void copyDB(String filename) {
		
		try {
			File file = new File(getFilesDir(), filename);
			if(file.exists() && file.length()>0){//ֻҪ�������Ͳ�Ҫ������
				Log.i(tag, "�Ѿ������ļ���");
			}else{
				InputStream is = getAssets().open(filename);
				FileOutputStream fos = new FileOutputStream(file);
				byte []buffer = new byte[1024];
				int len = 0;
				while((len=is.read(buffer)) != -1){
					fos.write(buffer, 0, len);
				}
				is.close();
				fos.close();
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private Handler handler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch(msg.what){
			case ENTER_HOME:
				enterHome();
				break;
			case SHOW_UPDATE_DIALOG:
				Log.i(tag, "��ʾ�����ĶԻ���");
				showUpdataDialog();
				break;
			case URL_ERROR:
				enterHome();
				Toast.makeText(getApplicationContext(), "URL����", 0).show();
				break;
			case NETWORK_ERROR:
				enterHome();
				Toast.makeText(getApplicationContext(), "�����쳣", 0).show();
				break;
			case JSON_ERROR:
				enterHome();
				Toast.makeText(SplashActivity.this, "JSON��������", 0).show();
				break;
			default:
				break;
			}
		}

	};
	
	protected void enterHome() {
		Intent intent = new Intent(this, HomeActivity.class);
		startActivity(intent);
		// �ر�����ҳ��
		finish();
	}
	
	//��ʾ�����Ի���
	protected void showUpdataDialog() {
		AlertDialog.Builder builder = new Builder(this);
		builder.setCancelable(false);//���ð����ؼ�����ȡ��
		builder.setTitle("���°汾��������");
		builder.setMessage(description);
		builder.setPositiveButton("��������", new OnClickListener(){

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// ����APK�������滻��װ
				if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
					//sdcard����
					//afinal
					FinalHttp finalHttp = new FinalHttp();
					finalHttp.download(apkurl, 
							Environment.getExternalStorageDirectory().getAbsolutePath()
							+"/mobilesafe.apk", new AjaxCallBack<File>() {

								@Override
								public void onFailure(Throwable t, int errorNo,
										String strMsg) {
									t.printStackTrace();
									Toast.makeText(getApplicationContext(), "����ʧ��", 1).show();
									super.onFailure(t, errorNo, strMsg);
								}

								@Override
								public void onLoading(long count, long current) {
									// TODO Auto-generated method stub
									super.onLoading(count, current);
									
									int progress = (int) (current*100/count);
									tv_update_info.setText("���ؽ��ȣ�" + progress +"%");
								}

								@Override
								public void onSuccess(File t) {
									// TODO Auto-generated method stub
									super.onSuccess(t);
									installAPK(t);
								}
								
								/**
								 * ��װapk�ļ�
								 * @param t
								 */
								private void installAPK(File t) {
									Intent intent = new Intent();
									intent.setAction("android.intent.action.VIEW");
									intent.addCategory("android.intent.category.DEFAULT");
									intent.setDataAndType(Uri.fromFile(t), "application/vnd.android.package-archive");
									startActivity(intent);
								}
								
							});
				}else{
					Toast.makeText(getApplicationContext(), "û��sdcard�������豸������", 0).show();
					return;
				}
			}
		});
		builder.setNegativeButton("�´���˵", new OnClickListener(){

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// ֱ�ӽ�����ҳ��
				dialog.dismiss();
				enterHome();
			}
		});
		builder.show();
	}

	/**
	 * ����Ƿ����°汾������о�����
	 */
	private void checkUpdate() {

		new Thread(){
			public void run(){
				Message msg = Message.obtain();
				
				long startTime = System.currentTimeMillis();
				
				try {
					
					URL url = new URL(getString(R.string.serverurl));
					//����
					conn = (HttpURLConnection)url.openConnection();
					conn.setRequestMethod("GET");
				//	conn.setReadTimeout(2000);
					conn.setConnectTimeout(5000);
					code = conn.getResponseCode();			//���Ǹ�����ʽ�ķ�����Ҫע��
					
					if(code == 200){
						//�����ɹ�
						InputStream is = conn.getInputStream();
						String result = StreamTools.readFromStream(is);
						Log.i(tag , "�����ɹ�"+ result);
						
						//JSON����
						JSONObject obj = new JSONObject(result);
						version = (String)obj.get("version");
						description = (String)obj.get("description");
						apkurl = (String)obj.get("apkurl");
						System.out.println(apkurl);
						if(getVersion().equals(version)){
							//�汾һ�£����ø��£�ֱ�ӽ�����ҳ��
							msg.what = ENTER_HOME;
						}else{
							//���°汾������һ�������Ի���
							msg.what = SHOW_UPDATE_DIALOG;
						}
					}
					
				} catch (MalformedURLException e) {
					msg.what = URL_ERROR;
					e.printStackTrace();
				} catch (IOException e) {
					System.out.println("444");
					msg.what = NETWORK_ERROR;
					e.printStackTrace();
				} catch (JSONException e) {
					msg.what = JSON_ERROR;
					e.printStackTrace();
				}finally{
					long endTime = System.currentTimeMillis();
					long dTime = endTime - startTime;
					System.out.println(dTime/1000);
					//ͣ��2����
					if(dTime<2000){
						try {
							Thread.sleep(2000-dTime);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					handler.sendMessage(msg);
				}
			}
		}.start();
		
	}

	/**
	 * �õ���ǰ�汾��
	 */
	private String getVersion(){
		
		PackageManager pm = getPackageManager();
		//�õ�ָ��APK��Manifest
		try {
			PackageInfo info = pm.getPackageInfo(getPackageName(), 0);
			return info.versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			return "����д����";
		}
	}
}
