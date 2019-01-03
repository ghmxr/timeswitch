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
import com.github.ghmxr.timeswitch.data.PublicConsts;

import java.util.Calendar;
import java.util.regex.PatternSyntaxException;

/**
 * @author mxremail@qq.com  https://github.com/ghmxr/timeswitch
 */
public class ValueUtils {

    /**
     * ��һ����������ת��Ϊ��0����Ӧfalse����1����Ӧtrue������Ӣ�Ķ��Ÿ������ַ�����
     * @param array Ҫת���Ĳ�������
     * @return  ת����ɵ��ַ���
     */
    public static String booleanArray2String(boolean [] array){
        StringBuilder values=new StringBuilder("");
        if(array==null){
            return "";
        }
        if(array.length<=0){
            return "";
        }
        for(int i=0;i<array.length;i++){
            if(array[i]){
                values.append(1);
            }
            else{
                values.append(0);
            }
            if(i<(array.length-1)&&array.length>1) values.append(PublicConsts.SEPARATOR_FIRST_LEVEL);
        }

        return values.toString();
    }

    /**
     * ���ַ���ת��Ϊ�������飬���������Ϊ����0��1��ɵĲ���Ӣ�Ķ��Ÿ������ַ��������磺 0,0,1,1  �򷵻� {false,false,true,true}   ��
     * ���������ַ����޷����ת����᷵��һ������Ϊ0���²�������ʵ��
     * @param text Ҫת�����ַ���ֵ
     * @return ת����ɵĲ�������
     */

    public static boolean [] string2booleanArray(String text){
        boolean[] array=null;
        if(text==null){
            return new boolean[0];
        }

        String[] sarray;

        try{
            sarray=text.split(PublicConsts.SPLIT_SEPARATOR_FIRST_LEVEL);
            if(sarray.length<=0) return new boolean[0];
            if(sarray.length==1){
                array=new boolean [1];
                if(Integer.parseInt(sarray[0])==1) array[0]=true;
                return array;
            }
            array=new boolean[sarray.length];
            for(int i=0;i<array.length;i++){
                if(Integer.parseInt(sarray[i])==1){
                    array[i]=true;
                }
            }
        }catch(PatternSyntaxException pe){
            pe.printStackTrace();
        }catch (NumberFormatException ne){
            ne.printStackTrace();
        }

        if(array==null){
            return new boolean[0];
        }
        return array;

    }

    /**
     * ��int����ת��Ϊstring���ͣ�����Ӣ�Ķ��Ÿ�����
     * ���� {0,1,2} ��ת��Ϊ "0,1,2"
     * @param array int[]
     * @return string
     */

    public static String intArray2String(int[] array){
        StringBuilder valuesBuilder=new StringBuilder("");
        if(array==null){
            return "";
        }
        else{
            if(array.length<=0){
                return "";
            }
            for(int i=0;i<array.length;i++){
                //values+=""+array[i];
                valuesBuilder.append(array[i]);
                if(i<(array.length-1)&&array.length>1){
                    //values+=",";
                    valuesBuilder.append(PublicConsts.SEPARATOR_FIRST_LEVEL);
                }
            }
        }
        return valuesBuilder.toString();
    }

    /**
     * ����Ӣ�Ķ��Ÿ����������ַ���ת��Ϊint����
     * ����  "0,1,2" ת��Ϊ  {0,1,2}
     * @param text
     * @return int[]
     */
    public static int[] string2intArray(String text){
        if(text==null){
            return new int[0];
        }
        else{
            int[] values=null;
            try{
                String[] svalues=text.split(PublicConsts.SPLIT_SEPARATOR_FIRST_LEVEL);
                if(svalues.length<=0) return new int[0];
                if(svalues.length==1){
                    values=new int[1];
                    values[0]=Integer.parseInt(svalues[0]);
                    return values;
                }
                values=new int[svalues.length];
                for(int i=0;i<svalues.length;i++){
                    values[i]=Integer.parseInt(svalues[i]);
                }
            }catch(PatternSyntaxException pe){
                pe.printStackTrace();
            }catch(NumberFormatException ne){
                ne.printStackTrace();
            }

            if(values==null) return new int[0];
            return  values;

        }
    }

    /**
     * ����Ӣ�Ķ��Ÿ����������ַ���ת��Ϊlong����
     * ����  "0,1,2" ת��Ϊ  {0,1,2}
     * @param text
     * @return long[]
     */
    public static long[] string2longArray(String text){
        if(text==null){
            return new long[0];
        }
        else{
            long[] values=null;
            try{
                String[] svalues=text.split(PublicConsts.SPLIT_SEPARATOR_FIRST_LEVEL);
                if(svalues.length<=0) return new long[0];
                if(svalues.length==1){
                    values=new long[1];
                    values[0]=Long.parseLong(svalues[0]);
                    return values;
                }
                values=new long[svalues.length];
                for(int i=0;i<svalues.length;i++){
                    values[i]=Long.parseLong(svalues[i]);
                }
            }catch(PatternSyntaxException pe){
                pe.printStackTrace();
            }catch(NumberFormatException ne){
                ne.printStackTrace();
            }

            if(values==null) return new long[0];
            return  values;

        }
    }

