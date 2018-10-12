package cldy.hanru.blockchain.net;

import cldy.hanru.blockchain.util.ByteUtils;
import cldy.hanru.blockchain.util.CommandUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class ServerSocketClient {


    public static void sendData(String toAddress, String command, byte[] payload) {
        /*
         * 第一个参数：IP：InetAddress，String，要连接的服务端的ip
         *
         * 第二个参数：int port，要连接的服务端的程序的端口。
         */
        Socket client = null;
        OutputStream outputStream = null;
        try {
            //step1:创建客户端对象
            //指定要链接的主节点的地址和端口
            String address = toAddress.substring(0, toAddress.indexOf(":"));
            int port = Integer.parseInt(toAddress.substring(toAddress.indexOf(":") + 1));
            client = new Socket(address, port);
            System.out.println("客户端已经建立，同时向服务端发送连接请求。。" + address + "，" + port);
            //step2：从Socket中获取输出流
            outputStream = client.getOutputStream();
            outputStream.write(toData(command, payload));

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            //step3：关闭资源
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (client != null) {//可以省略不写
                try {
                    client.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    /**
     * 将要发送的数据进行封装：
     * 格式为：type + data length + data
     *
     * @param payload
     * @return
     */
    public static byte[] toData(String command, byte[] payload) {
        byte[] commandBytes = CommandUtils.commandToBytes(command);
        byte[] dataLenBytes = ByteUtils.toBytes(payload.length);
        System.out.println("写出payload长度：" + payload.length);
        byte[] bytes = ByteUtils.merge(commandBytes,dataLenBytes,payload);
        return bytes;
    }

}
