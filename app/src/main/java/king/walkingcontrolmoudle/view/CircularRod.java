package king.walkingcontrolmoudle.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.text.SimpleDateFormat;
import java.util.Date;

import king.walkingcontrolmoudle.iface.OnDataInfoListener;
import king.walkingcontrolmoudle.iface.TransmitController;

import king.walkingcontrolmoudle.R;
import king.walkingcontrolmoudle.manager.ControlCodeManager;
import king.walkingcontrolmoudle.utils.DataFormatTool;

/**
 * Created by King on 2018/5/17.
 * 控制杆，这是一个自定义的控件。
 */
public class CircularRod extends View {

    private Context context;
    private Drawable outerDrawable;
    private Drawable innerDrawable;
    //中心坐标(控件左上角为原点)
    private int centerX;
    private int centerY;
    //内圆坐标(控件左上角为原点)
    private int innerCircleX;
    private int innerCircleY;

    private int innerCircleR;
    private int outerCircleR;

    private TransmitController transmitController;
    private OnDataInfoListener onDataInfoListener;

    private final String TAG="CircularRod";

    public CircularRod(Context context) {
        super(context);
        init(context,null, 0);
    }

    public CircularRod(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context,attrs, 0);
    }

    public CircularRod(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context,attrs, defStyle);
    }

    private void init(Context context,AttributeSet attrs, int defStyle) {
        this.context=context;
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.CircularRod, defStyle, 0);

        if(a.hasValue(R.styleable.CircularRod_bgOuterDrawable)){
            outerDrawable=a.getDrawable(R.styleable.CircularRod_bgOuterDrawable);
            outerDrawable.setCallback(this);
        }
        if(a.hasValue(R.styleable.CircularRod_bgInnerDrawable)){
            innerDrawable=a.getDrawable(R.styleable.CircularRod_bgInnerDrawable);
            innerDrawable.setCallback(this);
        }
        a.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int w=getWidth();
        int h=getHeight();
        centerX =innerCircleX= w/2;
        centerY =innerCircleY= h/2;
        if(w<=h){
            innerCircleR= (int) (w*0.2);
            outerCircleR=centerX;
        }else{
            innerCircleR= (int) (h*0.2);
            outerCircleR=centerY;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (outerDrawable != null) {
            outerDrawable.setBounds(centerX-outerCircleR,centerY-outerCircleR,
                    centerX+outerCircleR,centerY+outerCircleR);
            outerDrawable.setAlpha(128);
            outerDrawable.draw(canvas);
        }
        if (innerDrawable != null) {
            innerDrawable.setBounds(innerCircleX-innerCircleR,innerCircleY-innerCircleR,
                    innerCircleX+innerCircleR,innerCircleY+innerCircleR);
            outerDrawable.setAlpha(245);
            innerDrawable.draw(canvas);
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        byte temp[]=null;
        switch(event.getAction()){
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                double x=event.getX()-centerX;
                double y=event.getY()-centerY;
                double z= Math.sqrt(x*x+y*y);
                double d= Math.abs(outerCircleR-innerCircleR);
                if(z>0) {
                    double sinY = y / z;
                    double w= Math.toDegrees(Math.asin(sinY));
                    if(x<0){
                        w=180-w;
                    }else if(x>=0&&y<0){
                        w=360+w;
                    }
                    if (z <= d) {
                        innerCircleX = (int) event.getX();
                        innerCircleY = (int) event.getY();
                        temp=makeData(w,z,d);
                    } else {
                        double sinX = x / z;
                        innerCircleX = (int) (centerX + d * sinX);
                        innerCircleY = (int) (centerY + d * sinY);
                        temp=makeData(w,d,d);
                    }
                }else{
                    temp=makeData(0,0,0);
                }
                break;
            case MotionEvent.ACTION_UP:
                innerCircleX=centerX;
                innerCircleY=centerY;
                temp=makeData(0,0,0);
                break;
        }
        invalidate();
        if(transmitController!=null)
        {transmitController.send(temp);}
        return false;
    }

    public void setTransmitController(TransmitController transmitController){
        this.transmitController=transmitController;
    }

    public void setOnDataInfoListener(OnDataInfoListener onDataInfoListener){
        this.onDataInfoListener=onDataInfoListener;
    }

    //控制指令格式化
    @RequiresApi(api = Build.VERSION_CODES.N)
    private byte[] makeData(double w, double z, double d){
        boolean isCustomedControl= PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(ControlCodeManager.KEY_OVERRIDE_CONTROL_MODE, false);
        byte data[]=new byte[8];
        data[0]=(byte)0x55;
        data[1]=(byte)0x00;
        data[2]=(byte)0xff;
        data[3]=(byte)0x00;
        data[4]=(byte)0xff;
        data[5]=(byte)0x00;
        data[6]=(byte)0x00;
        data[7]=(byte)0xaa;
        if(z>0){
            if(w>=22.5&&w<=67.5){
                //右下
                if(isCustomedControl){
                    data= DataFormatTool.obtainBytes(PreferenceManager.getDefaultSharedPreferences(context)
                           .getString(ControlCodeManager.KEY_CONTROL_RB,null));
                }else {
                    data[1] = data[3]=(byte)2;
                    data[2] = (byte)64;
                    data[4] = (byte)192;
                }
                Log.d(TAG,"右下 w:"+w+" z:"+z);
            }else if(w>67.5&&w<112.5){
                //下
                if(isCustomedControl){
                    data= DataFormatTool.obtainBytes(PreferenceManager.getDefaultSharedPreferences(context)
                            .getString(ControlCodeManager.KEY_CONTROL_BACK,null));
                }else {
                    data[1] = data[3] = (byte)2;
                    data[2] = data[4] = (byte)(255* (1 - z / d));
                }
                Log.d(TAG,"下 w:"+w+" z:"+z);
            }else if(w>=112.5&&w<=157.5){
                //左下
                if(isCustomedControl){
                    data= DataFormatTool.obtainBytes(PreferenceManager.getDefaultSharedPreferences(context)
                            .getString(ControlCodeManager.KEY_CONTROL_LB,null));
                }else {
                    data[1] = data[3] = (byte)2;
                    data[2] = (byte) 192;
                    data[4] = (byte) 64;
                }
                Log.d(TAG,"左下 w:"+w+" z:"+z);
            }else if(w>157.5&&w<202.5){
                //左
                if(isCustomedControl){
                    data= DataFormatTool.obtainBytes(PreferenceManager.getDefaultSharedPreferences(context)
                            .getString(ControlCodeManager.KEY_CONTROL_LEFT,null));
                }else {
                    data[1]=data[3] =(byte)1 ;
                    data[4] = (byte) (255 * (1 - z / d));
                    Log.d(TAG, "左 w:" + w + " z:" + z);
                }
            }else if(w>=202.5&&w<=247.5){
                //左上
                if(isCustomedControl){
                    data= DataFormatTool.obtainBytes(PreferenceManager.getDefaultSharedPreferences(context)
                            .getString(ControlCodeManager.KEY_CONTROL_LF,null));
                }else {
                    data[1] = data[3] = (byte)1;
                    data[2] = (byte) 192;
                    data[4] = (byte) 64;
                }
                Log.d(TAG,"左上 w:"+w+" z:"+z);
            }else if(w>247.5&&w<292.5){
                //上
                if(isCustomedControl){
                    data= DataFormatTool.obtainBytes(PreferenceManager.getDefaultSharedPreferences(context)
                            .getString(ControlCodeManager.KEY_CONTROL_FRONT,null));
                }else {
                    data[1] = data[3] = (byte)1;
                    data[2] = data[4] = (byte) (255* (1 - z / d));
                    Log.d(TAG, "上 w:" + w + " z:" + z);
                }
            }else if(w>=292.5&&w<=337.5){
                //右上
                if(isCustomedControl){
                    data= DataFormatTool.obtainBytes(PreferenceManager.getDefaultSharedPreferences(context)
                            .getString(ControlCodeManager.KEY_CONTROL_RF,null));
                }else {
                    data[1] = data[3] = (byte)1;
                    data[2] = (byte) 64;
                    data[4] = (byte) 192;
                }
                Log.d(TAG,"右上 w:"+w+" z:"+z);
            }else{
                //右
                if(isCustomedControl){
                    data= DataFormatTool.obtainBytes(PreferenceManager.getDefaultSharedPreferences(context)
                            .getString(ControlCodeManager.KEY_CONTROL_RIGHT,null));
                }else {
                    data[1] = data[3] =(byte)1;
                    data[2] = (byte) (255* (1 - z / d));
                }
                Log.d(TAG,"右 w:"+w+" z:"+z);
            }
        }else{
            if(isCustomedControl){
                data= DataFormatTool.obtainBytes(PreferenceManager.getDefaultSharedPreferences(context)
                        .getString(ControlCodeManager.KEY_CONTROL_STOP,null));
            }
            Log.d(TAG,"没有移动->z:"+z);
        }
        if(onDataInfoListener!=null)
        {
            //String time=new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss").format(new Date(System.currentTimeMillis()));
            String info=" 角度:"+w+" 圆心距:"+z+" 限制距："+d+" 数据包:"+DataFormatTool.obtainString(data);
            onDataInfoListener.publish(info);
            Log.d(TAG,"makeData():info:"+info);
        }
        return data;
    }
}