    /**
     * ��long����ת��Ϊstring���ͣ�����Ӣ�Ķ��Ÿ�����
     * ���� {0,1,2} ��ת��Ϊ "0,1,2"
     * @param array long[]
     * @return string
     */
    public static String longArray2String(long[] array){
        StringBuilder valuesBuilder=new StringBuilder("");
        if(array==null){
            return "";
        }
        else{
            if(array.length<=0){
                return "";
            }
            for(int i=0;i<array.length;i++){
                //values+=""+array[i];
                valuesBuilder.append(array[i]);
                if(i<(array.length-1)&&array.length>1){
                    //values+=",";
                    valuesBuilder.append(PublicConsts.SEPARATOR_FIRST_LEVEL);
                }
            }
        }
        return valuesBuilder.toString();
    }

    /**
     * ��String����ת��ΪӢ�Ķ��Ÿ������ַ�����
     * @param array �ַ�������
     * @return Ӣ�Ķ��Ÿ������ַ���
     */
    public static String stringArray2String(String[] array){
        StringBuilder builder=new StringBuilder("");
        if(array==null||array.length==0) return "";
        for(int i=0;i<array.length;i++){
            builder.append(array[i]);
            if(i<(array.length-1)) builder.append(PublicConsts.SEPARATOR_FIRST_LEVEL);
        }
        return builder.toString();
    }

    /**
     * ����Ӣ�Ķ��Ÿ������ַ���ת��Ϊ�ַ�������
     * @param value ��Ӣ�Ķ��Ÿ������ַ���
     * @return �ַ�������
     */
    public static String[] string2StringArray(String value){
        if(value==null||value.equals("")) return new String[0];
        String [] values;
        try{
            values=value.split(PublicConsts.SPLIT_SEPARATOR_FIRST_LEVEL);
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
     * ������ʱ��ת��Ϊ�죬Сʱ�����ӵ��ַ���
     * @param context context
     * @param milliseconds ����ʱ��
     * @return �죬Сʱ���ֵ��ַ���
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
     * ��ȡ�ܼ���Ӣ����д
     * @param timeofmillis ����ʱ�����ֵ
     * @return �ܼ���Ӣ����д�ַ�����������һ "MON"
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
     * ��ȡ�����ʽ������ʱ��ֵ�ַ���"yyyy/mm/dd/(WFD)HH:MM:SS"
     * @param millisecond ʱ��ֵ
     * @return �����ַ���
     */
    public static String getFormatDateTime(long millisecond){
        Calendar calendar=Calendar.getInstance();
        calendar.setTimeInMillis(millisecond);
        int month=calendar.get(Calendar.MONTH)+1;
        return calendar.get(Calendar.YEAR)+"/"+ ValueUtils.format(month)+"/"+ValueUtils.format(calendar.get(Calendar.DAY_OF_MONTH))+"("+ValueUtils.getDayOfWeek(millisecond)+")/"+ValueUtils.format(calendar.get(Calendar.HOUR_OF_DAY))+":"+ValueUtils.format(calendar.get(Calendar.MINUTE))+":"+ValueUtils.format(calendar.get(Calendar.SECOND));
    }

    /**
     * ��ȡһ�����ľ���ֵ��
     * @param value ��ֵ
     * @return ����ֵ
     */
    public static int getAbsoluteValue(int value){
        if(value<0){
            return 0-value;
        }
        return value;
    }

    /**
     * ���������λ�ã�y�������anchorView����������������ʾ��x�����������Ļ�ұ߶�����ʾ
     * ���anchorView��λ���б仯���Ϳ����ʵ��Լ��������ƫ��������
     * @param anchorView  ����window��view
     * @param contentView   window�����ݲ���
     * @return window��ʾ�����Ͻǵ�xOff,yOff����
     */
    public static int[] calculatePopWindowPos(final View anchorView, final View contentView) {
        final int windowPos[] = new int[2];
        final int anchorLoc[] = new int[2];
        // ��ȡê��View����Ļ�ϵ����Ͻ�����λ��
        anchorView.getLocationOnScreen(anchorLoc);
        final int anchorHeight = anchorView.getHeight();
        // ��ȡ��Ļ�ĸ߿�
        final int screenHeight = anchorView.getContext().getResources().getDisplayMetrics().heightPixels;//ScreenUtils.getScreenHeight(anchorView.getContext());
        final int screenWidth = anchorView.getResources().getDisplayMetrics().widthPixels;
        contentView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        // ����contentView�ĸ߿�
        final int windowHeight = contentView.getMeasuredHeight();
        final int windowWidth = contentView.getMeasuredWidth();
        // �ж���Ҫ���ϵ����������µ�����ʾ
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
            LogUtil.putExceptionLog(context,e);
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
            width_res=width_res/sample;
            height_res=height_res/sample;
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
