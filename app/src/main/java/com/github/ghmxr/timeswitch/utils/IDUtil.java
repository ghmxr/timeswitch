package com.github.ghmxr.timeswitch.utils;

import java.util.concurrent.atomic.AtomicInteger;
import android.annotation.SuppressLint;
import android.os.Build;
import android.view.View;

public class IDUtil {
	
	private static final AtomicInteger sNextGeneratedId = new AtomicInteger(1);
	
	/**
	* Generate a value suitable for use in {@link #setId(int)}.
	* This value will not collide with ID values generated at build time by aapt for R.id.
	*
	* @return a generated ID value
	*/
	@SuppressLint("NewApi")
	public static int generateViewId() {
		
		if(Build.VERSION.SDK_INT>=17){
			return View.generateViewId();
		}
		
		for (;;) {
	       final int result = sNextGeneratedId.get();
	       // aapt-generated IDs have the high byte nonzero; clamp to the range under that.
	       int newValue = result + 1;
	       if (newValue > 0x00FFFFFF) newValue = 1; // Roll over to 1, not 0.
	       if (sNextGeneratedId.compareAndSet(result, newValue)) {
	           return result;
	       }
	   }
	}

}
