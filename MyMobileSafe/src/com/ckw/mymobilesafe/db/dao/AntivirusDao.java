package com.ckw.mymobilesafe.db.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class AntivirusDao {

	public static final String path = "/data/data/com.ckw.mymobilesafe/files/antivirus.db";

	/**
	 * ��ѯ�Ƿ��ǲ���
	 * 
	 * @param md5
	 * @return true�����ǲ�����false�����ǲ���
	 */
	public static boolean isVirus(String md5) {
		boolean result = false;
		SQLiteDatabase db = SQLiteDatabase.openDatabase(path, null,
				SQLiteDatabase.OPEN_READONLY);
		
		Cursor cursor = db.rawQuery("select * from datable where md5=?", new String[]{md5});
		if(cursor.moveToNext()){
			result = true;
		}
		
		cursor.close();
		db.close();
		return result;
	}
}
