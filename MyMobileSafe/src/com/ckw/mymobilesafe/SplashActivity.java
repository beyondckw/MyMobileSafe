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
	
	int code;  //连接网络返回的状态码
	HttpURLConnection conn;
	SharedPreferences sp;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);
		tv_splash_version = (TextView)findViewById(R.id.tv_splash_version);
		tv_splash_version.setText("版本号：" + getVersion());
		tv_update_info = (TextView)findViewById(R.id.tv_update_info);
		
		sp = getSharedPreferences("config", MODE_PRIVATE);
		//先取出参数update，看看是否需要自动升级
		boolean update = sp.getBoolean("update", true);
		
		//创建应用程序的快捷图标
		installShortCut();
		
		//拷贝数据库进来
		copyDB("address.db");
		copyDB("antivirus.db");
		
		if(update){
			checkUpdate();
		}else{
			handler.postDelayed(new Runnable() {
				
				@Override
				public void run() {
					//进入主页面
					enterHome();
					
				}
			}, 2000);		//延迟两秒进入主页面
		}
		
		//动画
		AlphaAnimation aa = new AlphaAnimation(0.2f, 1.0f);
		aa.setDuration(2000);
		findViewById(R.id.rl_root_splash).startAnimation(aa);
		
	}
	
	private void installShortCut() {
		//判断是否已经安装过快捷方式了
		boolean shortcut = sp.getBoolean("shortcut", false);
		if(shortcut){
			return ;
		}
		
		//发送广播的意图
		Intent intent = new Intent();
		intent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");//启动系统的某一个应用来创建快捷方式
		//快捷方式里面要包含三个重要的信息：1，名称    2，图标   3，快捷方式要启动的应用程序
		intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, "手机卫士");
		Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
		intent.putExtra(Intent.EXTRA_SHORTCUT_ICON, bitmap);
		
		//桌面点击图标对应的意图
		Intent shortcutIntent = new Intent();
		shortcutIntent.setAction("android.intent.action.MAIN");
		shortcutIntent.addCategory("android.intent.category.LAUNCHER");
		shortcutIntent.setClassName(getPackageName(), "com.ckw.mymobilesafe.SplashActivity");
		intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
		
		Editor editor = sp.edit();
		editor.putBoolean("shortcut", true);
		editor.commit();
		
		//发送广播
		sendBroadcast(intent);
	}

	//把address.db拷贝到data/data/<包名>/files/address.db
	//因为存放在assets下的文件只要图片文件才可以直接用
	private void copyDB(String filename) {
		
		try {
			File file = new File(getFilesDir(), filename);
			if(file.exists() && file.length()>0){//只要拷贝过就不要拷贝了
				Log.i(tag, "已经存在文件了");
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
				Log.i(tag, "显示升级的对话框");
				showUpdataDialog();
				break;
			case URL_ERROR:
				enterHome();
				Toast.makeText(getApplicationContext(), "URL错误", 0).show();
				break;
			case NETWORK_ERROR:
				enterHome();
				Toast.makeText(getApplicationContext(), "网络异常", 0).show();
				break;
			case JSON_ERROR:
				enterHome();
				Toast.makeText(SplashActivity.this, "JSON解析出错", 0).show();
				break;
			default:
				break;
			}
		}

	};
	
	protected void enterHome() {
		Intent intent = new Intent(this, HomeActivity.class);
		startActivity(intent);
		// 关闭启动页面
		finish();
	}
	
	//提示升级对话框
	protected void showUpdataDialog() {
		AlertDialog.Builder builder = new Builder(this);
		builder.setCancelable(false);//设置按返回键不能取消
		builder.setTitle("有新版本可以升级");
		builder.setMessage(description);
		builder.setPositiveButton("立刻升级", new OnClickListener(){

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// 下载APK，并且替换安装
				if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
					//sdcard存在
					//afinal
					FinalHttp finalHttp = new FinalHttp();
					finalHttp.download(apkurl, 
							Environment.getExternalStorageDirectory().getAbsolutePath()
							+"/mobilesafe.apk", new AjaxCallBack<File>() {

								@Override
								public void onFailure(Throwable t, int errorNo,
										String strMsg) {
									t.printStackTrace();
									Toast.makeText(getApplicationContext(), "下载失败", 1).show();
									super.onFailure(t, errorNo, strMsg);
								}

								@Override
								public void onLoading(long count, long current) {
									// TODO Auto-generated method stub
									super.onLoading(count, current);
									
									int progress = (int) (current*100/count);
									tv_update_info.setText("下载进度：" + progress +"%");
								}

								@Override
								public void onSuccess(File t) {
									// TODO Auto-generated method stub
									super.onSuccess(t);
									installAPK(t);
								}
								
								/**
								 * 安装apk文件
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
					Toast.makeText(getApplicationContext(), "没有sdcard，请检查设备后再试", 0).show();
					return;
				}
			}
		});
		builder.setNegativeButton("下次再说", new OnClickListener(){

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// 直接进入主页面
				dialog.dismiss();
				enterHome();
			}
		});
		builder.show();
	}

	/**
	 * 检查是否有新版本，如果有就升级
	 */
	private void checkUpdate() {

		new Thread(){
			public void run(){
				Message msg = Message.obtain();
				
				long startTime = System.currentTimeMillis();
				
				try {
					
					URL url = new URL(getString(R.string.serverurl));
					//联网
					conn = (HttpURLConnection)url.openConnection();
					conn.setRequestMethod("GET");
				//	conn.setReadTimeout(2000);
					conn.setConnectTimeout(5000);
					code = conn.getResponseCode();			//这是个阻塞式的方法，要注意
					
					if(code == 200){
						//联网成功
						InputStream is = conn.getInputStream();
						String result = StreamTools.readFromStream(is);
						Log.i(tag , "联网成功"+ result);
						
						//JSON解析
						JSONObject obj = new JSONObject(result);
						version = (String)obj.get("version");
						description = (String)obj.get("description");
						apkurl = (String)obj.get("apkurl");
						System.out.println(apkurl);
						if(getVersion().equals(version)){
							//版本一致，不用更新，直接进入主页面
							msg.what = ENTER_HOME;
						}else{
							//有新版本，弹出一个升级对话框
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
					//停留2秒钟
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
	 * 得到当前版本号
	 */
	private String getVersion(){
		
		PackageManager pm = getPackageManager();
		//得到指定APK的Manifest
		try {
			PackageInfo info = pm.getPackageInfo(getPackageName(), 0);
			return info.versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			return "包名写错了";
		}
	}
}
