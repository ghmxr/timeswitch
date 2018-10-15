package com.github.ghmxr.timeswitch.utils;

import java.io.File;

import android.os.Environment;

public class StorageUtil {
	
	public static String getSDPath(){ 
	       File sdDir = null; 
	       boolean sdCardExist = Environment.getExternalStorageState()   
	       .equals(android.os.Environment.MEDIA_MOUNTED);//�ж�sd���Ƿ����
	       if(sdCardExist)   
	       {                               
	         sdDir = Environment.getExternalStorageDirectory();//��ȡ��Ŀ¼
	      }   
	       return sdDir.toString(); 
	}

}
