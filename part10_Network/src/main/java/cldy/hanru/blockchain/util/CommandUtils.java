package cldy.hanru.blockchain.util;

import org.apache.commons.lang3.ArrayUtils;

public class CommandUtils {

    /**
     * 规定message type转化后的byte[]长度
     */
    public final static int MESSAGE_TYPE_DATA_LENGTH = 12;







    /**
     * 将消息类型字符串转化长度为12的字节数组
     * @param command
     * @return
     */
    public static byte[] commandToBytes(String command ){
        byte[] byteArray = new byte[MESSAGE_TYPE_DATA_LENGTH];
        for (int i = 0; i < command.getBytes().length; i++) {
            byteArray[i] = command.getBytes()[i];
        }
        return byteArray;
    }


    /**
     * 消息类型byte[] 转化为字符串
     *
     * @param bytes
     * @return
     */
    public static String bytesToCommand(byte[] bytes) {
        byte[] typeBytes = ArrayUtils.removeAllOccurences(bytes, (byte) 0);
        return new String(typeBytes);
    }

}
