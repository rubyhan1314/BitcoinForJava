package cldy.hanru.blockchain.store;


import cldy.hanru.blockchain.block.Block;
import cldy.hanru.blockchain.transaction.TXOutput;
import cldy.hanru.blockchain.util.SerializeUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

import java.util.HashMap;
import java.util.Map;

/**
 * 数据库存储的工具类
 * @author hanru
 */
@Slf4j
public class RocksDBUtils {
    /**
     * 区块链数据文件
     */
    private static  String DB_FILE = "blockchain_%s.db";

    /**
     * 区块桶前缀
     */
    private static final String BLOCKS_BUCKET_KEY = "blocks";


    /**
     * 链状态桶Key
     */
    private static final String CHAINSTATE_BUCKET_KEY = "chainstate";

    /**
     * 最新一个区块的hash
     */
    private static final String LAST_BLOCK_KEY = "l";



    private volatile static RocksDBUtils instance;


    /**
     * 获取RocksDBUtils的单例
     * @return
     */
    public static RocksDBUtils getInstance(String nodeID) {
        if (instance == null) {
            synchronized (RocksDBUtils.class) {
                if (instance == null) {
                    instance = new RocksDBUtils(nodeID);
                }
            }
        }
        return instance;
    }


    private RocksDBUtils(String nodeID) {
        openDB(nodeID);
        initBlockBucket();
        initChainStateBucket();
    }


    private RocksDB db;

    /**
     * block buckets
     */
    private Map<String, byte[]> blocksBucket;


    /**
     * 打开数据库
     */
    private void openDB(String nodeID) {
        DB_FILE = String.format(DB_FILE,nodeID);
        try {
            db = RocksDB.open(DB_FILE);
        } catch (RocksDBException e) {
            throw new RuntimeException("打开数据库失败。。 ! ", e);
        }
    }
    /**
     * 初始化 blocks 数据桶
     */
    private void initBlockBucket() {
        try {
            //
            byte[] blockBucketKey = SerializeUtils.serialize(BLOCKS_BUCKET_KEY);
            byte[] blockBucketBytes = db.get(blockBucketKey);
            if (blockBucketBytes != null) {
                blocksBucket = (Map) SerializeUtils.deserialize(blockBucketBytes);
            } else {
                blocksBucket = new HashMap<>();
                db.put(blockBucketKey, SerializeUtils.serialize(blocksBucket));
            }
        } catch (RocksDBException e) {
            throw new RuntimeException("初始化block的bucket失败。。! ", e);
        }
    }

    /**
     * 保存区块
     *
     * @param block
     */
    public void putBlock(Block block) {
        try {
            blocksBucket.put(block.getHash(), SerializeUtils.serialize(block));
            db.put(SerializeUtils.serialize(BLOCKS_BUCKET_KEY), SerializeUtils.serialize(blocksBucket));
        } catch (RocksDBException e) {
            throw new RuntimeException("存储区块失败。。  ", e);
        }
    }

    /**
     * 查询区块
     *
     * @param blockHash
     * @return
     */
    public Block getBlock(String blockHash) {
        return (Block) SerializeUtils.deserialize(blocksBucket.get(blockHash));
    }

    /**
     * 保存最新一个区块的Hash值
     *
     * @param tipBlockHash
     */
    public void putLastBlockHash(String tipBlockHash) {
        try {
            blocksBucket.put(LAST_BLOCK_KEY, SerializeUtils.serialize(tipBlockHash));
            db.put(SerializeUtils.serialize(BLOCKS_BUCKET_KEY), SerializeUtils.serialize(blocksBucket));
        } catch (RocksDBException e) {
            throw new RuntimeException("数据库存储最新区块hash失败。。 ", e);
        }
    }

    /**
     * 查询最新一个区块的Hash值
     *
     * @return
     */
    public String getLastBlockHash() {
        byte[] lastBlockHashBytes = blocksBucket.get(LAST_BLOCK_KEY);
        if (lastBlockHashBytes != null) {
            return (String) SerializeUtils.deserialize(lastBlockHashBytes);
        }
        return "";
    }

    /**
     * 关闭数据库
     */
    public void closeDB() {
        try {
            db.close();
        } catch (Exception e) {
            throw new RuntimeException("关闭数据库失败。。 ", e);
        }
    }



    //-------------------UTXOSet---------------------


    /**
     * chainstate buckets
     */
    @Getter
    private Map<String, byte[]> chainstateBucket;

    /**
     * 初始化 blocks 数据桶
     */
    private void initChainStateBucket() {
        try {
            byte[] chainstateBucketKey = SerializeUtils.serialize(CHAINSTATE_BUCKET_KEY);
            byte[] chainstateBucketBytes = db.get(chainstateBucketKey);
            if (chainstateBucketBytes != null) {
                chainstateBucket = (Map) SerializeUtils.deserialize(chainstateBucketBytes);
            } else {
                chainstateBucket = new HashMap<>();
                db.put(chainstateBucketKey, SerializeUtils.serialize(chainstateBucket));
            }
        } catch (RocksDBException e) {
            log.error("Fail to init chainstate bucket ! ", e);
            throw new RuntimeException("Fail to init chainstate bucket ! ", e);
        }
    }

    /**
     * 清空chainstate bucket
     */
    public void cleanChainStateBucket() {
        try {
            chainstateBucket.clear();
        } catch (Exception e) {
            log.error("Fail to clear chainstate bucket ! ", e);
            throw new RuntimeException("Fail to clear chainstate bucket ! ", e);
        }
    }

    /**
     * 保存UTXO数据
     *
     * @param key   交易ID
     * @param utxos UTXOs
     */
    public void putUTXOs(String key, TXOutput[] utxos) {
        try {
            chainstateBucket.put(key, SerializeUtils.serialize(utxos));
            db.put(SerializeUtils.serialize(CHAINSTATE_BUCKET_KEY), SerializeUtils.serialize(chainstateBucket));
        } catch (Exception e) {
            log.error("Fail to put UTXOs into chainstate bucket ! key=" + key, e);
            throw new RuntimeException("Fail to put UTXOs into chainstate bucket ! key=" + key, e);
        }
    }

    /**
     * 查询UTXO数据
     *
     * @param key 交易ID
     */
    public TXOutput[] getUTXOs(String key) {
        byte[] utxosByte = chainstateBucket.get(key);
        if (utxosByte != null) {
            return (TXOutput[]) SerializeUtils.deserialize(utxosByte);
        }
        return null;
    }

    /**
     * 删除 UTXO 数据
     *
     * @param key 交易ID
     */
    public void deleteUTXOs(String key) {
        try {
            chainstateBucket.remove(key);
            db.put(SerializeUtils.serialize(CHAINSTATE_BUCKET_KEY), SerializeUtils.serialize(chainstateBucket));
        } catch (Exception e) {
            log.error("Fail to delete UTXOs by key ! key=" + key, e);
            throw new RuntimeException("Fail to delete UTXOs by key ! key=" + key, e);
        }
    }

    //---------
    /**
     * 获取最新一个区块
     *
     * @return
     */
    public Block getLastBlock() {
        String lastBlockHash = getLastBlockHash();
        if (StringUtils.isBlank(lastBlockHash)) {
            throw new RuntimeException("ERROR: Fail to get last block hash ! ");
        }
        Block lastBlock = getBlock(lastBlockHash);
        if (lastBlock == null) {
            throw new RuntimeException("ERROR: Fail to get last block ! ");
        }
        return lastBlock;
    }

}

