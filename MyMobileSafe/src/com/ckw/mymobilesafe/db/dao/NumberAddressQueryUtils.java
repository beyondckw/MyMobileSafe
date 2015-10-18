package com.ckw.mymobilesafe.db.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class NumberAddressQueryUtils {

	private static String path = "data/data/com.ckw.mymobilesafe/files/address.db";
	
	/*
	 * 传一个号码进来，返回号码的归属地
	 */
	public static String queryNumber(String number){
		String address = number;
		
		//path--把address.db拷贝到data/data/<包名>/files/address.db
		SQLiteDatabase database = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READONLY);
		
		//先判断是不是11位的手机号码135  147  159  ........
		if(number.matches("^1[34568]\\d{9}$")){
			
			String sql = "select location from data2 where id = (select outkey from data1 where id = ?)";
			Cursor cursor = database.rawQuery(sql, new String[]{number.substring(0, 7)});
			
			while(cursor.moveToNext()){
				address = cursor.getString(0);
			}
			cursor.close();
		}else{
			//其他号码
			switch(number.length()){
			case 3:
				//比如110
				address = "公安局";
				break;
			case 4:
				//比如 5556
				address = "模拟器";
				break;
			case 5:
				//比如 10086
				address = "客服电话";
				break;
			case 7:
			case 8:
				address = "本地号码";
				break;
			default:
				//处理长途电话 010  0759
				if(number.length()>10 && number.startsWith("0")){
					String sql = "select location from data2 where area = ?";
					Cursor cursor = database.rawQuery(sql, new String[]{number.substring(1, 3)});
					
					while(cursor.moveToNext()){
						address = cursor.getString(0);
					}
					//先查三位的，再查四位的 010  0778
					cursor = database.rawQuery(sql, new String[]{number.substring(1, 4)});
					while(cursor.moveToNext()){
						address = cursor.getString(0);
					}
					cursor.close();
				}
				break;
			}
		}
		return address;
	}
}
