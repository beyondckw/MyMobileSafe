package com.ckw.mymobilesafe.db.dao;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.ckw.mymobilesafe.db.BlackNumberDBOpenHelper;
import com.ckw.mymobilesafe.domain.BlackNumberInfo;

/*
 * 数据库的增删改查业务类
 */
public class BlackNumberDao {

	private BlackNumberDBOpenHelper helper;
	private String sql;
	
	public BlackNumberDao(Context context){
		helper = new BlackNumberDBOpenHelper(context);
	}
	
	public boolean find(String number){
		boolean result = false;
		SQLiteDatabase db = helper.getReadableDatabase();
		sql = "select * from blacknumber where number = ?";
		Cursor cursor = db.rawQuery(sql, new String []{number});
		if(cursor.moveToNext()){
			result = true;
		}
		cursor.close();
		db.close();
		return result;
	}
	
	/**
	 * 查询黑名单号码的拦截模式
	 * @param number
	 * @return 返回号码的拦截模式，不是黑名单号码返回null
	 */
	public String findMode(String number){
		String result = null;
		SQLiteDatabase db = helper.getReadableDatabase();
		Cursor cursor = db.rawQuery("select mode from blacknumber where number=?", new String[]{number});
		if(cursor.moveToNext()){
			result = cursor.getString(0);
		}
		cursor.close();
		db.close();
		return result;
	}
	
	//mode=1电话拦截 mode=2短信拦截mode=3全部拦截
	public void add(String number, String mode){
		SQLiteDatabase db = helper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("number", number);
		values.put("mode", mode);
		db.insert("blacknumber", null, values);
		db.close();
	}
	
	public void update(String number, String newMode){
		SQLiteDatabase db = helper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("mode", newMode);
		db.update("blacknumber", values, "number=?", new String[]{number});
		db.close();
	}
	
	public void delete(String number){
		SQLiteDatabase db = helper.getWritableDatabase();
		db.delete("blacknumber", "number=?", new String[]{number});
		db.close();
	}
	
	//获取数据库中所有的记录数
	public int totalNumber(){
		String sql = "select count(*) from blacknumber";
		SQLiteDatabase db = helper.getReadableDatabase();
		Cursor cursor = db.rawQuery(sql, null);
		cursor.moveToFirst();
		return cursor.getInt(0);
	}
	
	/**
	 * 查询部分的黑名单号码
	 * @param offset 从哪个位置开始读取数据
	 * @param maxnumber 一次最多获取多少条数据
	 * @return
	 * sql语句为 select * from blacknumber limit 10 offset 1;
	 */
	public List<BlackNumberInfo> findPart(int offset, int maxnumber){
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		List<BlackNumberInfo> result = new ArrayList<BlackNumberInfo>();
		SQLiteDatabase db = helper.getReadableDatabase();
		sql = "select number,mode from blacknumber order by _id desc limit ? offset ?";
		Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(maxnumber),String.valueOf(offset)});
		while(cursor.moveToNext()){
			BlackNumberInfo info = new BlackNumberInfo();
			String number = cursor.getString(0);
			String mode = cursor.getString(1);
			info.setNumber(number);
			info.setMode(mode);
			result.add(info);
		}
		cursor.close();
		db.close();
		return result;
	}
}
