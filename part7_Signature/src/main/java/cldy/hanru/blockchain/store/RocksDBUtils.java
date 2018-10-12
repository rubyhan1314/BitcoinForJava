package cldy.hanru.blockchain.store;


import cldy.hanru.blockchain.block.Block;
import cldy.hanru.blockchain.util.SerializeUtils;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

import java.util.HashMap;
import java.util.Map;

/**
 * 数据库存储的工具类
 * @author hanru
 */
public class RocksDBUtils {
    /**
     * 区块链数据文件
     */
    private static final String DB_FILE = "blockchain.db";

    /**
     * 区块桶前缀
     */
    private static final String BLOCKS_BUCKET_KEY = "blocks";

    /**
     * 最新一个区块的hash
     */
    private static final String LAST_BLOCK_KEY = "l";



    private volatile static RocksDBUtils instance;


    /**
     * 获取RocksDBUtils的单例
     * @return
     */
    public static RocksDBUtils getInstance() {
        if (instance == null) {
            synchronized (RocksDBUtils.class) {
                if (instance == null) {
                    instance = new RocksDBUtils();
                }
            }
        }
        return instance;
    }


    private RocksDBUtils() {
        openDB();
        initBlockBucket();
    }


    private RocksDB db;

    /**
     * block buckets
     */
    private Map<String, byte[]> blocksBucket;


    /**
     * 打开数据库
     */
    private void openDB() {
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

}
