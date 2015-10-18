package com.ckw.mymobilesafe.test;

import java.io.File;

import android.annotation.SuppressLint;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import com.ckw.mymobilesafe.db.BlackNumberDBOpenHelper;

public class TestBlackNumberDB extends AndroidTestCase {
	
	public void testCreateDB() throws Exception{
		BlackNumberDBOpenHelper helper = new BlackNumberDBOpenHelper(getContext());
		SQLiteDatabase db = helper.getWritableDatabase();
		
	}
}
