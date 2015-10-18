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
 * �������ŵĹ�����
 * @author Administrator
 *
 */
public class SmsUtils {

	/*
	 * ���ݶ��ŵĻص��ӿ�
	 */
	public interface BackUpCallBack{
		
		/***
		 * ��ʼ���ݵ�ʱ�����ý��ȵ����ֵ
		 * @param max �ܽ���
		 */
		public void beforeBackup(int max);
		/***
		 * ���ݹ����У����ӽ���
		 * @param progress ��ǰ����
		 */
		public void onSmsBackup(int process);
	}
	
	/*
	 * ��ԭ���ŵĻص��ӿ�
	 */
	public interface RestoreSmsCallBack{
		/***
		 * ��ʼ��ԭ��ʱ�����ý��ȵ����ֵ
		 * @param max �ܽ���
		 */
		public void beforeRestore(int max);
		/***
		 * ��ԭ�����У����ӽ���
		 * @param progress ��ǰ����
		 */
		public void onSmsRestore(int process);
	}
	
	
	/*
	 * �����û��Ķ���
	 * @param callBack ���ݶ��ŵĻص��ӿ�
	 */
	public static void backupSms(Context context, BackUpCallBack callBack) throws Exception{
		ContentResolver resolver = context.getContentResolver();
	    
		File file = new File(Environment.getExternalStorageDirectory(), "backup.xml");
		//System.out.println(Environment.getExternalStorageDirectory()+"/");
		FileOutputStream fos = new FileOutputStream(file);
		//���û��Ķ���һ��һ��������������һ���ĸ�ʽд��xml�ļ���
		/*
		 * �Զ����Xml�ļ�����
		   <xml version .........>
	  		<smss max= ?>         //����һ������Ϊ�������
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
		XmlSerializer serializer = Xml.newSerializer();//��ȡXml�ļ���������
		//��ʼ��������
		serializer.setOutput(fos, "utf-8");
		serializer.startDocument("utf-8", true); //ͷ
		
		serializer.startTag(null, "smss");	//���ڵ�
		Uri uri = Uri.parse("content://sms/");//�����Ϣ�����ݿ�
		Cursor cursor = resolver.query(uri, new String[]{"body", "date", "type", "address"}, null, null, null);
		
		//��ʼ���ݵ�ʱ�����ý����������ֵ
		int max = cursor.getCount();
		//pd.setMax(max);
		callBack.beforeBackup(max);
		
		serializer.attribute(null, "max", max +"");  //��������
		
		int process = 0;
		while(cursor.moveToNext()){
			//Thread.sleep(1000);
			String body = cursor.getString(0);
			String date = cursor.getString(1);
			String type = cursor.getString(2);
			String address = cursor.getString(3);
			serializer.startTag(null, "sms");
			
			serializer.startTag(null, "body");
			serializer.text(body);     //�ı�
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
			
			//���ӽ���
			process++;
			//pd.setProgress(process);
			callBack.onSmsBackup(process);
		}
		serializer.endTag(null, "smss");
		
		serializer.endDocument();
		fos.close();
	}
	
	/*
	 * ��ԭ�û��Ķ���
	 * @param callBack ��ԭ���ŵĻص��ӿ�
	 */
	public static void smsRestore(Context context, RestoreSmsCallBack callBack) throws Exception{
		Uri uri = Uri.parse("content://sms/");
		
		//�Ȱ��û����ı��ݣ���ֹ��һ��ɾ����ʱ���û���û���б���
		backupSms(context, new BackUpCallBack() {
			@Override
			public void onSmsBackup(int process) { //���ø��±��ݵĽ�����
				
			}
			@Override
			public void beforeBackup(int max) {
				
			}
		});
		
		//�Ȱ����ж���ɾ���ٻ�ԭ�������λ�ԭ���ִ����ظ�����
		context.getContentResolver().delete(uri, null, null);
		
		String body = null, date = null, type = null, address = null;
		//��ȡSD���ϵ�Xml�ļ�
		File file = new File(Environment.getExternalStorageDirectory(), "backup.xml");
		FileInputStream fis = new FileInputStream(file);
		XmlPullParser parser = Xml.newPullParser();
		parser.setInput(fis, "utf-8");
		
		int process = 0;
		int max = 0;
		//��ȡÿһ����Ϣ
		ContentValues values = null;
		int eventType = parser.getEventType();    // ������һ���¼�
        while (eventType != XmlPullParser.END_DOCUMENT) {  
            switch (eventType) {  
            case XmlPullParser.START_DOCUMENT:  
            	 values = new ContentValues();
                 break;  
            case XmlPullParser.START_TAG:  
            	if(parser.getName().equals("smss")){
            		//��ȡmax�����ý����������ֵ
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
                if (parser.getName().equals("sms")) {  //����һ�������Ķ���
                	Thread.sleep(500);   
                	//�Ѷ��Ų��뵽ϵͳ��ϢӦ����
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
