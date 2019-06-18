package com.github.ghmxr.timeswitch.utils;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.View;

import com.github.ghmxr.timeswitch.R;

import java.util.Calendar;

/**
 * @author mxremail@qq.com  https://github.com/ghmxr/timeswitch
 */
public class ValueUtils {

    /**
     * 将int数组转换为string类型，并由sep。
     * 例如 {0,1,2} 则转换为 "0,1,2"
     * @param array int[]
     * @return string
     */

    public static String intArray2String(String sep,int[] array){
        StringBuilder valuesBuilder=new StringBuilder("");
        if(array==null||array.length==0){
            return "";
        }
        else{
            for(int i=0;i<array.length;i++){
                valuesBuilder.append(array[i]);
                if(i<(array.length-1)&&array.length>1){
                    valuesBuilder.append(sep);
                }
            }
        }
        return valuesBuilder.toString();
    }

    /**
     * 将由split隔开的数字字符串转换为int数组
     * 例如  "0,1,2" 转换为  {0,1,2}
     * @param text
     * @return int[]
     */
    public static int[] string2intArray(String split,String text){
        try{
            String[] svalues=text.split(split);
            int[] values=new int[svalues.length];
            for(int i=0;i<svalues.length;i++){
                values[i]=Integer.parseInt(svalues[i]);
            }
            return values;
        }catch (Exception e){e.printStackTrace();}
        return new int[0];
    }

    /**
     * 将由英文逗号隔开的数字字符串转换为long数组
     * 例如  "0,1,2" 转换为  {0,1,2}
     * @param text
     * @return long[]
     */
    public static long[] string2longArray(String split,String text){
        try {
            String[] svalues=text.split(split);
            long[] values=new long[svalues.length];
            for(int i=0;i<svalues.length;i++){
                values[i]=Long.parseLong(svalues[i]);
            }
            return values;
        }catch (Exception e){e.printStackTrace();}
        return new long[0];
    }

    /**
     * 将long数组转换为string类型，并由英文逗号隔开。
     * 例如 {0,1,2} 则转换为 "0,1,2"
     * @param array long[]
     * @return string
     */
    public static String longArray2String(String seperator,long[] array){
        StringBuilder valuesBuilder=new StringBuilder("");
        if(array==null||array.length==0){
            return "";
        }
        else{
            for(int i=0;i<array.length;i++){
                valuesBuilder.append(array[i]);
                if(i<(array.length-1)&&array.length>1){
                    valuesBuilder.append(seperator);
                }
            }
        }
        return valuesBuilder.toString();
    }

    /**
     * 将String数组转换为指定符号隔开的字符串。
     * @param array 字符串数组
     * @param seperator 指定符号
     * @return 指定符号隔开的字符串
     */
    public static String stringArray2String(String seperator,String[] array){
        StringBuilder builder=new StringBuilder("");
        if(array==null||array.length==0) return "";
        for(int i=0;i<array.length;i++){
            builder.append(array[i]);
            if(i<(array.length-1)) builder.append(seperator);
        }
        return builder.toString();
    }

    /**
     * 将由指定符号隔开的字符串转换为字符串数组
     * @param value 由指定符号隔开的字符串
     * @return 字符串数组
     */
    public static String[] string2StringArray(String split,String value){
        if(value==null||value.equals("")) return new String[0];
        String [] values;
        try{
            values=value.split(split);
        }catch (Exception e){
            e.printStackTrace();
            return new String[0];
        }
        return values;
    }

    public static String format(int num){
        if(num<10&&num>=0){
            return ""+0+num;
        }
        return ""+num;
    }

    /**
     * 将毫秒时间转换为天，小时，分钟的字符串
     * @param context context
     * @param milliseconds 毫秒时间
     * @return 天，小时，分的字符串
     */
    public static String getFormatTime(Context context,long milliseconds){
        int day=(int)(milliseconds/(1000*60*60*24));
        int hour=(int)((milliseconds%(1000*60*60*24))/(1000*60*60));
        int minute=(int)((milliseconds%(1000*60*60))/(1000*60));
        StringBuilder value=new StringBuilder("");
        if(day!=0) {
            value.append(day+context.getResources().getString(R.string.day)
                    +hour+context.getResources().getString(R.string.hour)
                    +minute+context.getResources().getString(R.string.minute));
        }
        else if(hour!=0){
            value.append(hour+context.getResources().getString(R.string.hour)
                    +minute+context.getResources().getString(R.string.minute));
        }else{
            value.append(minute+context.getResources().getString(R.string.minute));
        }
        return value.toString();
    }

    /**
     * 获取周几的英文缩写
     * @param timeofmillis 毫秒时间绝对值
     * @return 周几的英文缩写字符串，例如周一 "MON"
     */
    public static String getDayOfWeek(long timeofmillis){
        Calendar calendar=Calendar.getInstance();
        calendar.setTimeInMillis(timeofmillis);
        switch (calendar.get(Calendar.DAY_OF_WEEK)){
            case Calendar.SUNDAY:return "SUN";
            case Calendar.MONDAY:return "MON";
            case Calendar.TUESDAY:return "TUE";
            case Calendar.WEDNESDAY:return "WED";
            case Calendar.THURSDAY:return "THU";
            case Calendar.FRIDAY:return "FRI";
            case Calendar.SATURDAY:return "SAT";
            default:break;
        }
        return "";
    }

