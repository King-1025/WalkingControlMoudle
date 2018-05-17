package king.walkingcontrolmoudle.utils;

import android.util.Log;
import android.widget.Toast;

/**
 * Created by King on 2017/8/8.
 * 指令格式化工具
 */

public class DataFormatTool {

    private static final String TAG="DataFormatTool";

    public static String obtainString(byte[] bys){
        if(bys==null){
            return null;
        }
        String str=bys.length+":";
        for(int i=0;i<bys.length;i++){
            int temp=(int) bys[i];
            if(temp<0) {temp=256+temp;}//取反
            if(temp>255||temp<0){
                str+="["+i+":"+temp+"]";
            }else{
                str+=intToTwoHexString(temp,true);
            }
        }
        return str;
    }

    public static byte[] obtainBytes(String str){
        if(str==null)
        {return null;}
        int length=str.length();
        int dlen=length/2;
        byte data[]=new byte[dlen];
        str=str.toLowerCase();
        for(int i=0;i<dlen;i++)
        {
            int i0=charToInt(str.charAt(2*i),false);
            if(i0==9999)
            {
                Log.d(TAG,"obtainBytes():无法解析未知字符:"+str.charAt(2*i));
                return null;
            }
            int i1=charToInt(str.charAt(2*i+1),false);
            if(i1==9999)
            {
                 Log.d(TAG,"obtainBytes():无法解析未知字符:"+str.charAt(2*i+1));
                return null;
            }
            data[i]=(byte)(16*i0+i1);
        }
        return data;
    }

    //字符转整型
    private static int charToInt(char ch,boolean bl){
        int value=9999;
        String rule="0123456789abcdef";
        if(bl)
        {rule=rule.toUpperCase();}
        int mode=rule.length();
        for(int i=0;i<mode;i++)
        {
            if(ch==rule.charAt(i))
            {
                value=i;
                break;
            }
        }
        return value;
    }

    private static String intToTwoHexString(int it, boolean bl){

        for (int i=0;i<16;i++)
        {
            int a=it-i;
            if(a%16==0)
            {
                int first=a/16;
                int second=i;
                if(first>=0&&first<16)
                {
                    //匹配成功
                    String temp=catchMode(first)+catchMode(second);
                    if(bl){
                        temp=temp.toUpperCase();
                    }
                    return temp;
                }
            }
        }
        return null;
    }

    private static String catchMode(int value){

        switch(value)
        {
            case 10:
                return "a";
            case 11:
                return "b";
            case 12:
                return "c";
            case 13:
                return "d";
            case 14:
                return "e";
            case 15:
                return "f";
            default:
                return value+"";
        }

    }
}
