package cldy.hanru.blockchain;


import cldy.hanru.blockchain.block.Block;
import cldy.hanru.blockchain.block.Blockchain;
import cldy.hanru.blockchain.cli.CLI;
import cldy.hanru.blockchain.store.RocksDBUtils;

import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * 测试
 *
 * @author hanru
 */
public class Main {

    public static void main(String[] args) {
//		// 1.创建创世区块
//		Block genesisBlock = Block.newGenesisBlock();
//		System.out.println("创世区块的信息：");
//		System.out.println("\thash:" + genesisBlock.getHash());
//		System.out.println("\tprevBlockHash:" + genesisBlock.getPrevBlockHash());
//		System.out.println("\tdata:" + genesisBlock.getData());
//		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//		String date = sdf.format(new Date(genesisBlock.getTimeStamp()*1000L));
//
//		System.out.println("\ttimeStamp:" + date);
//
//		//2.创建第二个区块
//		Block block2 = Block.newBlock(genesisBlock.getHash(), "I am hanru",1);
//		System.out.println("第二个区块的信息：");
//		System.out.println("\thash:" + block2.getHash());
//		System.out.println("\tprevBlockHash:" + block2.getPrevBlockHash());
//		System.out.println("\tdata:" + block2.getData());
//		String date2 = sdf.format(new Date(block2.getTimeStamp()*1000L));
//		System.out.println("\ttimeStamp:" + date2);


        //3.测试Blockchain

//        Blockchain blockchain = Blockchain.newBlockchain();
//
//
//        System.out.println("创世链的信息：");
//        System.out.println("区块的长度：" + blockchain.getBlockList().size());
//
//        //4.添加区块
//        blockchain.addBlock("Send 1 BTC to 韩茹");
//        blockchain.addBlock("Send 2 more BTC to ruby");
//        blockchain.addBlock("Send 4 more BTC to 王二狗");
//
//        for (int i = 0; i < blockchain.getBlockList().size(); i++) {
//            Block block = blockchain.getBlockList().get(i);
//            System.out.println("第" + block.getHeight() + "个区块信息：");
//            System.out.println("\tprevBlockHash: " + block.getPrevBlockHash());
//            System.out.println("\tData: " + block.getData());
//            System.out.println("\tHash: " + block.getHash());
//            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//            String date2 = sdf.format(new Date(block.getTimeStamp() * 1000L));
//            System.out.println("\ttimeStamp:" + date2);
//
//
//            ProofOfWork pow = ProofOfWork.newProofOfWork(block);
//            System.out.println("是否有效: " + pow.validate() + "\n");
//            System.out.println();
//        }



/*
        // 5.检测pow
        //1.创建一个big对象 0000000.....00001
        BigInteger target = BigInteger.ONE;

        System.out.printf("0x%x\n",target); //0x1

        //2.左移256-bits位
        target = target.shiftLeft((256 - ProofOfWork.TARGET_BITS));

        System.out.printf("0x%x\n",target); //61
        //61位：0x1000000000000000000000000000000000000000000000000000000000000
        //64位：0x0001000000000000000000000000000000000000000000000000000000000000

        //检测hash
        System.out.println();
        String s1="HelloWorld";
        String hash = DigestUtils.sha256Hex(s1);
        System.out.printf("0x%s\n",hash);
*/

/*
        //5.测试持久化
        Blockchain blockchain = Blockchain.newBlockchain();
        System.out.println(blockchain);
//        RocksDBUtils.getInstance().closeDB();


        //6.添加区块
        try {
            blockchain.addBlock("Send 1 BTC to 韩茹");
            blockchain.addBlock("Send 2 more BTC to ruby");
            blockchain.addBlock("Send 4 more BTC to 王二狗");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            RocksDBUtils.getInstance().closeDB();
        }



        //7.遍历区块
//        Blockchain blockchain = Blockchain.newBlockchain();
        Blockchain.BlockchainIterator iterator = blockchain.getBlockchainIterator();
        long index = 0;
        while (iterator.hashNext()) {
            Block block = iterator.next();
            System.out.println("第" + block.getHeight() + "个区块信息：");
            System.out.println("\tprevBlockHash: " + block.getPrevBlockHash());
            System.out.println("\tData: " + block.getData());
            System.out.println("\tHash: " + block.getHash());
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String date = sdf.format(new Date(block.getTimeStamp() * 1000L));
            System.out.println("\ttimeStamp:" + date);
            System.out.println();
        }
        RocksDBUtils.getInstance().closeDB();
*/
        CLI cli = new CLI(args);
        cli.run();
    }

}
