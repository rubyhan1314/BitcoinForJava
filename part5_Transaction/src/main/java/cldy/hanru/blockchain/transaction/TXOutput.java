package cldy.hanru.blockchain.transaction;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author hanru
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TXOutput {

    /**
     * 数值金额
     */
    private int value;
    /**
     * 锁定脚本
     */
    private String scriptPubKey;

    /**
     * 判断解锁数据是否能够解锁交易输出
     *
     * @param unlockingData
     * @return
     */
    public boolean canBeUnlockedWith(String unlockingData) {
        return this.getScriptPubKey().endsWith(unlockingData);
    }
}
