package cldy.hanru.blockchain.pow;

import java.math.BigInteger;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

import cldy.hanru.blockchain.block.Block;
import cldy.hanru.blockchain.util.ByteUtils;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 工作量证明
 * @author hanru
 *
 */
@Data
@AllArgsConstructor
public class ProofOfWork {

	/**
     * 难度目标位
     * 0000 0000 0000 0000 1001 0001 0000  .... 0001
     * 256位Hash里面前面至少有16个零
     */
    public static final int TARGET_BITS = 16;
    
    /**
     * 要验证的区块
     */
    private Block block;
    
    /**
     * 难度目标值
     */
    private BigInteger target;
    
    
    /**
     * 创建新的工作量证明对象
     * 
     * 对1进行移位运算，将1向左移动 (256 - TARGET_BITS) 位，得到我们的难度目标值
     * @param block
     * @return
     */
    public static ProofOfWork newProofOfWork(Block block) {
    	/*
    	1.创建一个BigInteger的数值1.
    	0000000.....00001
    	2.左移256-bits位
    	
    	以8 bit为例
    	0000 0001
    	0010 0000
    	
    	8-6 
    	 */

        BigInteger targetValue = BigInteger.ONE.shiftLeft((256 - TARGET_BITS));
        return new ProofOfWork(block, targetValue);
    }
    
    /**
     * 运行工作量证明，开始挖矿，找到小于难度目标值的Hash
     * @return
     */
    public PowResult run() {
        long nonce = 0;
        String shaHex = "";
//        System.out.printf("开始进行挖矿：%s \n", this.getBlock().getData());
        System.out.printf("开始进行挖矿： \n");
        long startTime = System.currentTimeMillis();
        while (nonce < Long.MAX_VALUE) {
            byte[] data = this.prepareData(nonce);
            shaHex = DigestUtils.sha256Hex(data);
            System.out.printf("\r%d: %s",nonce,shaHex);
            if (new BigInteger(shaHex, 16).compareTo(this.target) == -1) {
            	System.out.println();
                System.out.printf("耗时 Time: %s seconds \n", (float) (System.currentTimeMillis() - startTime) / 1000);
                System.out.printf("当前区块Hash: %s \n\n", shaHex);
                break;
            } else {
                nonce++;
            }
        }
        return new PowResult(nonce, shaHex);
    }
    
    /**
     * 根据block的数据，以及nonce，生成一个byte数组
     *
     * 注意：在准备区块数据时，一定要从原始数据类型转化为byte[]，不能直接从字符串进行转换
     * @param nonce
     * @return
     */
    private byte[] prepareData(long nonce) {
        byte[] prevBlockHashBytes = {};
        if (StringUtils.isNoneBlank(this.getBlock().getPrevBlockHash())) {
            prevBlockHashBytes = new BigInteger(this.getBlock().getPrevBlockHash(), 16).toByteArray();
        }

        return ByteUtils.merge(
                prevBlockHashBytes,
//                this.getBlock().getData().getBytes(),
                this.getBlock().hashTransaction(),
                ByteUtils.toBytes(this.getBlock().getTimeStamp()),
                ByteUtils.toBytes(TARGET_BITS),
                ByteUtils.toBytes(nonce)
        );

    }
    
    /**
     * 验证区块是否有效
     *
     * @return
     */
    public boolean validate() {
        byte[] data = this.prepareData(this.getBlock().getNonce());
        return new BigInteger(DigestUtils.sha256Hex(data), 16).compareTo(this.target) == -1;
    }
    
}
