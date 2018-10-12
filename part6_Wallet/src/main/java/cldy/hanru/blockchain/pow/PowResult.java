package cldy.hanru.blockchain.pow;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 工作量计算结果
 * @author ruby
 *
 */
@Data
@AllArgsConstructor
public class PowResult {

	/**
     * 计数器
     */
    private long nonce;
    /**
     * hash值
     */
    private String hash;
    
}
