package cldy.hanru.blockchain.cli;

import cldy.hanru.blockchain.block.Block;
import cldy.hanru.blockchain.block.Blockchain;
import cldy.hanru.blockchain.pow.ProofOfWork;
import cldy.hanru.blockchain.store.RocksDBUtils;
import cldy.hanru.blockchain.transaction.TXInput;
import cldy.hanru.blockchain.transaction.TXOutput;
import cldy.hanru.blockchain.transaction.Transaction;
import org.apache.commons.cli.*;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

        options.addOption(address);
        options.addOption(sendFrom);
        options.addOption(sendTo);
        options.addOption(sendAmount);
    }

    /**
     * 打印帮助信息
     */
    private void help() {


        System.out.println("Usage:");
        System.out.println("  getbalance -address ADDRESS - Get balance of ADDRESS");
        System.out.println("  createblockchain -address ADDRESS - Create a blockchain and send genesis block reward to ADDRESS");
        System.out.println("  printchain - Print all the blocks of the blockchain");
        System.out.println("  send -from FROM -to TO -amount AMOUNT - Send AMOUNT of coins from FROM address to TO");
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
        try {
            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(options, args);

            switch (args[0]) {
                case "createblockchain":
                    String createblockchainAddress = cmd.getOptionValue("address");
                    if (StringUtils.isBlank(createblockchainAddress)) {
                        help();
                    }
                    this.createBlockchain(createblockchainAddress);
                    break;
                case "getbalance":
                    String getBalanceAddress = cmd.getOptionValue("address");
                    if (StringUtils.isBlank(getBalanceAddress)) {
                        help();
                    }
                    this.getBalance(getBalanceAddress);
                    break;
                case "send":
                    String sendFrom = cmd.getOptionValue("from");
                    String sendTo = cmd.getOptionValue("to");
                    String sendAmount = cmd.getOptionValue("amount");
                    if (StringUtils.isBlank(sendFrom) ||
                            StringUtils.isBlank(sendTo) ||
                            !NumberUtils.isDigits(sendAmount)) {
                        help();
                    }
                    this.send(sendFrom, sendTo, Integer.valueOf(sendAmount));
                    break;
                case "printchain":
                    this.printChain();
                    break;
                case "h":
                    this.help();
                    break;
                default:
                    this.help();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            RocksDBUtils.getInstance().closeDB();
        }
    }

    /**
     * 创建创世块
     */
//        private void createBlockchainWithGenesisBlock () {
//            Blockchain.newBlockchain();
//        }

    /**
     * 创建区块链
     *
     * @param address
     */
    private void createBlockchain(String address) {
        Blockchain.createBlockchain(address);
        System.out.println("Done ! ");
    }

    /**
     * 添加区块
     *
     * @param data
     */
//        private void addBlock (String data) throws Exception {
//            Blockchain blockchain = Blockchain.newBlockchain();
//            blockchain.addBlock(data);
//        }

    /**
     * 打印出区块链中的所有区块
     */
    private void printChain() {
//            Blockchain blockchain = Blockchain.newBlockchain();
        Blockchain blockchain = null;
        try {
            blockchain = Blockchain.initBlockchainFromDB();
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
                    System.out.printf("\t\t交易ID：%s\n" , Hex.encodeHexString(tx.getTxId()));
                    System.out.println("\t\t输入：");
                    for (TXInput in : tx.getInputs()) {
                        System.out.printf("\t\t\tTxID:%s\n" , Hex.encodeHexString(in.getTxId()));
                        System.out.printf("\t\t\tOutputIndex:%d\n" , in.getTxOutputIndex());
                        System.out.printf("\t\t\tScriptSiq:%s\n" , in.getScriptSig());

                    }
                    System.out.println("\t\t输出：");
                    for (TXOutput out : tx.getOutputs()) {
                        System.out.printf("\t\t\tvalue:%d\n" , out.getValue());
                        System.out.printf("\t\t\tScriptPubKey:%s\n" , out.getScriptPubKey());
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
    private void getBalance(String address) throws Exception {
        Blockchain blockchain = Blockchain.createBlockchain(address);
        TXOutput[] txOutputs = blockchain.findUTXO(address);
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
    private void send(String from, String to, int amount) throws Exception {
        Blockchain blockchain = Blockchain.createBlockchain(from);
        Transaction transaction = Transaction.newUTXOTransaction(from, to, amount, blockchain);
        List<Transaction> transactions = new ArrayList<>();
        transactions.add(transaction);
        blockchain.mineBlock(transactions);
        RocksDBUtils.getInstance().closeDB();
        System.out.println("Success!");
    }
}