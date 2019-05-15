package com.github.ghmxr.timeswitch.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.os.Environment;

public class StorageUtil {

	public static List<String> getAvailableStoragePaths(){
		try{
			List<String> paths=new ArrayList<>();
			String mainStorage=null;
			try{
				mainStorage=getExternalStorageDirectory().toLowerCase(Locale.getDefault());
				if(!mainStorage.equals("")) paths.add(mainStorage);
			}catch(Exception e){e.printStackTrace();}

			Runtime runtime=Runtime.getRuntime();
			Process process=runtime.exec("mount");
			BufferedReader reader=new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line;
			while((line=reader.readLine())!=null){
				//Log.d("", line);
				if (line.contains("proc") || line.contains("tmpfs") || line.contains("media") || line.contains("asec") || line.contains("secure") || line.contains("system") || line.contains("cache")
						|| line.contains("sys") || line.contains("data") || line.contains("shell") || line.contains("root") || line.contains("acct") || line.contains("misc") || line.contains("obb")){
					continue;
				}
				if (line.contains("fat") || line.contains("fuse") || (line.contains("ntfs"))){

					String items[] = line.split(" ");
					if (items != null && items.length > 1){
						String path = items[1].toLowerCase(Locale.getDefault());
						if(!path.equals(mainStorage)) paths.add(path);
					}
				}


			}
			//Log.d("StoragePaths", Arrays.toString(paths.toArray()));
			return paths;
		}catch(Exception e){e.printStackTrace();}
		return new ArrayList<>();
	}

	public static String getExternalStorageDirectory(){
		try{
			return Environment.getExternalStorageDirectory().getAbsolutePath();
		}catch (Exception e){e.printStackTrace();}
		return "";
	}

}
