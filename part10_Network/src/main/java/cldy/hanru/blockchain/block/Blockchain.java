package cldy.hanru.blockchain.block;


import cldy.hanru.blockchain.store.RocksDBUtils;
import cldy.hanru.blockchain.transaction.TXInput;
import cldy.hanru.blockchain.transaction.TXOutput;
import cldy.hanru.blockchain.transaction.Transaction;
import cldy.hanru.blockchain.util.ByteUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;

import java.util.*;

/**
 * 区块链
 *
 * @author hanru
 */
@Data
@AllArgsConstructor
public class Blockchain {


    /**
     * 最后一个区块的hash
     */
    private String lastBlockHash;

    /**
     * 当前节点的nodeID
     */
    private String nodeID;

    /**
     * 创建区块链，createBlockchain
     *
     * @param address
     * @return
     */
    public static Blockchain createBlockchain(String address, String nodeID) {

        String lastBlockHash = RocksDBUtils.getInstance(nodeID).getLastBlockHash();
        if (StringUtils.isBlank(lastBlockHash)) {
            //对应的bucket不存在，说明是第一次获取区块链实例
            // 创建 coinBase 交易
            Transaction coinbaseTX = Transaction.newCoinbaseTX(address, "");
            Block genesisBlock = Block.newGenesisBlock(coinbaseTX);
//			Block genesisBlock = Block.newGenesisBlock();
            lastBlockHash = genesisBlock.getHash();
            RocksDBUtils.getInstance(nodeID).putBlock(genesisBlock);
            RocksDBUtils.getInstance(nodeID).putLastBlockHash(lastBlockHash);

        }
        return new Blockchain(lastBlockHash, nodeID);
    }

    /**
     * 根据block，添加区块
     *
     * @param block
     */
    public void addBlock(Block block) {

        RocksDBUtils.getInstance(nodeID).putLastBlockHash(block.getHash());
        RocksDBUtils.getInstance(nodeID).putBlock(block);
        this.lastBlockHash = block.getHash();

    }

    /**
     * 根据data添加区块
     * @param data
     */
//	public void addBlock(String data)  throws Exception{
//
//        String lastBlockHash = RocksDBUtils.getInstance().getLastBlockHash();
//        if (StringUtils.isBlank(lastBlockHash)){
//			throw new Exception("还没有数据库，无法直接添加区块。。");
//		}
//		this.addBlock(Block.newBlock(lastBlockHash,data));
//    }


    /**
     * 区块链迭代器：内部类
     */
    public class BlockchainIterator {

        /**
         * 当前区块的hash
         */
        private String currentBlockHash;

        /**
         * 构造函数
         *
         * @param currentBlockHash
         */
        public BlockchainIterator(String currentBlockHash) {
            this.currentBlockHash = currentBlockHash;
        }

        /**
         * 判断是否有下一个区块
         *
         * @return
         */
        public boolean hashNext() {
            if (ByteUtils.ZERO_HASH.equals(currentBlockHash)) {
                return false;
            }
            Block lastBlock = RocksDBUtils.getInstance(nodeID).getBlock(currentBlockHash);
            if (lastBlock == null) {
                return false;
            }
            // 如果是创世区块
            if (ByteUtils.ZERO_HASH.equals(lastBlock.getPrevBlockHash())) {
                return true;
            }
            return RocksDBUtils.getInstance(nodeID).getBlock(lastBlock.getPrevBlockHash()) != null;
        }


        /**
         * 迭代获取区块
         *
         * @return
         */
        public Block next() {
            Block currentBlock = RocksDBUtils.getInstance(nodeID).getBlock(currentBlockHash);
            if (currentBlock != null) {
                this.currentBlockHash = currentBlock.getPrevBlockHash();
                return currentBlock;
            }
            return null;
        }
    }

    /**
     * 添加方法，用于获取迭代器实例
     *
     * @return
     */
    public BlockchainIterator getBlockchainIterator() {
        return new BlockchainIterator(lastBlockHash);
    }


    /**
     * 打包交易，进行挖矿
     *
     * @param transactions
     */
    public Block mineBlock(List<Transaction> transactions) throws Exception {
        // 挖矿前，先验证交易记录
        for (Transaction tx : transactions) {
            if (!this.verifyTransactions(tx)) {
                throw new Exception("ERROR: Fail to mine block ! Invalid transaction ! ");
            }
        }
        String lastBlockHash = RocksDBUtils.getInstance(nodeID).getLastBlockHash();
        Block lastBlock = RocksDBUtils.getInstance(nodeID).getBlock(lastBlockHash);
        if (lastBlockHash == null) {
            throw new Exception("ERROR: Fail to get last block hash ! ");
        }
        Block block = Block.newBlock(lastBlockHash, transactions, lastBlock.getHeight() + 1);
        this.addBlock(block);
        return block;

    }


