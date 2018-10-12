package cldy.hanru.blockchain.net;

import cldy.hanru.blockchain.block.Blockchain;
import cldy.hanru.blockchain.util.ByteUtils;
import cldy.hanru.blockchain.util.CommandUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

@Slf4j
@AllArgsConstructor
/**
 * 处理从其他节点，接收到的消息
 * @author hanru
 */
public class ServerThread implements Runnable {
    private Socket socket;
    private Blockchain bc;


    @Override
    public void run() {
        InputStream inputStream = null;
        try {
            inputStream = socket.getInputStream();
            byte[] commandData = new byte[CommandUtils.MESSAGE_TYPE_DATA_LENGTH];
            // 消息数据长度 byte array
            byte[] dataLen = new byte[4];

            if (inputStream.read(commandData) != CommandUtils.MESSAGE_TYPE_DATA_LENGTH) {
                throw new IOException("EOF in PeerMessage constructor: command");
            }

            if (inputStream.read(dataLen) != 4) {
                throw new IOException("EOF in PeerMessage constructor: dataLen");
            }


            int len = ByteUtils.toInt(dataLen);
            System.out.println("data的长度是：" + len);
            byte[] data = new byte[len];

            if (inputStream.read(data) != len) {
                throw new IOException("EOF in PeerMessage constructor: Unexpected message data length");
            }

            String command = CommandUtils.bytesToCommand(commandData);
            System.out.printf("Receive a Message:%s\n", command);

            switch (command) {
                case ServerConst.COMMAND_VERSION:
                    ServerHandle.handleVersion(data, bc);
                    break;
                case ServerConst.COMMAND_GETBLOCKS:
                    ServerHandle.handleGetblocks(data, bc);
                    break;
                case ServerConst.COMMAND_INV:
                    ServerHandle.handleInv(data, bc);
                    break;
                case ServerConst.COMMAND_GETDATA:
                    ServerHandle.handleGetData(data, bc);
                    break;
                case ServerConst.COMMAND_BLOCK:
                    ServerHandle.handleBlock(data, bc);
                    break;
                case ServerConst.COMMAND_TX:
                    ServerHandle.handleTx(data, bc);
                    break;
                default:
                    log.info("Unknown command!");
            }


        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
