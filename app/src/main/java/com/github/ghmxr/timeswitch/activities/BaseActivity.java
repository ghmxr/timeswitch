package com.github.ghmxr.timeswitch.activities;

import java.lang.reflect.Method;
import java.util.LinkedList;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;

/**
 * @author mxremail@qq.com  https://github.com/ghmxr/timeswitch
 */
public abstract class BaseActivity extends AppCompatActivity {

	public static LinkedList<BaseActivity> queue=new LinkedList<>();
	public static MyHandler myHandler;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(queue.size()<=0) myHandler=new MyHandler();
		if(!queue.contains(this)) queue.add(this);
	}

	public void setIconEnable(Menu menu, boolean enable) {
		try {
			Class<?> clazz =Class.forName("android.support.v7.view.menu.MenuBuilder"); //Class.forName("com.android.internal.view.menu.MenuBuilder");
			Method m = clazz.getDeclaredMethod("setOptionalIconsVisible", boolean.class);
			m.setAccessible(true);
			m.invoke(menu, enable);
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

	public abstract void processMessage(Message msg);

	public static void sendMessage(Message msg){
		if(myHandler!=null) myHandler.sendMessage(msg);
	}

	public static void sendEmptyMessage(int what){
		if(myHandler!=null) myHandler.sendEmptyMessage(what);
	}

	@Override
	public void finish(){
		super.finish();
		if(queue.contains(this)) queue.remove(this);
		if(queue.size()<=0) myHandler=null;
	}

	public static class MyHandler extends Handler{
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			try{
				if(queue.size()>0) queue.getLast().processMessage(msg);
			}catch (Exception e){
				e.printStackTrace();
			}
		}
	}

}