package com.ckw.mymobilesafe.engine;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import android.widget.TextView;

import com.ckw.mymobilesafe.domain.AppInfo;

/**
 * ҵ�񷽷����ṩ�ֻ����氲װ�����е�Ӧ�ó�����Ϣ
 * @author Administrator
 *
 */
public class AppInfoProvider {

	public static List<AppInfo> getAppInfos(Context context){
		
		List<AppInfo> appInfos = new ArrayList<AppInfo>();
		AppInfo appInfo;
		PackageManager pm = context.getPackageManager();
		//�õ����а�װ��ϵͳ�ϵ�Ӧ�ó������Ϣ 
		List<PackageInfo> packInfos = pm.getInstalledPackages(0);
		for(PackageInfo info : packInfos){
			appInfo = new AppInfo();
			//ÿһ��Ӧ�ó������Ϣ���Ǵ����manifest�У�����һ��info���൱��һ��apk�����嵥�ļ�
			String packname = info.packageName;
			Drawable icon = info.applicationInfo.loadIcon(pm);
			String name = info.applicationInfo.loadLabel(pm).toString();
			int uid = info.applicationInfo.uid;   //����ϵͳ�����Ӧ�ó����Ψһ�Ĺ̶��ı��
			
			//��ȡӦ�ó���ı�ǣ������Ǵ�����Ӧ�ó���ĸ���״̬
			int flags = info.applicationInfo.flags;
			if((flags&ApplicationInfo.FLAG_SYSTEM) == 1){
				//����ϵͳ����
				appInfo.setUserApp(false);
			}else{
				//�����û�����
				appInfo.setUserApp(true);
			}
			if((flags&ApplicationInfo.FLAG_EXTERNAL_STORAGE) == 1){
				//��Ӧ�ó�������SD����
				appInfo.setInRom(false);
			}else{
				//��Ӧ�ó��������ֻ��ڴ�
				appInfo.setInRom(true);
			}
			
			appInfo.setIcon(icon);
			appInfo.setName(name);
			appInfo.setPackname(packname);
			appInfo.setUid(uid);
			
			appInfos.add(appInfo);
		}
		return appInfos;
	}
	
}
