package cldy.hanru.blockchain.transaction;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author hanru
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class TXInput {


    /**
     * 交易Id的hash值
     */
    private byte[] txId;
    /**
     * 交易输出索引
     */
    private int txOutputIndex;
    /**
     * 解锁脚本
     */
    private String scriptSig;

    /**
     * 判断解锁数据是否能够解锁交易输出
     *
     * @param unlockingData
     * @return
     */
    public boolean canUnlockOutputWith(String unlockingData) {
        return this.getScriptSig().endsWith(unlockingData);
    }
}