    /**
     * 查找钱包地址对应的所有未花费的交易
     *
     * @param pubKeyHash 钱包公钥Hash
     * @return
     */
    /*
    private Transaction[] findUnspentTransactions(byte[] pubKeyHash) throws Exception {
        Map<String, int[]> allSpentTXOs = this.getAllSpentTXOs(pubKeyHash);
        Transaction[] unspentTxs = {};

        // 再次遍历所有区块中的交易输出
        for (BlockchainIterator blockchainIterator = this.getBlockchainIterator(); blockchainIterator.hashNext(); ) {
            Block block = blockchainIterator.next();
            for (Transaction transaction : block.getTransactions()) {

                String txId = Hex.encodeHexString(transaction.getTxId());

                int[] spentOutIndexArray = allSpentTXOs.get(txId);

                for (int outIndex = 0; outIndex < transaction.getOutputs().length; outIndex++) {
                    if (spentOutIndexArray != null && ArrayUtils.contains(spentOutIndexArray, outIndex)) {
                        continue;
                    }

                    // 保存不存在 allSpentTXOs 中的交易
//                    if (transaction.getOutputs()[outIndex].canBeUnlockedWith(address)) {
                    if (transaction.getOutputs()[outIndex].isLockedWithKey(pubKeyHash)) {
                        unspentTxs = ArrayUtils.add(unspentTxs, transaction);
                    }
                }
            }
        }
        return unspentTxs;
    }
    */

    /**
     * 查找钱包地址对应的所有UTXO
     *
     * @param pubKeyHash 钱包公钥Hash
     * @return
     */
    /*
    public TXOutput[] findUTXO(byte[] pubKeyHash) throws Exception {
        Transaction[] unspentTxs = this.findUnspentTransactions(pubKeyHash);
        TXOutput[] utxos = {};
        if (unspentTxs == null || unspentTxs.length == 0) {
            return utxos;
        }
        for (Transaction tx : unspentTxs) {
            for (TXOutput txOutput : tx.getOutputs()) {
//                if (txOutput.canBeUnlockedWith(address)) {
                if (txOutput.isLockedWithKey(pubKeyHash)) {
                    utxos = ArrayUtils.add(utxos, txOutput);
                }
            }
        }
        return utxos;
    }

*/
    /**
     * 寻找能够花费的交易
     *
     * @param pubKeyHash 钱包公钥Hash
     * @param amount  花费金额
     */

    /*
    public SpendableOutputResult findSpendableOutputs(byte[] pubKeyHash, int amount) throws Exception {
        Transaction[] unspentTXs = this.findUnspentTransactions(pubKeyHash);
        int accumulated = 0;
        Map<String, int[]> unspentOuts = new HashMap<>();
        for (Transaction tx : unspentTXs) {

            String txId = Hex.encodeHexString(tx.getTxId());

            for (int outId = 0; outId < tx.getOutputs().length; outId++) {

                TXOutput txOutput = tx.getOutputs()[outId];

//                if (txOutput.canBeUnlockedWith(address) && accumulated < amount) {
                    if (txOutput.isLockedWithKey(pubKeyHash) && accumulated < amount) {
                    accumulated += txOutput.getValue();

                    int[] outIds = unspentOuts.get(txId);
                    if (outIds == null) {
                        outIds = new int[]{outId};
                    } else {
                        outIds = ArrayUtils.add(outIds, outId);
                    }
                    unspentOuts.put(txId, outIds);
                    if (accumulated >= amount) {
                        break;
                    }
                }
            }
        }
        return new SpendableOutputResult(accumulated, unspentOuts);
    }
*/


    //----------------------Sign-------------------------

    /**
     * 依据交易ID查询交易信息
     *
     * @param txId 交易ID
     * @return
     */
    private Transaction findTransaction(byte[] txId) throws Exception {

        for (BlockchainIterator iterator = this.getBlockchainIterator(); iterator.hashNext(); ) {
            Block block = iterator.next();
            for (Transaction tx : block.getTransactions()) {
                if (Arrays.equals(tx.getTxId(), txId)) {
                    return tx;
                }
            }
        }
        throw new Exception("ERROR: Can not found tx by txId ! ");
    }

    /**
     * 从 DB 从恢复区块链数据
     *
     * @return
     * @throws Exception
     */
    public static Blockchain initBlockchainFromDB(String nodeID) throws Exception {
        String lastBlockHash = RocksDBUtils.getInstance(nodeID).getLastBlockHash();
        if (lastBlockHash == null) {
            throw new Exception("ERROR: Fail to init blockchain from db. ");
        }
        return new Blockchain(lastBlockHash, nodeID);
    }


    /**
     * 进行交易签名
     *
     * @param tx         交易数据
     * @param privateKey 私钥
     */
    public void signTransaction(Transaction tx, BCECPrivateKey privateKey) throws Exception {
        // 先来找到这笔新的交易中，交易输入所引用的前面的多笔交易的数据
        Map<String, Transaction> prevTxMap = new HashMap<>();
        for (TXInput txInput : tx.getInputs()) {
            Transaction prevTx = this.findTransaction(txInput.getTxId());
            prevTxMap.put(Hex.encodeHexString(txInput.getTxId()), prevTx);
        }
        tx.sign(privateKey, prevTxMap);
    }

