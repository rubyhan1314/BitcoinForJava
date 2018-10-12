package cldy.hanru.blockchain.block;

import java.time.Instant;

import org.apache.commons.codec.binary.Hex;

import cldy.hanru.blockchain.pow.PowResult;
import cldy.hanru.blockchain.pow.ProofOfWork;
import lombok.AllArgsConstructor;
import lombok.Data;
/**
 * 区块
 * @author hanru
 *
 */
@AllArgsConstructor
@Data
public class Block {

	/**
	 * 区块hash值
	 */
	private String hash;

	/**
	 * 前一个区块的hash值
	 */
	private String prevBlockHash;

	/**
	 * 区块数据，未来会替换为交易
	 */
	private String data;

	/**
	 * 时间戳，单位秒
	 */
	private long timeStamp;

	/**
	 * 区块的高度
	 */
	private long height;

	/**
     * 工作量证明计数器
     */
    private long nonce;


	
	
	/**
	 * 创建新的区块
	 * 
	 * @param previousHash
	 * @param data
	 * @return
	 */
	public static Block newBlock(String previousHash, String data,long height) {
		Block block = new Block("", previousHash, data, Instant.now().getEpochSecond(),height,0);
		ProofOfWork pow = ProofOfWork.newProofOfWork(block);
        PowResult powResult = pow.run();
        block.setHash(powResult.getHash());
        block.setNonce(powResult.getNonce());
//		block.setHash();
		return block;
	}

	/**
	 * 设置Hash
	 * 注意：在准备区块数据时，一定要从原始数据类型转化为byte[]，不能直接从字符串进行转换
	 */
//	private void setHash() {
//		byte[] prevBlockHashBytes = {};
//		if (StringUtils.isNoneBlank(this.getPrevBlockHash())) {
//			prevBlockHashBytes = new BigInteger(this.getPrevBlockHash(), 16).toByteArray();
//		}
//
//		byte[] headers = ByteUtils.merge(prevBlockHashBytes, this.getData().getBytes(),
//				ByteUtils.toBytes(this.getTimeStamp()));
//
//		this.setHash(DigestUtils.sha256Hex(headers));
//	}
	
	private static final String ZERO_HASH = Hex.encodeHexString(new byte[32]);
	
	/**
	 * 创建创世区块
	 * @return
	 */
	public static Block newGenesisBlock() {
        return Block.newBlock(ZERO_HASH, "Genesis Block",0);
    }
	

}
