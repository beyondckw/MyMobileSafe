package com.ckw.mymobilesafe.domain;

import android.graphics.drawable.Drawable;

/**
 * 应用程序信息的业务Bean
 * @author Administrator
 *
 */
public class AppInfo {

	private Drawable icon;
	private String name;
	private String packname;   //唯一识别一个应用程序
	private boolean inRom;  //是存放在内存还是SD卡
	private boolean userApp;  //是用户应用程序还是系统应用程序
	private int uid;
	
	
	public int getUid() {
		return uid;
	}
	public void setUid(int uid) {
		this.uid = uid;
	}
	public Drawable getIcon() {
		return icon;
	}
	public void setIcon(Drawable icon) {
		this.icon = icon;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPackname() {
		return packname;
	}
	public void setPackname(String packname) {
		this.packname = packname;
	}
	public boolean isInRom() {
		return inRom;
	}
	public void setInRom(boolean inRom) {
		this.inRom = inRom;
	}
	public boolean isUserApp() {
		return userApp;
	}
	public void setUserApp(boolean userApp) {
		this.userApp = userApp;
	}
	@Override
	public String toString() {
		return "AppInfo [name=" + name + ", packname=" + packname + ", inRom="
				+ inRom + ", userApp=" + userApp + ", uid=" + uid + "]";
	}
	
}
