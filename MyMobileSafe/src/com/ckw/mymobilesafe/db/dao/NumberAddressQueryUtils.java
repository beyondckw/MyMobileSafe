package com.ckw.mymobilesafe.db.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class NumberAddressQueryUtils {

	private static String path = "data/data/com.ckw.mymobilesafe/files/address.db";
	
	/*
	 * ��һ��������������غ���Ĺ�����
	 */
	public static String queryNumber(String number){
		String address = number;
		
		//path--��address.db������data/data/<����>/files/address.db
		SQLiteDatabase database = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READONLY);
		
		//���ж��ǲ���11λ���ֻ�����135  147  159  ........
		if(number.matches("^1[34568]\\d{9}$")){
			
			String sql = "select location from data2 where id = (select outkey from data1 where id = ?)";
			Cursor cursor = database.rawQuery(sql, new String[]{number.substring(0, 7)});
			
			while(cursor.moveToNext()){
				address = cursor.getString(0);
			}
			cursor.close();
		}else{
			//��������
			switch(number.length()){
			case 3:
				//����110
				address = "������";
				break;
			case 4:
				//���� 5556
				address = "ģ����";
				break;
			case 5:
				//���� 10086
				address = "�ͷ��绰";
				break;
			case 7:
			case 8:
				address = "���غ���";
				break;
			default:
				//����;�绰 010  0759
				if(number.length()>10 && number.startsWith("0")){
					String sql = "select location from data2 where area = ?";
					Cursor cursor = database.rawQuery(sql, new String[]{number.substring(1, 3)});
					
					while(cursor.moveToNext()){
						address = cursor.getString(0);
					}
					//�Ȳ���λ�ģ��ٲ���λ�� 010  0778
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
