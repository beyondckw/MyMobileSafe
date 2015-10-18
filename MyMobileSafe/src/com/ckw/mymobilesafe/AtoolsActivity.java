package com.ckw.mymobilesafe;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.ckw.mymobilesafe.utils.SmsUtils;
import com.ckw.mymobilesafe.utils.SmsUtils.BackUpCallBack;
import com.ckw.mymobilesafe.utils.SmsUtils.RestoreSmsCallBack;

public class AtoolsActivity extends Activity {
	
	//private ProgressDialog pd;
	private ProgressBar pd;
	private ProgressBar pb;
	
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_atools);
		pd = (ProgressBar) findViewById(R.id.progressBar1);
		pb = (ProgressBar) findViewById(R.id.progressBar2);
	}
	
	/*
	 * 点击事件，进入号码归属地查询的页面
	 */
	public void numberQuery(View view){
		Intent intent = new Intent(this, NumberAddressQueryActivity.class);
		startActivity(intent);
	}
	
	public void smsBackUp(View view){
		/*pd = new ProgressDialog(this);
		pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		pd.setMessage("正在备份短信");
		pd.show();*/
		pd.setVisibility(ProgressBar.VISIBLE);
		pd.setProgress(0);
		new Thread(){
			public void run(){
				try {
					SmsUtils.backupSms(getApplicationContext(), new BackUpCallBack() {
						
						public void onSmsBackup(int process) {
							pd.setProgress(process);
						}
						
						public void beforeBackup(int max) {
							pd.setMax(max);
						}
					});
					runOnUiThread(new Runnable() {
						public void run() {
							Toast.makeText(AtoolsActivity.this, "备份成功", 0).show();
						}
					});
					
				} catch (Exception e) {
					e.printStackTrace();
					runOnUiThread(new Runnable() {
						public void run() {
							
							Toast.makeText(AtoolsActivity.this, "备份失败", 0).show();
						}
					});
				}finally{
					//pd.dismiss();
					runOnUiThread(new Runnable() {
						public void run() {
							pd.setVisibility(ProgressBar.GONE);
						}
					});
					
				}
			}
		}.start();
	}
	
	public void smsRestore(View view){
		pb.setVisibility(ProgressBar.VISIBLE);
		pb.setProgress(0);
		new Thread(){
			public void run(){
				try {
					SmsUtils.smsRestore(getApplicationContext(), new RestoreSmsCallBack() {
						
						public void onSmsRestore(int process) {
							pb.setProgress(process);
						}
						
						public void beforeRestore(int max) {
							pb.setMax(max);
						}
					});
					runOnUiThread(new Runnable() {
						public void run() {
							Toast.makeText(AtoolsActivity.this, "还原成功", 0).show();
						}
					});
					
				} catch (Exception e) {
					e.printStackTrace();
					runOnUiThread(new Runnable() {
						public void run() {
							
							Toast.makeText(AtoolsActivity.this, "还原失败", 0).show();
						}
					});
				}finally{
					//pd.dismiss();
					runOnUiThread(new Runnable() {
						public void run() {
							pb.setVisibility(ProgressBar.GONE);
						}
					});
					
				}
			}
		}.start();
	}
}
