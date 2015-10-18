package com.ckw.mymobilesafe.db.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class AntivirusDao {

	public static final String path = "/data/data/com.ckw.mymobilesafe/files/antivirus.db";

	/**
	 * 查询是否是病毒
	 * 
	 * @param md5
	 * @return true代表是病毒，false代表不是病毒
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
