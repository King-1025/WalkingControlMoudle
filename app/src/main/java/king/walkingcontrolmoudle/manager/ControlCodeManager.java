package king.walkingcontrolmoudle.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by King on 2018/5/17.
 * 控制指令管理
 */

public class ControlCodeManager {
    private Context context;

    public final static String KEY_OVERRIDE_CONTROL_MODE="override_control_mode";
    public final static String KEY_CONTROL_RB="control_rb";
    public final static String KEY_CONTROL_BACK="control_back";
    public final static String KEY_CONTROL_LB="control_lb";
    public final static String KEY_CONTROL_LEFT="control_left";
    public final static String KEY_CONTROL_LF="control_lf";
    public final static String KEY_CONTROL_FRONT="control_front";
    public final static String KEY_CONTROL_RF="control_rf";
    public final static String KEY_CONTROL_RIGHT="control_right";
    public final static String KEY_CONTROL_STOP="control_stop";

    public ControlCodeManager(Context context){
        this.context=context;
        initControlCode();
    }

    private void initControlCode(){
        //指令定义
        SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putBoolean(KEY_OVERRIDE_CONTROL_MODE,false);//false：不启用自定义指令，true：启用自定义指令
        editor.putString(KEY_CONTROL_RB,"");//右后
        editor.putString(KEY_CONTROL_BACK,"");//后退
        editor.putString(KEY_CONTROL_LB,"");//左后
        editor.putString(KEY_CONTROL_LEFT,"");//左
        editor.putString(KEY_CONTROL_LF,"");//左前
        editor.putString(KEY_CONTROL_FRONT,"");//前进
        editor.putString(KEY_CONTROL_RF,"");//右前
        editor.putString(KEY_CONTROL_RIGHT,"");//右
        editor.putString(KEY_CONTROL_STOP,"");//停止
        editor.commit();
    }
}
