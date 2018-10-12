
step1：在uitl包下创建SerializeUtils.java工具类
    serialize()
    deserialize()

step2：修改pow.xml文件，添加序列化的依赖包
    kryo

step3新建包store，用于持久化


step4：修改pow.xml文件，添加rocksdb的依赖包
    rocksdbjni


step5：创建RocksDBUtils工具类
    A：创建RocksDBUtils类
    B：添加静态常量：
        文件名： DB_FILE = "blockchain.db";
        区块桶：BLOCKS_BUCKET_KEY = "blocks";
        最后一个区块hash：LAST_BLOCK_KEY = "l";

    C：单例获取该类实例
    D：添加两个字段属性
        private RocksDB db;
        private Map<String, byte[]> blocksBucket;

    E：添加方法openDB()
    F：initBlockBucket()

step6：修改pow.xml，添加google的包
    需要全局翻墙

step7：继续添加方法
    G：putBlock(),存储区块
    H：getBlock(),查询区块
    I：putLastBlockHash(),
    J：getLastBlockHash(),
    K：closeDB()



step8：修改blockchain类，
    A：删除lst集合，添加lastBlockHash字段

    B：修改newBlockchain()

    C：修改两个addBlock()方法


step9：遍历区块
