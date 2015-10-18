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
 * 业务方法，提供手机里面安装的所有的应用程序信息
 * @author Administrator
 *
 */
public class AppInfoProvider {

	public static List<AppInfo> getAppInfos(Context context){
		
		List<AppInfo> appInfos = new ArrayList<AppInfo>();
		AppInfo appInfo;
		PackageManager pm = context.getPackageManager();
		//得到所有安装在系统上的应用程序的信息 
		List<PackageInfo> packInfos = pm.getInstalledPackages(0);
		for(PackageInfo info : packInfos){
			appInfo = new AppInfo();
			//每一个应用程序的信息都是存放在manifest中，所以一个info就相当于一个apk包的清单文件
			String packname = info.packageName;
			Drawable icon = info.applicationInfo.loadIcon(pm);
			String name = info.applicationInfo.loadLabel(pm).toString();
			int uid = info.applicationInfo.uid;   //操作系统分配给应用程序的唯一的固定的编号
			
			//获取应用程序的标记，这个标记代表着应用程序的各种状态
			int flags = info.applicationInfo.flags;
			if((flags&ApplicationInfo.FLAG_SYSTEM) == 1){
				//这是系统程序
				appInfo.setUserApp(false);
			}else{
				//这是用户程序
				appInfo.setUserApp(true);
			}
			if((flags&ApplicationInfo.FLAG_EXTERNAL_STORAGE) == 1){
				//此应用程序存放在SD卡中
				appInfo.setInRom(false);
			}else{
				//此应用程序存放在手机内存
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
