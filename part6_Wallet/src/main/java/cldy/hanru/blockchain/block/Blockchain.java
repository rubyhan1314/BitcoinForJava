package cldy.hanru.blockchain.block;


import cldy.hanru.blockchain.store.RocksDBUtils;
import cldy.hanru.blockchain.transaction.SpendableOutputResult;
import cldy.hanru.blockchain.transaction.TXInput;
import cldy.hanru.blockchain.transaction.TXOutput;
import cldy.hanru.blockchain.transaction.Transaction;
import cldy.hanru.blockchain.util.ByteUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	 * 创建区块链，createBlockchain
	 * @param address
	 * @return
	 */
	public static Blockchain createBlockchain(String address) {

		String lastBlockHash = RocksDBUtils.getInstance().getLastBlockHash();
		if (StringUtils.isBlank(lastBlockHash)){
			//对应的bucket不存在，说明是第一次获取区块链实例
			// 创建 coinBase 交易
			Transaction coinbaseTX = Transaction.newCoinbaseTX(address, "");
			Block genesisBlock = Block.newGenesisBlock(coinbaseTX);
//			Block genesisBlock = Block.newGenesisBlock();
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
//	public void addBlock(String data)  throws Exception{
//
//        String lastBlockHash = RocksDBUtils.getInstance().getLastBlockHash();
//        if (StringUtils.isBlank(lastBlockHash)){
//			throw new Exception("还没有数据库，无法直接添加区块。。");
//		}
//		this.addBlock(Block.newBlock(lastBlockHash,data));
//    }


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



	/**
	 * 打包交易，进行挖矿
	 *
	 * @param transactions
	 */
	public void mineBlock(List<Transaction> transactions) throws Exception {
		String lastBlockHash = RocksDBUtils.getInstance().getLastBlockHash();
		Block lastBlock = RocksDBUtils.getInstance().getBlock(lastBlockHash);
		if (lastBlockHash == null) {
			throw new Exception("ERROR: Fail to get last block hash ! ");
		}
		Block block = Block.newBlock(lastBlockHash, transactions,lastBlock.getHeight()+1);
		this.addBlock(block);
	}


	/**
	 * 从交易输入中查询区块链中所有已被花费了的交易输出
	 *
	 * @param address 钱包地址
	 * @return 交易ID以及对应的交易输出下标地址
	 * @throws Exception
	 */
	private Map<String, int[]> getAllSpentTXOs(String address) {
		// 定义TxId ——> spentOutIndex[]，存储交易ID与已被花费的交易输出数组索引值
		Map<String, int[]> spentTXOs = new HashMap<>();
		for (BlockchainIterator blockchainIterator = this.getBlockchainIterator(); blockchainIterator.hashNext(); ) {
			Block block = blockchainIterator.next();

			for (Transaction transaction : block.getTransactions()) {
				// 如果是 coinbase 交易，直接跳过，因为它不存在引用前一个区块的交易输出
				if (transaction.isCoinbase()) {
					continue;
				}
				for (TXInput txInput : transaction.getInputs()) {
					if (txInput.canUnlockOutputWith(address)) {
						String inTxId = Hex.encodeHexString(txInput.getTxId());
						int[] spentOutIndexArray = spentTXOs.get(inTxId);
						if (spentOutIndexArray == null) {
							spentTXOs.put(inTxId, new int[]{txInput.getTxOutputIndex()});
						} else {
							spentOutIndexArray = ArrayUtils.add(spentOutIndexArray, txInput.getTxOutputIndex());
							spentTXOs.put(inTxId, spentOutIndexArray);
						}
					}
				}
			}
		}
		return spentTXOs;
	}
	/**
	 * 查找钱包地址对应的所有未花费的交易
	 *
	 * @param address 钱包地址
	 * @return
	 */
	private Transaction[] findUnspentTransactions(String address) throws Exception {
		Map<String, int[]> allSpentTXOs = this.getAllSpentTXOs(address);
		Transaction[] unspentTxs = {};

		// 再次遍历所有区块中的交易输出
		for (BlockchainIterator blockchainIterator = this.getBlockchainIterator(); blockchainIterator.hashNext(); ) {
			Block block = blockchainIterator.next();
			for (Transaction transaction : block.getTransactions()) {

				String txId = Hex.encodeHexString(transaction.getTxId());

				int[] spentOutIndexArray = allSpentTXOs.get(txId);

				for (int outIndex = 0; outIndex < transaction.getOutputs().length; outIndex++) {
					if (spentOutIndexArray != null && ArrayUtils.contains(spentOutIndexArray, outIndex)) {
						continue;
					}

					// 保存不存在 allSpentTXOs 中的交易
					if (transaction.getOutputs()[outIndex].canBeUnlockedWith(address)) {
						unspentTxs = ArrayUtils.add(unspentTxs, transaction);
					}
				}
			}
		}
		return unspentTxs;
	}

	/**
	 * 查找钱包地址对应的所有UTXO
	 *
	 * @param address 钱包地址
	 * @return
	 */
	public TXOutput[] findUTXO(String address) throws Exception {
		Transaction[] unspentTxs = this.findUnspentTransactions(address);
		TXOutput[] utxos = {};
		if (unspentTxs == null || unspentTxs.length == 0) {
			return utxos;
		}
		for (Transaction tx : unspentTxs) {
			for (TXOutput txOutput : tx.getOutputs()) {
				if (txOutput.canBeUnlockedWith(address)) {
					utxos = ArrayUtils.add(utxos, txOutput);
				}
			}
		}
		return utxos;
	}

	/**
	 * 寻找能够花费的交易
	 *
	 * @param address 钱包地址
	 * @param amount  花费金额
	 */
	public SpendableOutputResult findSpendableOutputs(String address, int amount) throws Exception {
		Transaction[] unspentTXs = this.findUnspentTransactions(address);
		int accumulated = 0;
		Map<String, int[]> unspentOuts = new HashMap<>();
		for (Transaction tx : unspentTXs) {

			String txId = Hex.encodeHexString(tx.getTxId());

			for (int outId = 0; outId < tx.getOutputs().length; outId++) {

				TXOutput txOutput = tx.getOutputs()[outId];

				if (txOutput.canBeUnlockedWith(address) && accumulated < amount) {
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
	 * 从 DB 从恢复区块链数据
	 *
	 * @return
	 * @throws Exception
	 */
	public static Blockchain initBlockchainFromDB() throws Exception {
		String lastBlockHash = RocksDBUtils.getInstance().getLastBlockHash();
		if (lastBlockHash == null) {
			throw new Exception("ERROR: Fail to init blockchain from db. ");
		}
		return new Blockchain(lastBlockHash);
	}

}