    /**
     * 获取具体格式的日期时间值字符串"yyyy/mm/dd/(WFD)HH:MM:SS"
     * @param millisecond 时间值
     * @return 日期字符串
     */
    public static String getFormatDateTime(long millisecond){
        Calendar calendar=Calendar.getInstance();
        calendar.setTimeInMillis(millisecond);
        int month=calendar.get(Calendar.MONTH)+1;
        return calendar.get(Calendar.YEAR)+"/"+ ValueUtils.format(month)+"/"+ValueUtils.format(calendar.get(Calendar.DAY_OF_MONTH))+"("+ValueUtils.getDayOfWeek(millisecond)+")/"+ValueUtils.format(calendar.get(Calendar.HOUR_OF_DAY))+":"+ValueUtils.format(calendar.get(Calendar.MINUTE))+":"+ValueUtils.format(calendar.get(Calendar.SECOND));
    }

    /**
     * 获取一个数的绝对值。
     * @param value 数值
     * @return 绝对值
     */
    public static int getAbsoluteValue(int value){
        if(value<0){
            return 0-value;
        }
        return value;
    }

    /**
     * 计算出来的位置，y方向就在anchorView的上面和下面对齐显示，x方向就是与屏幕右边对齐显示
     * 如果anchorView的位置有变化，就可以适当自己额外加入偏移来修正
     * @param anchorView  呼出window的view
     * @param contentView   window的内容布局
     * @return window显示的左上角的xOff,yOff坐标
     */
    public static int[] calculatePopWindowPos(final View anchorView, final View contentView) {
        final int windowPos[] = new int[2];
        final int anchorLoc[] = new int[2];
        // 获取锚点View在屏幕上的左上角坐标位置
        anchorView.getLocationOnScreen(anchorLoc);
        final int anchorHeight = anchorView.getHeight();
        // 获取屏幕的高宽
        final int screenHeight = anchorView.getContext().getResources().getDisplayMetrics().heightPixels;//ScreenUtils.getScreenHeight(anchorView.getContext());
        final int screenWidth = anchorView.getResources().getDisplayMetrics().widthPixels;
        contentView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        // 计算contentView的高宽
        final int windowHeight = contentView.getMeasuredHeight();
        final int windowWidth = contentView.getMeasuredWidth();
        // 判断需要向上弹出还是向下弹出显示
        final boolean isNeedShowUp = (screenHeight - anchorLoc[1] - anchorHeight < windowHeight);
        if (isNeedShowUp) {
            windowPos[0] = screenWidth - windowWidth;
            windowPos[1] = anchorLoc[1] - windowHeight;
        } else {
            windowPos[0] = screenWidth - windowWidth;
            windowPos[1] = anchorLoc[1] + anchorHeight;
        }
        return windowPos;
    }

    public static String getRealPathFromUri(Context context,Uri uri){
        if(uri==null) return "";
        try{
            Cursor cursor = context.getContentResolver().query(uri,new String[]{MediaStore.Files.FileColumns.DATA},//}MediaStore.Images.ImageColumns.DATA},
                    null, null, null);
            if (cursor == null) return uri.getPath();
            else {
                cursor.moveToFirst();
                int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                String path=cursor.getString(index);
                cursor.close();
                return path;
            }
        }catch (java.lang.SecurityException se){
            return context.getResources().getString(R.string.permission_denied);
        }catch (Exception e){
            e.printStackTrace();
            return "";
        }
    }

    /**
     * Use this method in a child thread
     */
    public static Bitmap getDecodedBitmapFromFile(String filepath,long maxSize){
        if(filepath==null) return null;
        BitmapFactory.Options options=new BitmapFactory.Options();
        options.inJustDecodeBounds=true;
        BitmapFactory.decodeFile(filepath,options);
        int width_res=options.outWidth;
        int height_res=options.outHeight;
        int sample=1;
        while (width_res*height_res>maxSize){
            sample*=2;
            width_res=width_res/2;
            height_res=height_res/2;
        }
        options.inSampleSize=sample;
        //Log.d("Scale","Bitmap scale value is "+options.inSampleSize);
        options.inJustDecodeBounds=false;
        return BitmapFactory.decodeFile(filepath,options);
    }

    public static String toDisplaySSIDString(String ssid){
        if(ssid==null||ssid.length()<2) return "";
        return ssid.substring(1,ssid.length()-1);
    }

    /**
     * hh:mm~hh:mm
     * @param beginMin 0~1439
     * @param endMin 0~1439
     * @return hh:mm~hh:mm
     */
    public static String timePeriodFormatValue(int beginMin,int endMin){
        if(beginMin<0||endMin<0||beginMin>=1440||endMin>=1440) return "";
        return format(beginMin/60)+":"+format(beginMin%60)+"~"+format(endMin/60)+":"+format(endMin%60);
    }

    public static double getBrightnessOfRGBColor(int color){
        int red = (color & 0xff0000) >> 16;
        int green = (color & 0x00ff00) >> 8;
        int blue = (color & 0x0000ff);
        return red*0.30+green*0.59+blue*0.11;
    }

    public static double getBrightnessOfRGBColor(String color){
        try{
            int color_value= Color.parseColor(color);
            return getBrightnessOfRGBColor(color_value);
        }catch (Exception e){
            e.printStackTrace();
            return 0;
        }
    }

    public static boolean isHighLightRGB(int color){
        if(getBrightnessOfRGBColor(color)>195) return true;
        return false;
    }

}
