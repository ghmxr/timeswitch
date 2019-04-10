package com.github.ghmxr.timeswitch;

import android.content.Context;
import android.content.res.Resources;

public class Resource {
    private static Resources resources;
    private Resource(){}
    public static Resources getResources(Context context){
        if(resources==null){
            synchronized (Resource.class){
                if(resources==null){
                    resources=context.getApplicationContext().getResources();
                }
            }
        }
        return resources;
    }
}