    /**
     * 交易签名验证
     *
     * @param tx
     */
    public boolean verifyTransactions(Transaction tx) throws Exception {
        if (tx.isCoinbase()) {
            return true;
        }

        Map<String, Transaction> prevTx = new HashMap<>();
        for (TXInput txInput : tx.getInputs()) {
            Transaction transaction = this.findTransaction(txInput.getTxId());
            prevTx.put(Hex.encodeHexString(txInput.getTxId()), transaction);
        }
        try {
            return tx.verify(prevTx);
        } catch (Exception e) {
            throw new Exception("Fail to verify transaction ! transaction invalid ! ");
        }
    }


    //------------------------UTXOSet--------------------------


    /**
     * 从交易输入中查询区块链中所有已被花费了的交易输出
     *
     * @return 交易ID以及对应的交易输出下标地址
     */

    private Map<String, int[]> getAllSpentTXOs() {
        // 定义TxId ——> spentOutIndex[]，存储交易ID与已被花费的交易输出数组索引值
        Map<String, int[]> spentTXOs = new HashMap<>();
        for (BlockchainIterator blockchainIterator = this.getBlockchainIterator(); blockchainIterator.hashNext(); ) {
            Block block = blockchainIterator.next();

            for (Transaction transaction : block.getTransactions()) {
                // 如果是 coinbase 交易，直接跳过，因为它不存在引用前一个区块的交易输出
                if (transaction.isCoinbase()) {
                    continue;
                }
                for (TXInput txInput : transaction.getInputs()) {
//					if (txInput.canUnlockOutputWith(address)) {
//                    if (txInput.usesKey(pubKeyHash)) {
                    String inTxId = Hex.encodeHexString(txInput.getTxId());
                    int[] spentOutIndexArray = spentTXOs.get(inTxId);
                    if (spentOutIndexArray == null) {
                        spentTXOs.put(inTxId, new int[]{txInput.getTxOutputIndex()});
                    } else {
                        spentOutIndexArray = ArrayUtils.add(spentOutIndexArray, txInput.getTxOutputIndex());
                    }
                    spentTXOs.put(inTxId, spentOutIndexArray);
                }
//                }
            }
        }
        return spentTXOs;
    }


    /**
     * 查找所有的 unspent transaction outputs
     *
     * @return
     */
    public Map<String, TXOutput[]> findAllUTXOs() {
        Map<String, int[]> allSpentTXOs = this.getAllSpentTXOs();
        Map<String, TXOutput[]> allUTXOs = new HashMap<>();
        // 再次遍历所有区块中的交易输出
        for (BlockchainIterator blockchainIterator = this.getBlockchainIterator(); blockchainIterator.hashNext(); ) {
            Block block = blockchainIterator.next();
            for (Transaction transaction : block.getTransactions()) {

                String txId = Hex.encodeHexString(transaction.getTxId());

                int[] spentOutIndexArray = allSpentTXOs.get(txId);
                TXOutput[] txOutputs = transaction.getOutputs();
                for (int outIndex = 0; outIndex < txOutputs.length; outIndex++) {
                    if (spentOutIndexArray != null && ArrayUtils.contains(spentOutIndexArray, outIndex)) {
                        continue;
                    }
                    TXOutput[] UTXOArray = allUTXOs.get(txId);
                    if (UTXOArray == null) {
                        UTXOArray = new TXOutput[]{txOutputs[outIndex]};
                    } else {
                        UTXOArray = ArrayUtils.add(UTXOArray, txOutputs[outIndex]);
                    }
                    allUTXOs.put(txId, UTXOArray);
                }
            }
        }
        return allUTXOs;
    }

    //----------
    //获取最新区块的高度
    public long getBestHeight() {

        Block block = this.getBlockchainIterator().next();

        return block.getHeight();
    }


    //获取所有区块的hash
    public List<String> getBlockHashes() {

        BlockchainIterator it = this.getBlockchainIterator();

        List<String> blockHashs = new ArrayList<>();

        while (it.hashNext()) {
            Block block = it.next();
            blockHashs.add(0, block.getHash());
        }

        return blockHashs;
    }

    //根据hash获取区块
    public Block getBlock(String blockHash) {
        Block block = RocksDBUtils.getInstance(this.nodeID).getBlock(blockHash);
        return block;

    }

    //节点传来的区块，保存到数据库中
    public void saveBlock(Block newBlock) {
        BlockchainIterator it = this.getBlockchainIterator();
        while (it.hashNext()) {
            Block block = it.next();
            if (block.getHash().equals(newBlock.getHash())) {
                System.out.println("区块已经存在，无需存储。。。");
                return;
            }
        }

        //存储
        RocksDBUtils.getInstance(nodeID).putBlock(newBlock);

        Block lastBlock = RocksDBUtils.getInstance(nodeID).getLastBlock();
        if (lastBlock.getHeight() < newBlock.getHeight()) {
            RocksDBUtils.getInstance(nodeID).putLastBlockHash(newBlock.getHash());
            this.lastBlockHash = newBlock.getHash();
        }
        System.out.printf("Added block %s\n", newBlock.getHash());
    }
}

