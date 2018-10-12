package cldy.hanru.blockchain.cli;

import cldy.hanru.blockchain.block.Block;
import cldy.hanru.blockchain.block.Blockchain;
import cldy.hanru.blockchain.net.Server;
import cldy.hanru.blockchain.net.ServerSend;
import cldy.hanru.blockchain.pow.ProofOfWork;
import cldy.hanru.blockchain.store.RocksDBUtils;
import cldy.hanru.blockchain.transaction.TXInput;
import cldy.hanru.blockchain.transaction.TXOutput;
import cldy.hanru.blockchain.transaction.Transaction;
import cldy.hanru.blockchain.transaction.UTXOSet;
import cldy.hanru.blockchain.util.Base58Check;
import cldy.hanru.blockchain.wallet.Wallet;
import cldy.hanru.blockchain.wallet.WalletUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.*;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author hanru
 */
@Slf4j
public class CLI {
    private String[] args;
    private Options options = new Options();


    public CLI(String[] args) {
        this.args = args;

        Option helpCmd = Option.builder("h").desc("show help").build();
        options.addOption(helpCmd);

        Option address = Option.builder("address").hasArg(true).desc("Source wallet address").build();
        Option sendFrom = Option.builder("from").hasArg(true).desc("Source wallet address").build();
        Option sendTo = Option.builder("to").hasArg(true).desc("Destination wallet address").build();
        Option sendAmount = Option.builder("amount").hasArg(true).desc("Amount to send").build();
        Option mine = Option.builder("mine").hasArg(true).desc("mine a block").build();

        options.addOption(address);
        options.addOption(sendFrom);
        options.addOption(sendTo);
        options.addOption(sendAmount);
        options.addOption(mine);
    }

    /**
     * 打印帮助信息
     */
    private void help() {

        System.out.println("Usage:");
        System.out.println("  createwallet - Generates a new key-pair and saves it into the wallet file");
        System.out.println("  printaddresses - print all wallet address");
        System.out.println("  getbalance -address ADDRESS - Get balance of ADDRESS");
        System.out.println("  createblockchain -address ADDRESS - Create a blockchain and send genesis block reward to ADDRESS");
        System.out.println("  printchain - Print all the blocks of the blockchain");
        System.out.println("  send -from FROM -to TO -amount AMOUNT -mine MINENOW - Send AMOUNT of coins from FROM address to TO");
        System.out.println("  startnode -address ADDRESS -- 启动节点服务器，并且指定挖矿奖励的地址.");
        System.exit(0);
    }

    /**
     * 验证入参
     *
     * @param args
     */
    private void validateArgs(String[] args) {
        if (args == null || args.length < 1) {
            help();
        }
    }

