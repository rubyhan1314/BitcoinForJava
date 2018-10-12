package cldy.hanru.blockchain.transaction;

import cldy.hanru.blockchain.util.AddressUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Arrays;

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
//    private String scriptSig;


    /**
     * 签名
     */
    private byte[] signature;
    /**
     * 公钥
     */
    private byte[] pubKey;


    /**
     * 判断解锁数据是否能够解锁交易输出
     *
     * @param unlockingData
     * @return
     */
//    public boolean canUnlockOutputWith(String unlockingData) {
//        return this.getScriptSig().endsWith(unlockingData);
//    }


    /**
     * 检查公钥hash是否用于交易输入
     *
     * @param pubKeyHash
     * @return
     */
    public boolean usesKey(byte[] pubKeyHash) {
        byte[] lockingHash = AddressUtils.ripeMD160Hash(this.getPubKey());
        return Arrays.equals(lockingHash, pubKeyHash);
    }
}
