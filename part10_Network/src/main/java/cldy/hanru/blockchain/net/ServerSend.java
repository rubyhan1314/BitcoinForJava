package cldy.hanru.blockchain.net;

import cldy.hanru.blockchain.block.Block;
import cldy.hanru.blockchain.block.Blockchain;
import cldy.hanru.blockchain.transaction.Transaction;
import cldy.hanru.blockchain.util.ByteUtils;
import cldy.hanru.blockchain.util.CommandUtils;
import cldy.hanru.blockchain.util.SerializeUtils;

import java.util.List;

/**
 * 发送消息
 * @author hanru
 */
public class ServerSend {


    //发送版本
    public static void sendVersion(String toAddress, Blockchain bc) {
        long bestHeight = bc.getBestHeight();
        Version version = new Version(ServerConst.NODE_VERSION, bestHeight, Server.nodeAddress);
        byte[] payload = SerializeUtils.serialize(version);
        System.out.println("sendVersion:"+payload.length);
        //version
        ServerSocketClient.sendData(toAddress, ServerConst.COMMAND_VERSION, payload);
    }

    //COMMAND_GETBLOCKS
    public static void sendGetBlocks(String toAddress) {
        GetBlocks getBlocks = new GetBlocks(Server.nodeAddress);
        byte[] payload = SerializeUtils.serialize(getBlocks);
        System.out.println("sendGetBlocks:"+payload.length);
        ServerSocketClient.sendData(toAddress, ServerConst.COMMAND_GETBLOCKS, payload);

    }

    // 主节点将自己的所有的区块hash发送给钱包节点
    //COMMAND_BLOCK
    public static void sendInv(String toAddress, String kind, List<String> blockHashs) {
        Inv inv = new Inv(Server.nodeAddress, kind, blockHashs);
        byte[] payload = SerializeUtils.serialize(inv);
        System.out.println("sendInv:"+payload.length);
        ServerSocketClient.sendData(toAddress, ServerConst.COMMAND_INV, payload);

    }

    //COMMAND_GETDATA
    public static void sendGetData(String toAddress, String kind, String blockHash) {
        GetData getData = new GetData(Server.nodeAddress, kind, blockHash);

        byte[] payload = SerializeUtils.serialize(getData);
        System.out.println("sendGetData:"+payload.length);
        ServerSocketClient.sendData(toAddress, ServerConst.COMMAND_GETDATA, payload);


    }

    public static void sendBlock(String toAddress, Block block) {
        BlockData blockData = new BlockData(Server.nodeAddress, SerializeUtils.serialize(block));

        byte[] payload = SerializeUtils.serialize(blockData);

        ServerSocketClient.sendData(toAddress, ServerConst.COMMAND_BLOCK, payload);


    }

    public static void sendTx(String toAddress, Transaction tx) {
        TransactionData txData = new TransactionData(Server.nodeAddress, SerializeUtils.serialize(tx));

        byte[] payload = SerializeUtils.serialize(txData);

        ServerSocketClient.sendData(toAddress, ServerConst.COMMAND_TX, payload);


    }


}
