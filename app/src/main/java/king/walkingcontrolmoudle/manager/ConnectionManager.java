package king.walkingcontrolmoudle.manager;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

import king.walkingcontrolmoudle.iface.OnConnectionListener;
import king.walkingcontrolmoudle.iface.TransmitController;
import king.walkingcontrolmoudle.utils.DataFormatTool;


/**
 * Created by King on 2017/8/7.
 * 控制连接管理
 */

public class ConnectionManager implements TransmitController {

    private Context context;
    private Handler handler;
    private HandlerThread handlerThread;
    private Socket socket;
    private OutputStream outputStream;

    private final int C0NNECTION_BUILD=1000;
    private final int SEND_DATA=1001;
    private boolean isConnected=false;
    private boolean isSending=false;
    private OnConnectionListener onConnectionListener;

    private final String FLAG="ConnectionManager_HandlerThread";
    private final String TAG="ConnectionManager";

    private String host;
    private String port;

    public ConnectionManager(final Context context){
        this.context=context;
        handlerThread=new HandlerThread(FLAG);
        handlerThread.start();
        handler=new Handler(handlerThread.getLooper()){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch(msg.what){
                    case C0NNECTION_BUILD:
                        //判断是否初始化成功
                        boolean is=initSocket();
                        if(is){
                            isConnected=true;
                            Log.d(TAG,"handleMessage():连接建立成功！");
                            Toast.makeText(context,"控制连接建立成功！", Toast.LENGTH_SHORT).show();
                            if(onConnectionListener!=null){
                                onConnectionListener.success();
                            }
                        }else{
                            isConnected=false;
                            Log.d(TAG,"handleMessage():连接建立失败！");
                            Toast.makeText(context,"控制连接建立失败！", Toast.LENGTH_SHORT).show();
                            if(onConnectionListener!=null){
                                onConnectionListener.faild();
                            }
                        }
                        break;
                    case SEND_DATA:
                        if(outputStream!=null){
                            try {
                                byte data[]=(byte[])msg.obj;
                                outputStream.write(data);
                                if(isConnected&&isSending)
                                {
                                    outputStream.flush();
                                    Log.d(TAG,"handleMessage():清除缓冲区");
                                    //Toast.makeText(context,"数据发送:"+DataFormatTool.obtainString(data),Toast.LENGTH_SHORT).show();
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        isSending=false;
                        break;
                }
            }
        };

    }
    public ConnectionManager bulid(String host,String port){
        if(host==null||port==null){
            Toast.makeText(context,"目标无效！",Toast.LENGTH_SHORT).show();
        }else {
            if(isConnected)
            {
                Log.d(TAG,"bulid():连接已经建立,请先关闭当前连接！");
            }else {
                this.host=host;
                this.port=port;
                handler.sendEmptyMessage(C0NNECTION_BUILD);
                Log.d(TAG,"bulid():发送建立连接消息");
            }
        }
        return this;
    }
    public void disConnect(){
        if(isConnected) {
            pause();
            closeSocket();
        }
    }
    public void setOnConnectionListener(OnConnectionListener onConnectionListener){
        this.onConnectionListener=onConnectionListener;
    }
    @Override
    public void send(byte[] data) {
        sendDelayed(data,0);
        Log.d(TAG,"send()"+ DataFormatTool.obtainString(data));
    }

    @Override
    public void sendDelayed(byte[] data, long time) {
        if(isConnected&&!isSending&&data!=null&&handler!=null)
        {
            isSending=true;
            if(time<0||time>30000)
            {
               time=0;
            }
            if(handler.hasMessages(SEND_DATA))
            {
                handler.removeMessages(SEND_DATA);
            }
            Message msg=handler.obtainMessage();
            msg.what=SEND_DATA;
            msg.obj=data;
            handler.sendMessageDelayed(msg,time);
            Log.d(TAG,"sendDelayed():发送数据包消息:time:"+time);
        }
    }

    public void pause(){
        isSending=false;
        byte data[];
        if(PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean("override_control_mode", false)){
            data= DataFormatTool.obtainBytes(PreferenceManager.getDefaultSharedPreferences(context).getString("control_stop",null));
        }else {
            data = new byte[8];
            data[0] = (byte) 0x55;
            data[1] = (byte) 0x00;
            data[2] = (byte) 0xff;
            data[3] = (byte) 0x00;
            data[4] = (byte) 0xff;
            data[5] = (byte) 0x00;
            data[6] = (byte) 0x00;
            data[7] = (byte) 0xaa;
        }
        send(data);
        Log.d(TAG,"执行pause()");
    }

    public void destroy(){
        pause();
        closeSocket();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            handlerThread.quitSafely();
        }
    }
    //初始化套接字
    private boolean initSocket(){
//        SharedPreferences sp= PreferenceManager.getDefaultSharedPreferences(context);
//        String ip=sp.getString("ip", "none");
//        int port= Integer.valueOf(sp.getString("port","-1"));
        Log.d(TAG,"initSocket():初始化Socket：host:"+host+" port:"+port);
        if(host==null||port==null)return false;
        //获取一个Socket对象，建立网络连接。
        //可以尝试调整Socket参数，以优化连接。
        try {
            socket=new Socket(host,Integer.parseInt(port));
            //socket.setSoTimeout(10000);
            //socket.setSoLinger(true,30);
            socket.setTcpNoDelay(true);
            //socket.setSendBufferSize(8);
            socket.setKeepAlive(true);
            outputStream=socket.getOutputStream();
            return true;
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void forceToClose(){
        try {
            if(socket!=null){
                socket.close();
            }
            if(outputStream!=null){
                outputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            socket=null;
            outputStream=null;
        }
        isConnected=false;
    }
    private void closeSocket(){
        if(isConnected)
        {
          forceToClose();
        }
        isConnected=false;
    }
}
