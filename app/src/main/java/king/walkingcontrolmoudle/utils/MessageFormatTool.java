package king.walkingcontrolmoudle.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by King on 2018/5/17.
 */

public class MessageFormatTool {
    public static String format(String message){
       return new SimpleDateFormat("yyyy-MM-dd HH:mm E").format(new Date())+":"+message;
    }
}
