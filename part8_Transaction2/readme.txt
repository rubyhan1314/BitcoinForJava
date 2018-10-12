

step1：修改pom.xml
    <!-- 引入日志框架 slf4j 和 logback  -->


step2：修改RocksDBUtils类

    A：添加String CHAINSTATE_BUCKET_KEY = "chainstate";

    B：添加private Map<String, byte[]> chainstateBucket;

    C：添加initChainStateBucket()

    D：添加cleanChainStateBucket()

    E：添加putUTXOs()

    F：添加getUTXOs()

    G：添加deleteUTXOs()

    H：修改RocksDBUtils()构造函数


 step3：修改Blockchain类
    A：修改getAllSpentTXOs()

    B:添加findAllUTXOs()

    C:删除
        findUnspentTransactions()
        findUTXO()
        findSpendableOutputs()


 step4：新建UTXOSet.java文件
    A：添加reIndex()
 step5：修改CLI文件
    A：修改createBlockchain()


 step6：修改UTXOSet文件
    A：添加findUTXOs(),用于查找指定账户的所有的utxo
 step7：修改CLI文件
    A：修改getBalance()


 step8：在UTXOSet中，继续修改，实现转账
    A：添加findSpendableOutputs()
    B: 添加update()


step9：修改Transaction类
    A：修改newUTXOTransaction()
 step10：修改Blockchain
    A：修改mineBlock(),添加返回值

  step11:修改CLI文件
    A：修改send()



 添加奖励：
 step12：在Transaction中
    A:添加时间戳字段
    B:修改：newCoinbaseTX()
    C：修改：newUTXOTransaction()

 step13：在CLI中
    A：修改send()方法，添加给与奖励的coinbase交易

 step14：修改Blockchain中
    A：修改verifyTransactions()方法，添加判断是否是coinbase交易