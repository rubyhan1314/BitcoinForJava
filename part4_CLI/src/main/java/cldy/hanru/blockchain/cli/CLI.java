package cldy.hanru.blockchain.cli;

import cldy.hanru.blockchain.block.Block;
import cldy.hanru.blockchain.block.Blockchain;
import cldy.hanru.blockchain.pow.ProofOfWork;
import cldy.hanru.blockchain.store.RocksDBUtils;
import org.apache.commons.cli.*;

import java.text.SimpleDateFormat;
import java.util.Date;

public class CLI {
    private String[] args;
    private Options options = new Options();



    public CLI(String[] args) {
        this.args = args;

        Option helpCmd = Option.builder("h").desc("show help").build();
        options.addOption(helpCmd);

        Option data = Option.builder("data").hasArg(true).desc("add block").build();

        options.addOption(data);

    }

    /**
     * 打印帮助信息
     */
    private void help() {
        System.out.println("Usage:");
        System.out.println("  createblockchain -address ADDRESS - Create a blockchain and send genesis block reward to ADDRESS");
        System.out.println("  addblock -data DATA - Get balance of ADDRESS");
        System.out.println("  printchain - Print all the blocks of the blockchain");
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
                    createBlockchainWithGenesisBlock();
                    break;
                case "addblock":
                    String data = cmd.getOptionValue("data");
                    addBlock(data);
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
    private void createBlockchainWithGenesisBlock(){
        Blockchain.newBlockchain();
    }

    /**
     * 添加区块
     *
     * @param data
     */
    private void addBlock(String data) throws Exception {
        Blockchain blockchain = Blockchain.newBlockchain();
        blockchain.addBlock(data);
    }

    /**
     * 打印出区块链中的所有区块
     */
    private void printChain() {
        Blockchain blockchain = Blockchain.newBlockchain();
        Blockchain.BlockchainIterator iterator = blockchain.getBlockchainIterator();
        while (iterator.hashNext()) {
            Block block = iterator.next();
            System.out.println("第" + block.getHeight() + "个区块信息：");

            if (block != null){
                boolean validate = ProofOfWork.newProofOfWork(block).validate();
                System.out.println("validate = " + validate);
                System.out.println("\tprevBlockHash: " + block.getPrevBlockHash());
                System.out.println("\tData: " + block.getData());
                System.out.println("\tHash: " + block.getHash());
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String date = sdf.format(new Date(block.getTimeStamp() * 1000L));
                System.out.println("\ttimeStamp:" + date);
                System.out.println();
            }
        }
    }
}
