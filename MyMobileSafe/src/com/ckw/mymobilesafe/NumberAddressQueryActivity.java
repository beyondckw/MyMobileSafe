package com.ckw.mymobilesafe;

import android.app.Activity;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ckw.mymobilesafe.db.dao.NumberAddressQueryUtils;

public class NumberAddressQueryActivity extends Activity {

	private EditText ed_phone;
	private TextView result;
	private String TAG = "NumberAddressQueryActivity";
	//系统的震动服务
	private Vibrator vibrator;
	private Toast toast;
	
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_number_address_query);
		ed_phone = (EditText) findViewById(R.id.ed_phone);
		result = (TextView) findViewById(R.id.result);
		vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
		
		//当搜索字符串达到一定长度的时候自动显示结果
		ed_phone.addTextChangedListener(new TextWatcher(){

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {

				if(s!=null && s.length() >= 3){
					String address = NumberAddressQueryUtils.queryNumber(s.toString());
					result.setText(address);
				}else{
					result.setText("");
				}
				
			}
			
		});
	}
	
	/*
	 * 查询号码归属地
	 */
	public void numberAddressQuery(View view){
		String phone = ed_phone.getText().toString().trim();
		if(TextUtils.isEmpty(phone)){
			
			if(toast == null){
				toast = Toast.makeText(this, "号码为空", 0);
			}else{
				toast.setText("号码为空");
				toast.setDuration(0);
			}
			toast.show();
		//	Toast.makeText(this, "号码为空", 0).show();
			//当输入框为空时，点击查询按钮就会启动一个使输入框摆动的动画
			Animation shake = AnimationUtils.loadAnimation(this, R.anim.shake);
			ed_phone.startAnimation(shake);
			
			//震动效果
			vibrator.vibrate(500);
			return ;
		}else{
			//去数据库查询号码归属地
			String address = NumberAddressQueryUtils.queryNumber(phone);
			result.setText(address);
			
			//Log.i(TAG, "你要查询的电话号码==" + phone);
		}
		
	}
}
