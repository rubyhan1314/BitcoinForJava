package cldy.hanru.blockchain.net;

import cldy.hanru.blockchain.block.Block;
import cldy.hanru.blockchain.block.Blockchain;
import cldy.hanru.blockchain.store.RocksDBUtils;
import cldy.hanru.blockchain.transaction.Transaction;
import cldy.hanru.blockchain.transaction.UTXOSet;
import cldy.hanru.blockchain.util.ByteUtils;
import cldy.hanru.blockchain.util.SerializeUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
/**
 * 处理消息的具体方法
 * @author hanru
 */
public class ServerHandle {

    // 存储hash值
    public static List<String> hashList = new ArrayList<>();

    public static Map<String, Transaction> memoryTxPool = new HashMap<>();


    public static void handleVersion(byte[] data, Blockchain bc) {

        // 反序列化
        Version version = (Version) SerializeUtils.deserialize(data);


        long bestHeight = bc.getBestHeight();          //2 1
        long foreignerBestHeight = version.getBestHeight(); // 1 2
        System.out.println("自己的版本：" + bestHeight + ",对方的版本：" + foreignerBestHeight);

        if (bestHeight > foreignerBestHeight) {
            ServerSend.sendVersion(version.getNodeAddress(), bc);
        } else if (bestHeight < foreignerBestHeight) {
            // 去向主节点要信息
            ServerSend.sendGetBlocks(version.getNodeAddress());
        }

        if (!Server.nodeIsKnown(version.getNodeAddress())) {
            Server.knowNodes.add(version.getNodeAddress());
        }
    }

    public static void handleGetblocks(byte[] data, Blockchain bc) {

        GetBlocks getBlocks = null;
        // 反序列化
        try {
             getBlocks = (GetBlocks) SerializeUtils.deserialize(data);
        }catch (ClassCastException e){
            System.out.println("handleGetblocks:"+e);
        }

        System.out.println("handleGetblocks:"+getBlocks);
        //获取当前节点的区块的hash

        List<String> blockHashs = bc.getBlockHashes();


        //txHash blockHash
        ServerSend.sendInv(getBlocks.getAddrFrom(), ServerConst.BLOCK_TYPE, blockHashs);

    }

    public static void handleInv(byte[] data, Blockchain bc) {
        //反序列化
        Inv inv = (Inv) SerializeUtils.deserialize(data);

        if (ServerConst.BLOCK_TYPE.equals(inv.getType())) {
            String blockHash = inv.getItems().get(0);
            ServerSend.sendGetData(inv.getAddrFrom(), ServerConst.BLOCK_TYPE, blockHash);

            if (inv.getItems().size() >= 1) {
                hashList = inv.getItems().subList(1, inv.getItems().size());
            }

        } else if (ServerConst.TX_TYPE.equals(inv.getType())) {
            String txHash = inv.getItems().get(0);
            if (!memoryTxPool.containsKey(txHash)) {
                ServerSend.sendGetData(inv.getAddrFrom(), ServerConst.TX_TYPE, txHash);
            }
        }
    }

    //
    public static void handleGetData(byte[] data, Blockchain bc) {
        //反序列化
        GetData getData = (GetData) SerializeUtils.deserialize(data);
        //如果是区块
        if (ServerConst.BLOCK_TYPE.equals(getData.getType())) {
            Block block = bc.getBlock(getData.getHash());
            ServerSend.sendBlock(getData.getAddrFrom(), block);

        } else if (ServerConst.TX_TYPE.equals(getData.getType())) {
            //如果是笔交易
            Transaction tx = memoryTxPool.get(getData.getHash());
            ServerSend.sendTx(getData.getAddrFrom(), tx);
        }

    }


    public static void handleBlock(byte[] data, Blockchain bc) {
        //反序列化
        BlockData blockData = (BlockData) SerializeUtils.deserialize(data);

        Block block = (Block) SerializeUtils.deserialize(blockData.getBlockData());

        System.out.println("Recevied a new block!");

        System.out.println("handleBlock:"+bc.getNodeID());
//        bc.addBlock(block);
        bc.saveBlock(block);

        UTXOSet utxoSet = new UTXOSet(bc);
//        utxoSet.reIndex();
        utxoSet.update(block);


        if (hashList.size() > 0) {
            String blockHash = hashList.get(0);
            ServerSend.sendGetData(blockData.getAddrFrom(), ServerConst.BLOCK_TYPE, blockHash);
            hashList = hashList.subList(1, hashList.size());
        }

    }

    //
    public static void handleTx(byte[] data, Blockchain bc) {
        //反序列化
        TransactionData txData = (TransactionData) SerializeUtils.deserialize(data);

        Transaction tx = (Transaction) SerializeUtils.deserialize(txData.getTxData());

        System.out.println("Recevied a new transaction!");

        String txIdStr = ByteUtils.bytesToHexString(tx.getTxId());

        memoryTxPool.put(txIdStr, tx);

        // 说明主节点自己
        if (Server.knowNodes.get(0).equals(Server.nodeAddress)) {
            // 给矿工节点发送交易hash
            List<String> txList = new ArrayList<>();
            txList.add(ByteUtils.bytesToHexString(tx.getTxId()));
            for (String toAddr : Server.knowNodes) {
                ServerSend.sendInv(toAddr, ServerConst.TX_TYPE, txList);
            }
        }

        // 矿工进行挖矿验证
        if (memoryTxPool.size() >= 1 && !"".equals(Server.minerAddress)) {
            UTXOSet utxoSet = new UTXOSet(bc);

            //验证数字签名。。
            try {
                if (!bc.verifyTransactions(tx)) {
                    log.info("ERROR: Invalid transaction..");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            List<Transaction> txs = new ArrayList<>();
            txs.add(tx);
            //奖励
            Transaction coinbaseTx = Transaction.newCoinbaseTX(Server.minerAddress, "");
            txs.add(coinbaseTx);
            //挖掘新区块
            Block lastBlock = RocksDBUtils.getInstance(bc.getNodeID()).getLastBlock();

            Block newBlock = Block.newBlock(lastBlock.getHash(), txs, lastBlock.getHeight() + 1);


            RocksDBUtils.getInstance(bc.getNodeID()).putBlock(newBlock);
            RocksDBUtils.getInstance(bc.getNodeID()).putLastBlockHash(newBlock.getHash());

            utxoSet.update(newBlock);

            ServerSend.sendBlock(Server.knowNodes.get(0), newBlock);

            //从内存池中删除
            memoryTxPool.remove(txIdStr);

        }
    }

}
