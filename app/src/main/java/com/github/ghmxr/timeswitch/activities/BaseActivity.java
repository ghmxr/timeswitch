package com.github.ghmxr.timeswitch.activities;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedList;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.transition.TransitionManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.github.ghmxr.timeswitch.R;
import com.github.ghmxr.timeswitch.data.v2.PublicConsts;
import com.github.ghmxr.timeswitch.utils.ValueUtils;

/**
 * @author mxremail@qq.com  https://github.com/ghmxr/timeswitch
 */
public abstract class BaseActivity extends AppCompatActivity {

	public static LinkedList<BaseActivity> queue;
	public static MyHandler myHandler;
	public static final String EXTRA_TITLE_COLOR="color_title";
	//public String color_title="#3F51B5";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		if(queue==null) queue=new LinkedList<>();
		super.onCreate(savedInstanceState);
		if(queue.size()==0) myHandler=new MyHandler();
		if(!queue.contains(this)) queue.add(this);
		Log.d("BaseActivity","onCreate Method called and queue size is "+queue.size());
	}

	@Override
	public void onResume(){
		super.onResume();
		try{
			if(!queue.getLast().equals(this)){
				if(queue.contains(this)){
					queue.remove(this);
					queue.addLast(this);
					Log.d("BaseActivity","Remove from the queue and re-add this activity");
				}else{
					queue.addLast(this);
					Log.d("BaseActivity","re-add this activity");
				}
			}
		}catch (Exception e){e.printStackTrace();}
	}

	public void setToolBarAndStatusBarColor(View toolbar,String color){
		try{
			if(color==null) return;
			int color_value=Color.parseColor(color);
			toolbar.setBackgroundColor(color_value);
			if(Build.VERSION.SDK_INT>=21){
				Window window=getWindow();
				window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
				window.setStatusBarColor(color_value);
			}
			if(ValueUtils.isHighLightRGB(color_value)){
				setSupportActionbarContentsColor(toolbar,getResources().getColor(R.color.color_black));
			}else{
				setSupportActionbarContentsColor(toolbar,getResources().getColor(R.color.color_white));
			}
		}catch (Exception e){
			e.printStackTrace();
		}
	}

	private void setSupportActionbarContentsColor(View toolbar,int color){
		try{
			Field titleTextView=toolbar.getClass().getDeclaredField("mTitleTextView");
			//Field titleArrow=toolbar.getClass().getDeclaredField("");
			titleTextView.setAccessible(true);
			((TextView)titleTextView.get(toolbar)).setTextColor(color);
			//titleTextView.set(toolbar,color);
		}catch (Exception e){
			e.printStackTrace();
		}
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

	public void showTransitionAnimation(){
		TransitionManager.beginDelayedTransition((ViewGroup)findViewById(android.R.id.content));
	}

	public void checkAndPlayTransitionAnimation(){
		if(!getSharedPreferences(PublicConsts.PREFERENCES_NAME, Context.MODE_PRIVATE).getBoolean(PublicConsts.PREFERENCE_DISABLE_ANIMATION_EFFECTS,PublicConsts.PREFERENCE_DISABLE_ANIMATION_EFFECTS_DEFAULT)){
			showTransitionAnimation();
		}
	}

	public void stopTransitionAnimation(){
		TransitionManager.endTransitions((ViewGroup)findViewById(android.R.id.content));
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
		if(queue.size()==0) {
			myHandler=null;
			queue=null;
		}

	}

	@Override
	public void onDestroy(){
		super.onDestroy();
		if(queue==null||queue.size()==0) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					System.gc();
				}
			}).start();
		}
	}

	public static class MyHandler extends Handler{
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			try{
				if(queue!=null&&queue.size()>0) queue.getLast().processMessage(msg);
			}catch (Exception e){
				e.printStackTrace();
			}
		}
	}

}
