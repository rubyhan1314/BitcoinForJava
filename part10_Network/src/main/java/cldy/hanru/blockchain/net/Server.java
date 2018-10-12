package cldy.hanru.blockchain.net;

import cldy.hanru.blockchain.block.Blockchain;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * 节点服务器
 * @author hanru
 */
public class Server {
    static{
        knowNodes = new ArrayList<>();
        Server.knowNodes.add("localhost:3000");
    }

    //存储节点的地址
    public static List<String> knowNodes ;

    public static String nodeAddress; //全局变量，节点地址

    public static String minerAddress ="";


    public static void startServer(String nodeID, String minerAdd) {
//        knowNodes.add("localhost:3000");//localhost:3000 主节点的地址
        System.out.println("knowNodes(0):"+knowNodes.get(0));
        // 当前节点的IP地址
        nodeAddress = String.format("localhost:%s", nodeID);
        minerAddress = minerAdd;
        System.out.printf("nodeAddress:%s,minerAddress:%s\n", nodeAddress, minerAddress);
        ServerSocket server = null;
        Socket socket = null;
        try {
            server = new ServerSocket(Integer.parseInt(nodeID));
            System.out.println("服务端已经建立，等待客户端的连接请求。。。");

            //获取blockchain
            Blockchain bc = Blockchain.initBlockchainFromDB(nodeID);
            System.out.println("blockchain-->"+bc.getNodeID());

            // 第一个终端：端口为3000,启动的就是主节点
            // 第二个终端：端口为3001，钱包节点
            // 第三个终端：端口号为3002，矿工节点
            if (!knowNodes.get(0).equals(nodeAddress)) {
                // 此节点是钱包节点或者矿工节点，需要向主节点发送请求同步数据
//                System.out.printf("knowNodes:%s\n", knowNodes.get(0));
                ServerSend.sendVersion(knowNodes.get(0), bc);
            }

            while (true) {
                // 收到的数据的格式是固定的，12字节+长度+ 结构体字节数组
                socket = server.accept();//该方法是阻塞式
                System.out.println("已有客户端连入。。客户端的ip：" + socket.getInetAddress() + "客户端的port：" + socket.getPort());
                //业务处理：放在子线程中
                new Thread(new ServerThread(socket, bc)).start();

            }


        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            if (socket != null) {//可以省略不写
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 判断节点地址是否在集合中
     * @param addr
     * @return
     */
    public static boolean nodeIsKnown(String addr) {

        for (String address : knowNodes) {
            if (addr == address) {
                return true;
            }
        }
        return false;

    }

}



