package cldy.hanru.blockchain.block;

import java.math.BigInteger;
import java.time.Instant;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

import lombok.AllArgsConstructor;
import lombok.Data;

import cldy.hanru.blockchain.util.ByteUtils;
/**
 * 
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
	 * 创建新的区块
	 * 
	 * @param previousHash
	 * @param data
	 * @return
	 */
	public static Block newBlock(String previousHash, String data,long height) {
		Block block = new Block("", previousHash, data, Instant.now().getEpochSecond(),height);
		block.setHash();
		return block;
	}

	/**
	 * 设置Hash
	 * 注意：在准备区块数据时，一定要从原始数据类型转化为byte[]，不能直接从字符串进行转换
	 */
	private void setHash() {
		byte[] prevBlockHashBytes = {};
		if (StringUtils.isNoneBlank(this.getPrevBlockHash())) {
			prevBlockHashBytes = new BigInteger(this.getPrevBlockHash(), 16).toByteArray();
		}

		byte[] headers = ByteUtils.merge(prevBlockHashBytes, this.getData().getBytes(),
				ByteUtils.toBytes(this.getTimeStamp()));

		this.setHash(DigestUtils.sha256Hex(headers));
	}
	
	private static final String ZERO_HASH = Hex.encodeHexString(new byte[32]);
	
	/**
	 * 创建创世区块
	 * @return
	 */
	public static Block newGenesisBlock() {
        return Block.newBlock(ZERO_HASH, "Genesis Block",0);
    }
	

}
