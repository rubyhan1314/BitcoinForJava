package cldy.hanru.blockchain.net;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 区块链版本
 * @author hanru
 */
@AllArgsConstructor
@Data
@NoArgsConstructor
public class Version {
    /**
     * 版本
     */
    public  int version ;
    /**
     * 当前节点区块的高度
     */
    private long bestHeight;
    /**
     * 当前节点的地址
     */
    private String nodeAddress;
}
