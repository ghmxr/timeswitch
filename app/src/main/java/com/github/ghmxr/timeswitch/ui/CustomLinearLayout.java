package com.github.ghmxr.timeswitch.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewParent;

/**
 * @author mxremail@qq.com  https://github.com/ghmxr/timeswitch
 */
public class CustomLinearLayout extends android.widget.LinearLayout{

	public CustomLinearLayout(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	public CustomLinearLayout(Context context,AttributeSet paramAttributeSet){
		super(context,paramAttributeSet);
	}
	
	@Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        if (ev.getActionMasked() == MotionEvent.ACTION_DOWN) {
            ViewParent p = getParent();
            if (p != null)
                p.requestDisallowInterceptTouchEvent(false);
        }

        return false;
    }
		
}
