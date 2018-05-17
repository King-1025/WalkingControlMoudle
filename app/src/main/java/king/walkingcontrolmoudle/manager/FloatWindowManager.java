package king.walkingcontrolmoudle.manager;

import android.content.Context;
import android.graphics.PixelFormat;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import king.walkingcontrolmoudle.iface.OnPositionChanger;


/**
 * Created by King on 2017/8/9.
 * 悬浮控制杆管理
 */

public class FloatWindowManager implements OnPositionChanger {
    private Context context;
    private WindowManager windowManager;
    private WindowManager.LayoutParams layoutParams;
    private int windowWidth;
    private int windowHeight;
    //private int statusBarHeight;
    //private int titileBarHeight;
    private int floatWidth=350;
    private int floatHeight=350;
    private int defPositionX=0;
    private int defPositionY=0;
    private View view;
    private boolean isViewBinded=false;
    private final String TAG="FloatWindowManager";
    public FloatWindowManager(Context context){
        this.context=context;
        //初始化悬浮窗
        windowManager= (WindowManager)context.getApplicationContext().getSystemService(context.getApplicationContext().WINDOW_SERVICE);
        layoutParams=new WindowManager.LayoutParams();
        layoutParams.type= WindowManager.LayoutParams.TYPE_PHONE;
        layoutParams.flags= WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL| WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        layoutParams.width= floatWidth;
        layoutParams.height=floatHeight;
        layoutParams.format= PixelFormat.RGBA_8888;
        layoutParams.x=defPositionX;
        layoutParams.y=defPositionY;
        //初始化屏幕宽和高
        windowWidth=windowManager.getDefaultDisplay().getWidth();
        windowHeight=windowManager.getDefaultDisplay().getHeight();
       // this.statusBarHeight=statusBarHeight;
       // this.titileBarHeight=titileBarHeight;
    }

    public int getWindowWidth(){
        return windowWidth;
    }
    public int getWindowHeight(){
        return windowHeight;
    }
    public void show(View v){
        if(v==null){
            return;
        }
        if(isViewBinded){
            Log.d(TAG,"bind():View已经绑定:isViewBinded:"+isViewBinded);
        }else {
            windowManager.addView(v, layoutParams);
            view=v;
            isViewBinded=true;
        }
    }
    public void hide(){
        if(isViewBinded)
        {
            windowManager.removeView(view);
            isViewBinded=false;
        }
    }

    @Override
    public void changePosition(float x, float y) {
        if(isViewBinded)
        {
            layoutParams.x = (int) (x - windowWidth / 2);
            layoutParams.y = (int) (y - windowHeight / 2);
            windowManager.updateViewLayout(view, layoutParams);
        }

    }
}
