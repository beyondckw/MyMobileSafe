package com.ckw.mymobilesafe.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.util.Xml;


/**
 * 操作短信的工具类
 * @author Administrator
 *
 */
public class SmsUtils {

	/*
	 * 备份短信的回调接口
	 */
	public interface BackUpCallBack{
		
		/***
		 * 开始备份的时候，设置进度的最大值
		 * @param max 总进度
		 */
		public void beforeBackup(int max);
		/***
		 * 备份过程中，增加进度
		 * @param progress 当前进度
		 */
		public void onSmsBackup(int process);
	}
	
	/*
	 * 还原短信的回调接口
	 */
	public interface RestoreSmsCallBack{
		/***
		 * 开始还原的时候，设置进度的最大值
		 * @param max 总进度
		 */
		public void beforeRestore(int max);
		/***
		 * 还原过程中，增加进度
		 * @param progress 当前进度
		 */
		public void onSmsRestore(int process);
	}
	
	
	/*
	 * 备份用户的短信
	 * @param callBack 备份短信的回调接口
	 */
	public static void backupSms(Context context, BackUpCallBack callBack) throws Exception{
		ContentResolver resolver = context.getContentResolver();
	    
		File file = new File(Environment.getExternalStorageDirectory(), "backup.xml");
		//System.out.println(Environment.getExternalStorageDirectory()+"/");
		FileOutputStream fos = new FileOutputStream(file);
		//把用户的短信一条一条读出来，按照一定的格式写到xml文件里
		/*
		 * 自定义的Xml文件如下
		   <xml version .........>
	  		<smss max= ?>         //定义一个属性为结点总数
	  				<sms>
	  					<body>   </body>
	  					<date>   </date>
	  					<type>   </type>
	  					<address>   </address>
	  				</sms>
	  				<sms>
	  				
	  				</sms>
	  		</smss>
		 */
		XmlSerializer serializer = Xml.newSerializer();//获取Xml文件的生成器
		//初始化生成器
		serializer.setOutput(fos, "utf-8");
		serializer.startDocument("utf-8", true); //头
		
		serializer.startTag(null, "smss");	//根节点
		Uri uri = Uri.parse("content://sms/");//存放信息的数据库
		Cursor cursor = resolver.query(uri, new String[]{"body", "date", "type", "address"}, null, null, null);
		
		//开始备份的时候，设置进度条的最大值
		int max = cursor.getCount();
		//pd.setMax(max);
		callBack.beforeBackup(max);
		
		serializer.attribute(null, "max", max +"");  //附加属性
		
		int process = 0;
		while(cursor.moveToNext()){
			//Thread.sleep(1000);
			String body = cursor.getString(0);
			String date = cursor.getString(1);
			String type = cursor.getString(2);
			String address = cursor.getString(3);
			serializer.startTag(null, "sms");
			
			serializer.startTag(null, "body");
			serializer.text(body);     //文本
			serializer.endTag(null, "body");
			
			serializer.startTag(null, "date");
			serializer.text(date);
			serializer.endTag(null, "date");
			
			serializer.startTag(null, "type");
			serializer.text(type);
			serializer.endTag(null, "type");
			
			serializer.startTag(null, "address");
			serializer.text(address);
			serializer.endTag(null, "address");
			
			serializer.endTag(null, "sms");
			
			//增加进度
			process++;
			//pd.setProgress(process);
			callBack.onSmsBackup(process);
		}
		serializer.endTag(null, "smss");
		
		serializer.endDocument();
		fos.close();
	}
	
	/*
	 * 还原用户的短信
	 * @param callBack 还原短信的回调接口
	 */
	public static void smsRestore(Context context, RestoreSmsCallBack callBack) throws Exception{
		Uri uri = Uri.parse("content://sms/");
		
		//先帮用户悄悄备份，防止下一步删除的时候用户还没进行备份
		backupSms(context, new BackUpCallBack() {
			@Override
			public void onSmsBackup(int process) { //不用更新备份的进度条
				
			}
			@Override
			public void beforeBackup(int max) {
				
			}
		});
		
		//先把所有短信删除再还原，避免多次还原出现大量重复现象
		context.getContentResolver().delete(uri, null, null);
		
		String body = null, date = null, type = null, address = null;
		//读取SD卡上的Xml文件
		File file = new File(Environment.getExternalStorageDirectory(), "backup.xml");
		FileInputStream fis = new FileInputStream(file);
		XmlPullParser parser = Xml.newPullParser();
		parser.setInput(fis, "utf-8");
		
		int process = 0;
		int max = 0;
		//读取每一条信息
		ContentValues values = null;
		int eventType = parser.getEventType();    // 产生第一个事件
        while (eventType != XmlPullParser.END_DOCUMENT) {  
            switch (eventType) {  
            case XmlPullParser.START_DOCUMENT:  
            	 values = new ContentValues();
                 break;  
            case XmlPullParser.START_TAG:  
            	if(parser.getName().equals("smss")){
            		//读取max，设置进度条的最大值
            		max = Integer.parseInt(parser.getAttributeValue(null, "max"));
            	//	System.out.println(max);
            		callBack.beforeRestore(max);
            	}else if (parser.getName().equals("body")) {  
                	eventType = parser.next(); 
                    body = parser.getText(); 
                } else if (parser.getName().equals("date")) {  
                    eventType = parser.next();  
                    date = parser.getText();  
                } else if (parser.getName().equals("type")) {  
                    eventType = parser.next();  
                    type = parser.getText();   
                } else if (parser.getName().equals("address")) {  
                    eventType = parser.next();  
                    address = parser.getText(); 
                }  
                break;  
            case XmlPullParser.END_TAG:  
                if (parser.getName().equals("sms")) {  //这是一条完整的短信
                	Thread.sleep(500);   
                	//把短信插入到系统信息应用中
        			values.put("body", body);
        			values.put("date", date);
        			values.put("type", type);
        			values.put("address", address);
        			context.getContentResolver().insert(uri, values);
        			
        			process++;
        			callBack.onSmsRestore(process);
                }  
                break;  
            }  
            eventType = parser.next();  
        }  
       
	}
}
