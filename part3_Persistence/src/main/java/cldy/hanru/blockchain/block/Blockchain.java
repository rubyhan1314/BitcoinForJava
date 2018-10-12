package cldy.hanru.blockchain.block;


import cldy.hanru.blockchain.store.RocksDBUtils;
import cldy.hanru.blockchain.util.ByteUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/**
 * 区块链
 * @author hanru
 *
 */
@Data
@AllArgsConstructor
public class Blockchain {


	/**
	 * 最后一个区块的hash
	 */
	private String lastBlockHash;

	
	/**
	 * 创建区块链
	 * @return
	 */
	public static Blockchain newBlockchain() {

		String lastBlockHash = RocksDBUtils.getInstance().getLastBlockHash();
		if (StringUtils.isBlank(lastBlockHash)){
			//对应的bucket不存在，说明是第一次获取区块链实例
			Block genesisBlock = Block.newGenesisBlock();
			lastBlockHash = genesisBlock.getHash();
			RocksDBUtils.getInstance().putBlock(genesisBlock);
			RocksDBUtils.getInstance().putLastBlockHash(lastBlockHash);

		}
        return new Blockchain(lastBlockHash);
    }
	
	/**
	 * 根据block，添加区块
	 * @param block
	 */
	public void addBlock(Block block) {

		RocksDBUtils.getInstance().putLastBlockHash(block.getHash());
		RocksDBUtils.getInstance().putBlock(block);
		this.lastBlockHash = block.getHash();

	}
	
	/**
	 * 根据data添加区块
	 * @param data
	 */
	public void addBlock(String data)  throws Exception{

        String lastBlockHash = RocksDBUtils.getInstance().getLastBlockHash();
		Block lastBlock = RocksDBUtils.getInstance().getBlock(lastBlockHash);
        if (StringUtils.isBlank(lastBlockHash)){
			throw new Exception("还没有数据库，无法直接添加区块。。");
		}
		this.addBlock(Block.newBlock(lastBlockHash,data,lastBlock.getHeight()+1));

    }


	/**
	 * 区块链迭代器：内部类
	 */
	public class BlockchainIterator{

		/**
		 * 当前区块的hash
		 */
		private String currentBlockHash;

		/**
		 * 构造函数
		 * @param currentBlockHash
		 */
		public BlockchainIterator(String currentBlockHash) {
			this.currentBlockHash = currentBlockHash;
		}

		/**
		 * 判断是否有下一个区块
		 * @return
		 */
		public boolean hashNext() {
			if (ByteUtils.ZERO_HASH.equals(currentBlockHash)) {
				return false;
			}
			Block lastBlock = RocksDBUtils.getInstance().getBlock(currentBlockHash);
			if (lastBlock == null) {
				return false;
			}
			// 如果是创世区块
			if (ByteUtils.ZERO_HASH.equals(lastBlock.getPrevBlockHash())) {
				return true;
			}
			return RocksDBUtils.getInstance().getBlock(lastBlock.getPrevBlockHash()) != null;
		}


		/**
		 * 迭代获取区块
		 * @return
		 */
		public Block next() {
			Block currentBlock = RocksDBUtils.getInstance().getBlock(currentBlockHash);
			if (currentBlock != null) {
				this.currentBlockHash = currentBlock.getPrevBlockHash();
				return currentBlock;
			}
			return null;
		}
	}

	/**
	 * 添加方法，用于获取迭代器实例
	 * @return
	 */
	public BlockchainIterator getBlockchainIterator() {
		return new BlockchainIterator(lastBlockHash);
	}



	
}
