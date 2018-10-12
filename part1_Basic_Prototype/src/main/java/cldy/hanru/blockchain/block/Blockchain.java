package cldy.hanru.blockchain.block;

import java.util.LinkedList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 区块链
 * @author hanru
 *
 */
@Data
@AllArgsConstructor
public class Blockchain {

	/**
	 * 存储区块的集合
	 */
	private List<Block> blockList;
	
	/**
	 * 创建区块链
	 * @return
	 */
	public static Blockchain newBlockchain() {
        List<Block> blocks = new LinkedList<>();
        blocks.add(Block.newGenesisBlock());
        return new Blockchain(blocks);
    }
	
	/**
	 * 根据block，添加区块
	 * @param block
	 */
	public void addBlock(Block block) {
		this.blockList.add(block);
	}
	
	/**
	 * 根据data添加区块
	 * @param data
	 */
	public void addBlock(String data) {
        Block previousBlock = blockList.get(blockList.size() - 1);
        this.addBlock(Block.newBlock(previousBlock.getHash(), data,previousBlock.getHeight()+1));
    }
	
	
}
