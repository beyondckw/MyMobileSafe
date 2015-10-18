package com.ckw.mymobilesafe.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class ApplockDBOpenHelper extends SQLiteOpenHelper {

	/*
	 * ���ݿⴴ���Ĺ��췽�������ݿ�����Ϊapplock.db,�汾Ϊ1
	 */
	public ApplockDBOpenHelper(Context context) {
		super(context, "applock.db", null, 1);
		
	}

	//��ʼ�����ݿ�ı�ṹ,��ŵ�����������Ӧ�ó���İ���
	@Override
	public void onCreate(SQLiteDatabase db) {
		String sql = "create table applock " +
				"(_id integer primary key autoincrement, packname varchar(20))";
		db.execSQL(sql);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

		
	}

}
