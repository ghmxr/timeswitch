package com.github.ghmxr.timeswitch.ui.bottomdialogs;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager.LayoutParams;

import com.github.ghmxr.timeswitch.R;
import com.github.ghmxr.timeswitch.ui.DialogConfirmedCallBack;

/**
 * @author mxremail@qq.com  https://github.com/ghmxr/timeswitch
 */
public class BottomDialog extends Dialog{

	DialogConfirmedCallBack callBack;

	public BottomDialog(Context context) {
		super(context);
		// TODO Auto-generated constructor stub		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        Window window = getWindow();       
        window.setGravity(Gravity.BOTTOM);
        window.setWindowAnimations(R.style.style_bottomdialog);         		
	}
	
	@Override
	public void show(){
		super.show();
		Window window =this.getWindow();
		LayoutParams params=window.getAttributes();
        params.width=LayoutParams.MATCH_PARENT;
        params.height=LayoutParams.WRAP_CONTENT;
        window.setAttributes(params);
        window.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#ffffff")));
	}

	/*public void setBackgroundColor(String color){
		this.getWindow().setBackgroundDrawable(new ColorDrawable(Color.parseColor(color)));
	}  */
	public void setOnDialogConfirmedListener(DialogConfirmedCallBack callBack){
		this.callBack=callBack;
	}
				
}
