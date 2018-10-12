package cldy.hanru.blockchain.net;

/**
 * 节点服务的常量
 * @author hanru
 */
public class ServerConst {

    public static int NODE_VERSION = 1;

    // 命令
    public static final String COMMAND_VERSION = "version";
    public static final String COMMAND_GETBLOCKS = "getblocks";
    public static final String COMMAND_ADDR = "addr";
    public static final String COMMAND_BLOCK = "block";
    public static final String COMMAND_INV = "inv";
    public static final String COMMAND_GETDATA = "getdata";
    public static final String COMMAND_TX = "tx";


    // 类型
    public static final String BLOCK_TYPE = "block";
    public static final String TX_TYPE = "tx";


}
