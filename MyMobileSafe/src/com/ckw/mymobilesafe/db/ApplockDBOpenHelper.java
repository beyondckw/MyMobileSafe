package com.ckw.mymobilesafe.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class ApplockDBOpenHelper extends SQLiteOpenHelper {

	/*
	 * 数据库创建的构造方法，数据库名称为applock.db,版本为1
	 */
	public ApplockDBOpenHelper(Context context) {
		super(context, "applock.db", null, 1);
		
	}

	//初始化数据库的表结构,存放的是上了锁的应用程序的包名
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
