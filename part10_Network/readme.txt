step1：在CLI中设置NODE_ID

step2：根据node_id创建，数据库文件，钱包文件等

          先修改数据库的名字：blockchain_%s.db
          A：修改CLI中，
                1.private void createBlockchain(String address,String nodeID)

          B：修改Blockchain中，
                1.添加字段private String nodeID;
                2.修改：public static Blockchain createBlockchain(String address,String nodeID)
                3.修改：public static Blockchain initBlockchainFromDB(String nodeID)

          C：修改RocksDBUtils中，
                1.getInstance(nodeID)；
                2.RocksDBUtils(nodeID);构造函数
                3.private void openDB(String nodeID)
                        添加：DB_FILE = String.format(DB_FILE,nodeID);

          D：修改CLI中
                1.修改打印区块信息：
                        private void printChain(String nodeID)
                2.修改查询余额：
                        private void getBalance(String address,String nodeID)



          修改钱包文件名称：WALLET_FILE = "wallet_%s.dat";

          E：修改CLI中
                1. private void createWallet(String nodeID)
                2.private void printAddresses()


          F：修改WalletUtils中
                1.public static WalletUtils getInstance(String nodeID)
                2.构造函数：WalletUtils(nodeID)
                3.private void initWalletFile(String nodeID)




           G:修改CLI中，转账
                1. private void send(String from, String to, int amount,String nodeID)


           H：修改Transaction中
                1. newUTXOTransaction()

            I：在UTXOSet中

 此时项目中的数据库的创建和钱包地址的创建，文件名称都会带有端口号
-----------------------
step3:在CLI中，添加一个终端命令，表示启动服务器
    并添加方法：private void startServer(String nodeID, String minerAdd)  {
    }

 step4：添加Version类

 step5：在uil包下，添加CommandUtils工具类


 step6：新建net包
 step7：新建Server类

 step8：新建ServerSend类，添加sendVersion方法

 step9：新建ServerSocketClient类，添加sendData()方法


step10：新建GetBlocks类

step11：在ServerSend类中，添加sendGetBlocks()方法

step12：在ServerHandle中，添加handleGetblocks()方法

step13：在Blockchain类中添加getBlockHashes()方法


step14：新建Inv类

step15：在ServerSend类中，添加sendInv()方法

step16：在ServerHandle中，添加handleInv()方法


step17：新建GetData类

step18：在ServerSend类中，添加sendGetData()方法

step19：在ServerHandle中，添加handleGetData()方法



step20：新建BlockData类

step21：在ServerSend类中，添加sendBlock()方法

step22：在ServerHandle中，添加handleBlock()方法


step23：新建TransactionData类

step24：在ServerSend类中，添加sendTx()方法

step25：在ServerHandle中，添加handleTx()方法




