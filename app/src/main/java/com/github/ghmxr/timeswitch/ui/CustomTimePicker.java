package com.github.ghmxr.timeswitch.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewParent;

/**
 * @author mxremail@qq.com  https://github.com/ghmxr/timeswitch
 */
public class CustomTimePicker extends android.widget.TimePicker{
	
	public CustomTimePicker(Context context){
		super(context);		
	}
	
	public CustomTimePicker(Context context,AttributeSet paramAttributeSet){
		super(context,paramAttributeSet);
	}
	
	public CustomTimePicker(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

	@Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        if (ev.getActionMasked() == MotionEvent.ACTION_DOWN) {
            ViewParent p = getParent();
            if (p != null)
                p.requestDisallowInterceptTouchEvent(true);
        }

        return false;
    }
				
}
