package com.ckw.mymobilesafe;

import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class Setup3Activity extends BaseSetupActivity {
	private EditText et;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setup3);
		
		et = (EditText) findViewById(R.id.et);
		et.setText(sp.getString("phone", ""));
		
	}
	
	@Override
	public void showNext() {
		
		String phone = et.getText().toString().trim();
		if(TextUtils.isEmpty(phone)){
			Toast.makeText(this, "安全号码还没设置", 0).show();
			return;
		}
		//保存安全号码
		Editor editor = sp.edit();
		editor.putString("phone", phone);
		editor.commit();
		
		Intent intent = new Intent(this,Setup4Activity.class);
		startActivity(intent);
		finish();
		overridePendingTransition(R.anim.tran_in, R.anim.tran_out);
	}

	@Override
	public void showPre() {
		Intent intent = new Intent(this,Setup2Activity.class);
		startActivity(intent);
		finish();
		overridePendingTransition(R.anim.tran_pre_in, R.anim.tran_pre_out);
		
	}
	
	/**
	 * 选择联系人的点击事件
	 */
	public void selectContact(View view){
		//Intent intent = new Intent(this, SelectContactActivity.class);
		//startActivityForResult(intent, 0);
		
		Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
		startActivityForResult(intent, 1);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		
		switch(requestCode){
		case 1:
			if(resultCode == RESULT_OK){
				Uri contactData = data.getData();
				@SuppressWarnings("deprecation")
				Cursor cursor = managedQuery(contactData, null, null, null, null);
				cursor.moveToFirst();
				String num = this.getContactPhone(cursor);
				et.setText(num);
				
			}
			break;
		default:
			break;
		}
		
	}

	private String getContactPhone(Cursor cursor) {
		// TODO Auto-generated method stub
		int phontCoulum = cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER);
		int phoneNum = cursor.getInt(phontCoulum);
		String result = "";
		if(phoneNum > 0){
			//获得联系人的ID
			int idColumn = cursor.getColumnIndex(ContactsContract.Contacts._ID);
			String contactId = cursor.getString(idColumn);
			//获得联系人电话的cursor
			Cursor phone = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
					null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID+"="+contactId, null, null);
			if(phone.moveToFirst()){
				int index;
				int typeindex;
				int phone_type;
				for(; !phone.isAfterLast(); phone.moveToNext()){
					index = phone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
					typeindex = phone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE);
					phone_type = phone.getInt(typeindex);
					String phoneNumber = phone.getString(index);
					result = phoneNumber;
					switch(phone_type){
					case 2:
						result = phoneNumber;
						break;
					default:
						break;
					}
				}
				if(!phone.isClosed()){
					phone.close();
				}
			}
		}
		cursor.close();
		result = result.replace("-", "");
		return result;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		//getMenuInflater().inflate(R.m, menu)
		return super.onCreateOptionsMenu(menu);
	}
}
