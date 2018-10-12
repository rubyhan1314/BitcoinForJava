package cldy.hanru.blockchain.transaction;

import cldy.hanru.blockchain.block.Block;
import cldy.hanru.blockchain.block.Blockchain;
import cldy.hanru.blockchain.store.RocksDBUtils;
import cldy.hanru.blockchain.util.SerializeUtils;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.ArrayUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 未被花费的交易输出池
 * @author hanru
 */
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class UTXOSet {

    private Blockchain blockchain;


    /**
     * 重建 UTXO 池索引
     */
    @Synchronized
    public void reIndex() {
        log.info("Start to reIndex UTXO set !");
        RocksDBUtils.getInstance(this.blockchain.getNodeID()).cleanChainStateBucket();
        Map<String, TXOutput[]> allUTXOs = blockchain.findAllUTXOs();
        for (Map.Entry<String, TXOutput[]> entry : allUTXOs.entrySet()) {
            RocksDBUtils.getInstance(this.blockchain.getNodeID()).putUTXOs(entry.getKey(), entry.getValue());
        }
        log.info("ReIndex UTXO set finished ! ");
    }

    /**
     * 查找钱包地址对应的所有UTXO
     *
     * @param pubKeyHash 钱包公钥Hash
     * @return
     */
    public TXOutput[] findUTXOs(byte[] pubKeyHash) {
        TXOutput[] utxos = {};
        Map<String, byte[]> chainstateBucket = RocksDBUtils.getInstance(this.blockchain.getNodeID()).getChainstateBucket();
        if (chainstateBucket.isEmpty()) {
            return utxos;
        }
        for (byte[] value : chainstateBucket.values()) {
            TXOutput[] txOutputs = (TXOutput[]) SerializeUtils.deserialize(value);
            for (TXOutput txOutput : txOutputs) {
                if (txOutput.isLockedWithKey(pubKeyHash)) {
                    utxos = ArrayUtils.add(utxos, txOutput);
                }
            }
        }
        return utxos;
    }


    /**
     * 寻找能够花费的交易
     *
     * @param pubKeyHash 钱包公钥Hash
     * @param amount     花费金额
     */
    public SpendableOutputResult findSpendableOutputs(byte[] pubKeyHash, int amount) {
        Map<String, int[]> unspentOuts = new HashMap<>();
        int accumulated = 0;
        Map<String, byte[]> chainstateBucket = RocksDBUtils.getInstance(this.blockchain.getNodeID()).getChainstateBucket();
        for (Map.Entry<String, byte[]> entry : chainstateBucket.entrySet()) {
            String txId = entry.getKey();
            TXOutput[] txOutputs = (TXOutput[]) SerializeUtils.deserialize(entry.getValue());

            for (int outId = 0; outId < txOutputs.length; outId++) {
                TXOutput txOutput = txOutputs[outId];
                if (txOutput.isLockedWithKey(pubKeyHash) && accumulated < amount) {
                    accumulated += txOutput.getValue();

                    int[] outIds = unspentOuts.get(txId);
                    if (outIds == null) {
                        outIds = new int[]{outId};
                    } else {
                        outIds = ArrayUtils.add(outIds, outId);
                    }
                    unspentOuts.put(txId, outIds);
                    if (accumulated >= amount) {
                        break;
                    }
                }
            }
        }
        return new SpendableOutputResult(accumulated, unspentOuts);
    }
    /**
     * 更新UTXO池
     * 当一个新的区块产生时，需要去做两件事情：
     * 1）从UTXO池中移除花费掉了的交易输出；
     * 2）保存新的未花费交易输出；
     *
     * @param tipBlock 最新的区块
     */
    @Synchronized
    public void update(Block tipBlock) {
        if (tipBlock == null) {
            log.error("Fail to update UTXO set ! tipBlock is null !");
            throw new RuntimeException("Fail to update UTXO set ! ");
        }
        for (Transaction transaction : tipBlock.getTransactions()) {

            // 根据交易输入排查出剩余未被使用的交易输出
            if (!transaction.isCoinbase()) {
                for (TXInput txInput : transaction.getInputs()) {
                    // 余下未被使用的交易输出
                    TXOutput[] remainderUTXOs = {};
                    String txId = Hex.encodeHexString(txInput.getTxId());
                    TXOutput[] txOutputs = RocksDBUtils.getInstance(this.blockchain.getNodeID()).getUTXOs(txId);

                    if (txOutputs == null) {
                        continue;
                    }

                    for (int outIndex = 0; outIndex < txOutputs.length; outIndex++) {
                        if (outIndex != txInput.getTxOutputIndex()) {
                            remainderUTXOs = ArrayUtils.add(remainderUTXOs, txOutputs[outIndex]);
                        }
                    }

                    // 没有剩余则删除，否则更新
                    if (remainderUTXOs.length == 0) {
                        RocksDBUtils.getInstance(this.blockchain.getNodeID()).deleteUTXOs(txId);
                    } else {
                        RocksDBUtils.getInstance(this.blockchain.getNodeID()).putUTXOs(txId, remainderUTXOs);
                    }
                }
            }

            // 新的交易输出保存到DB中
            TXOutput[] txOutputs = transaction.getOutputs();
            String txId = Hex.encodeHexString(transaction.getTxId());
            RocksDBUtils.getInstance(this.blockchain.getNodeID()).putUTXOs(txId, txOutputs);
        }

    }



}

