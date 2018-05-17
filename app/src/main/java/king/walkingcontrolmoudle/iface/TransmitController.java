package king.walkingcontrolmoudle.iface;

/**
 * Created by King on 2017/8/7.
 * 指令发送控制接口
 */

public interface TransmitController {
     void send(byte[] data);
     void sendDelayed(byte[] data, long time);
}
