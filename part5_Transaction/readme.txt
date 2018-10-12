
step1：创建transaction包

step2：创建TXOutput类


step3：创建TXInput类

step4：创建Transaction类

    A：设置字段属性

    B：添加方法，setTxId()

    C：添加方法，newCoinbaseTX()，创建Coinbase交易




step5：修改Block类
    A：修改字段，修改data为transaction。

    B：修改方法newBlock()

    C：修改方法newGenesisBlock()

    D：增加方法，hashTransaction()


step6：修改PoW类
    A：修改方法，run()

    B：修改方法，prepareData()



step7：修改Blockchain类
    A：修改方法newBlockchain()内容后，并将方法名改为createBlockchain()

    B：添加方法mineBlock()


step8：创建SpendableOutputResult类

step9：在Blockchain类中

    A：添加方法，getAllSpentTXOs()，从交易输入中查询区块链中所有已被花费了的交易输出

    B:在Transaction类中，添加isCoinbase()方法

    C：在Blockchain中，添加方法findUnspentTransactions()，查找钱包地址对应的所有未花费的交易

    D：在Blockchain中，添加方法findUTXO(),查找钱包地址对应的所有UTXO

    E：在Blockchain中，添加方法findSpendableOutputs(),寻找能够花费的交易



step10：修改CLI类

    A：修改构造函数

    B：修改help()方法

    C：修改run()方法

    D：修改createBlockchain()方法

    E：添加getBalance()方法

    F：添加send()方法

    G：修改printChain()方法

    H：在Blockchain中，添加initBlockchainFromDB()方法


最后修改sh脚本文件