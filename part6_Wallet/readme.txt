step：修改pom.xml文件
    添加加密的依赖包：org.bouncycastle

step1：创建包wallet

step2：创建wallet类

    设置字段属性

    添加方法：initWallet()

    添加方法：newECKeyPair()

    添加方法：初始化构造函数Wallet()


step3：在util包下，新建BtcAddressUtils类
    添加生成钱包的公钥方法


step4：在util包下，新建Base58Check类

step5：在wallet下，添加WalletUtils类





step6：修改CLI
     A:修改help()
     B:修改run()
     C:修改printChain()
    D:修改getBalance()
    E:修改send()
    F:添加方法createWallet()
    G:printAddresses()


 最后修改blockchain.sh脚本文件