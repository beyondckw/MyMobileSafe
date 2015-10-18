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
 * ���ݿ����ɾ�Ĳ�ҵ����
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
	 * ��ѯ���������������ģʽ
	 * @param number
	 * @return ���غ��������ģʽ�����Ǻ��������뷵��null
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
	
	//mode=1�绰���� mode=2��������mode=3ȫ������
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
	
	//��ȡ���ݿ������еļ�¼��
	public int totalNumber(){
		String sql = "select count(*) from blacknumber";
		SQLiteDatabase db = helper.getReadableDatabase();
		Cursor cursor = db.rawQuery(sql, null);
		cursor.moveToFirst();
		return cursor.getInt(0);
	}
	
	/**
	 * ��ѯ���ֵĺ���������
	 * @param offset ���ĸ�λ�ÿ�ʼ��ȡ����
	 * @param maxnumber һ������ȡ����������
	 * @return
	 * sql���Ϊ select * from blacknumber limit 10 offset 1;
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
