package king.walkingcontrolmoudle;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import king.walkingcontrolmoudle.iface.OnConnectionListener;
import king.walkingcontrolmoudle.iface.OnDataInfoListener;
import king.walkingcontrolmoudle.manager.ConnectionManager;
import king.walkingcontrolmoudle.manager.FloatWindowManager;
import king.walkingcontrolmoudle.utils.MessageFormatTool;
import king.walkingcontrolmoudle.view.CircularRod;
import king.walkingcontrolmoudle.R;
/**
 * Created by King on 2018/5/17.
 * 行走控制主界面
 */

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText etIP;
    private EditText etPort;
    private TextView showInfo;
    private Button connect;
    private Button disconnect;
    private ConnectionManager connectionManager;
    private FloatWindowManager floatWindowManager;
    private LayoutInflater layoutInflater;
    private CircularRod circularRod;
    private Handler handler;
    private View parentPanel;
    private ScrollView scrollView;
    private boolean isConnection;
    private static final String TAG="MainActivity";
    private static final int CONNECT_SUCCESS=0x00;
    private static final int CONNECT_FAILD=0x01;
    private static final int AUTO_SCROLL_BOTTOM=0x02;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //初始化主界面
        setContentView(R.layout.activity_main);
        parentPanel=findViewById(R.id.parent_panel);
        etIP=(EditText) findViewById(R.id.et_ip);
        etPort=(EditText) findViewById(R.id.et_port);
        showInfo=(TextView) findViewById(R.id.tv_status);
        connect=(Button)findViewById(R.id.bt_connect);
        disconnect=(Button) findViewById(R.id.bt_disconnect);
        scrollView=(ScrollView) findViewById(R.id.scroll_view);
        connect.setOnClickListener(this);
        disconnect.setOnClickListener(this);
        disconnect.setEnabled(false);

        //初始化连接管理者
        connectionManager = new ConnectionManager(this);
        //监听连接状态，若连接成功，回调success（）方法，失败，则回调faild（）方法。
        connectionManager.setOnConnectionListener(new OnConnectionListener() {
            @Override
            public void success() {
                handler.sendEmptyMessage(CONNECT_SUCCESS);
            }

            @Override
            public void faild() {
                handler.sendEmptyMessage(CONNECT_FAILD);
            }
        });

        //初始化摇杆控制器
        layoutInflater = LayoutInflater.from(this);
        circularRod = (CircularRod) layoutInflater.inflate(R.layout.circular_rod, null);
        //绑定connectionManager,处理指令的发送。（关键）
        circularRod.setTransmitController(connectionManager);
        circularRod.setOnDataInfoListener(new OnDataInfoListener(){
            @Override
            public void publish(String info) {
                updateShowInfo(MessageFormatTool.format(info));
                Log.d(TAG,"publish():info:"+info);
            }
        });

        //初始化悬浮窗管理者
        floatWindowManager=new FloatWindowManager(this);
        parentPanel.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        floatWindowManager.changePosition(event.getRawX(),event.getRawY());
                        break;
                    case MotionEvent.ACTION_UP:
                        Log.d(TAG,"onTouch():sf:离开屏幕");
                        break;
                }
                return false;
            }
        });

        //创建一个主线程（UI线程）Handler，用于更新主界面控件的状态。
        handler=new Handler(this.getMainLooper()){
            @Override
            public void handleMessage(Message msg) {
                switch(msg.what) {
                    case CONNECT_SUCCESS:
                        isConnection=true;
                        //显示摇杆
                        floatWindowManager.show(circularRod);
                        disconnect.setEnabled(true);
                        updateShowInfo(MessageFormatTool.format("控制连接已经成功创建！"));
                        break;
                    case CONNECT_FAILD:
                        isConnection=false;
                        etIP.setEnabled(true);
                        etPort.setEnabled(true);
                        connect.setEnabled(true);
                        updateShowInfo(MessageFormatTool.format("控制连接创建失败！"));
                        break;
                    case AUTO_SCROLL_BOTTOM:
                        scrollView.fullScroll(ScrollView.FOCUS_DOWN);
                        break;
                }
            }
        };

        isConnection=false;
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if(isConnection){
            floatWindowManager.show(circularRod);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        connectionManager.pause();
        floatWindowManager.hide();
        Log.d(TAG,"执行onPause()");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        connectionManager.destroy();
        Log.d(TAG,"执行onDestroy()");
    }

    @Override
    public void onClick(View v) {
        int id=v.getId();
        if(id==R.id.bt_connect){
            if(!isConnection){
                showInfo.setText("");
                connectionManager.bulid(etIP.getText().toString(),etPort.getText().toString());
                etIP.setEnabled(false);
                connect.setEnabled(false);
                etPort.setEnabled(false);
                disconnect.setEnabled(true);
                updateShowInfo(MessageFormatTool.format("正在创建控制连接..."));
            }
        }else if(id==R.id.bt_disconnect){
            if(isConnection){
                floatWindowManager.hide();
                connectionManager.disConnect();
                isConnection=false;
                etIP.setEnabled(true);
                etPort.setEnabled(true);
                connect.setEnabled(true);
                disconnect.setEnabled(false);
                updateShowInfo(MessageFormatTool.format("已关闭控制连接。"));
            }else{
                new AlertDialog.Builder(this).
                        setTitle("提示").
                        setMessage("正在创建连接中，你确定要强制关闭控制连接吗？").
                        setNegativeButton("取消",null).
                        setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                connectionManager.forceToClose();
                                isConnection=false;
                                etIP.setEnabled(true);
                                etPort.setEnabled(true);
                                connect.setEnabled(true);
                                disconnect.setEnabled(false);
                                updateShowInfo(MessageFormatTool.format("已强制关闭控制连接！"));
                            }
                        }).create().show();
            }
        }
    }

    private void updateShowInfo(String message){
        if(showInfo.getText().length()==0||showInfo.getText().length()>500) {
            showInfo.setText(message);
        } else{
            showInfo.append("\n"+message);
        }
        handler.sendEmptyMessageDelayed(AUTO_SCROLL_BOTTOM,100);
    }
}