    /**
     * 命令行解析入口
     */
    public void run() {
        this.validateArgs(args);

        /*
	获取节点ID
	解释：返回当前进程的环境变量varname的值,若变量没有定义时返回nil
	export NODE_ID=8888

	每次打开一个终端，都需要设置NODE_ID的值。
	变量名NODE_ID，可以更改别的。
	 */


        Map<String, String> map = System.getenv();

        String nodeID = map.get("NODE_ID");
        if (nodeID == "") {
            System.out.println("NODE_ID 环境变量没有设置。。");
            System.exit(0);
        }


        System.out.println("NODE_ID：" + nodeID);



        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = null;
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        switch (args[0]) {
            case "createblockchain":
                String createblockchainAddress = cmd.getOptionValue("address");
                if (StringUtils.isBlank(createblockchainAddress)) {
                    help();
                }
                this.createBlockchain(createblockchainAddress, nodeID);
                break;
            case "getbalance":
                String getBalanceAddress = cmd.getOptionValue("address");
                if (StringUtils.isBlank(getBalanceAddress)) {
                    help();
                }
                try {
                    this.getBalance(getBalanceAddress, nodeID);
                } catch (Exception e) {
                    e.printStackTrace();
                }finally {
                    RocksDBUtils.getInstance(nodeID).closeDB();
                }
                break;
            case "send":
                String sendFrom = cmd.getOptionValue("from");
                String sendTo = cmd.getOptionValue("to");
                String sendAmount = cmd.getOptionValue("amount");
                String mineNow = cmd.getOptionValue("mine"); //是否立即挖矿
                if (StringUtils.isBlank(sendFrom) ||
                        StringUtils.isBlank(sendTo) ||
                        !NumberUtils.isDigits(sendAmount)) {
                    help();
                }
                try {
                    this.send(sendFrom, sendTo, Integer.valueOf(sendAmount), nodeID, Boolean.parseBoolean(mineNow));
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    RocksDBUtils.getInstance(nodeID).closeDB();
                }
                break;
            case "printchain":
                this.printChain(nodeID);
                break;
            case "h":
                this.help();
                break;

            case "createwallet":
                try {
                    this.createWallet(nodeID);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case "printaddresses":
                try {
                    this.printAddresses(nodeID);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case "startnode":
                String minerAddress = cmd.getOptionValue("address");
                try {
                    start(nodeID, minerAddress);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            default:
                this.help();
        }

    }



    /**
     * 创建区块链
     *
     * @param address
     */
    private void createBlockchain(String address, String nodeID) {

        Blockchain blockchain = Blockchain.createBlockchain(address,nodeID);
        UTXOSet utxoSet = new UTXOSet(blockchain);
        utxoSet.reIndex();
        log.info("Done ! ");
    }



    /**
     * 打印出区块链中的所有区块
     */
    private void printChain( String nodeID) {
//            Blockchain blockchain = Blockchain.newBlockchain();
        Blockchain blockchain = null;
        try {
            blockchain = Blockchain.initBlockchainFromDB(nodeID);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Blockchain.BlockchainIterator iterator = blockchain.getBlockchainIterator();
        long index = 0;
        while (iterator.hashNext()) {
            Block block = iterator.next();
            System.out.println("第" + block.getHeight() + "个区块信息：");

            if (block != null) {
                boolean validate = ProofOfWork.newProofOfWork(block).validate();
                System.out.println("validate = " + validate);
                System.out.println("\tprevBlockHash: " + block.getPrevBlockHash());
//                    System.out.println("\tData: " + block.getData());
                System.out.println("\tTransaction: ");
                for (Transaction tx : block.getTransactions()) {
                    System.out.printf("\t\t交易ID：%s\n", Hex.encodeHexString(tx.getTxId()));
                    System.out.println("\t\t输入：");
                    for (TXInput in : tx.getInputs()) {
                        System.out.printf("\t\t\tTxID:%s\n", Hex.encodeHexString(in.getTxId()));
                        System.out.printf("\t\t\tOutputIndex:%d\n", in.getTxOutputIndex());
//                        System.out.printf("\t\t\tScriptSiq:%s\n" , in.getScriptSig());
                        System.out.printf("\t\t\tPubKey:%s\n", Hex.encodeHexString(in.getPubKey()));
                    }
                    System.out.println("\t\t输出：");
                    for (TXOutput out : tx.getOutputs()) {
                        System.out.printf("\t\t\tvalue:%d\n", out.getValue());
//                        System.out.printf("\t\t\tScriptPubKey:%s\n" , out.getScriptPubKey());
                        System.out.printf("\t\t\tPubKeyHash:%s\n", Hex.encodeHexString(out.getPubKeyHash()));
                    }


                }


                System.out.println("\tHash: " + block.getHash());
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String date = sdf.format(new Date(block.getTimeStamp() * 1000L));
                System.out.println("\ttimeStamp:" + date);
                System.out.println();
            }
        }
    }

    /**
     * 查询钱包余额
     *
     * @param address 钱包地址
     */
    private void getBalance(String address, String nodeID) throws Exception {

        // 检查钱包地址是否合法
        try {
            Base58Check.base58ToBytes(address);
        } catch (Exception e) {
            throw new Exception("ERROR: invalid wallet address");
        }
        Blockchain blockchain = Blockchain.createBlockchain(address,nodeID);
        // 得到公钥Hash值
        byte[] versionedPayload = Base58Check.base58ToBytes(address);
        byte[] pubKeyHash = Arrays.copyOfRange(versionedPayload, 1, versionedPayload.length);

//        TXOutput[] txOutputs = blockchain.findUTXO(address);
//        TXOutput[] txOutputs = blockchain.findUTXO(pubKeyHash);
        UTXOSet utxoSet = new UTXOSet(blockchain);
        TXOutput[] txOutputs = utxoSet.findUTXOs(pubKeyHash);
        int balance = 0;
        if (txOutputs != null && txOutputs.length > 0) {
            for (TXOutput txOutput : txOutputs) {
                balance += txOutput.getValue();
            }
        }
        System.out.printf("Balance of '%s': %d\n", address, balance);
    }

    /**
     * 转账
     *
     * @param from
     * @param to
     * @param amount
     */
    private void send(String from, String to, int amount,String nodeID, boolean mineNow) throws Exception {
        // 检查钱包地址是否合法
        try {
            Base58Check.base58ToBytes(from);
        } catch (Exception e) {
            throw new Exception("ERROR: sender address invalid ! address=" + from);
        }
        // 检查钱包地址是否合法
        try {
            Base58Check.base58ToBytes(to);
        } catch (Exception e) {
            throw new Exception("ERROR: receiver address invalid ! address=" + to);
        }
        if (amount < 1) {
            throw new Exception("ERROR: amount invalid ! ");
        }


        /*
        Blockchain blockchain = Blockchain.createBlockchain(from);
        Transaction transaction = Transaction.newUTXOTransaction(from, to, amount, blockchain);


        blockchain.mineBlock(new Transaction[]{transaction});
        RocksDBUtils.getInstance().closeDB();
        System.out.println("Success!");
        */

        Blockchain blockchain = Blockchain.createBlockchain(from,nodeID);
        // 新交易
        Transaction transaction = Transaction.newUTXOTransaction(from, to, amount, blockchain, nodeID);
        if(mineNow){
            // 奖励
            Transaction rewardTx = Transaction.newCoinbaseTX(from, "");
            List<Transaction> transactions = new ArrayList<>();
            transactions.add(transaction);
            transactions.add(rewardTx);
            Block newBlock = blockchain.mineBlock(transactions);
            new UTXOSet(blockchain).update(newBlock);
            log.info("Success!");
        }else{
            //矿工节点处理。。
            System.out.println("由矿工节点处理。。。。");
            ServerSend.sendTx(Server.knowNodes.get(0), transaction);
        }

    }

    /**
     * 创建钱包
     *
     * @throws Exception
     */
    private void createWallet(String nodeID) throws Exception {
        Wallet wallet = WalletUtils.getInstance(nodeID).createWallet();
        System.out.println("wallet address : " + wallet.getAddress());
    }

    /**
     * 打印钱包地址
     *
     * @throws Exception
     */
    private void printAddresses(String nodeID) throws Exception {
        Set<String> addresses = WalletUtils.getInstance(nodeID).getAddresses();
        if (addresses == null || addresses.isEmpty()) {
            System.out.println("There isn't address");
            return;
        }
        for (String address : addresses) {
            System.out.println("Wallet address: " + address);
        }
    }

    /**
     * 启动节点
     * @param nodeID
     * @param minerAddress
     * @throws Exception
     */
    private void start(String nodeID, String minerAddress) throws Exception {

        System.out.println("minerAddress:" + minerAddress);
        if (null == minerAddress || minerAddress == "") {

        } else {
            // 检查钱包地址是否合法
            try {
                Base58Check.base58ToBytes(minerAddress);
            } catch (Exception e) {
                throw new Exception("ERROR: receiver address invalid ! address=" + minerAddress);
            }
        }

        System.out.printf("启动服务器：localhost:%s\n", nodeID);
        Server.startServer(nodeID, minerAddress);


    }
}