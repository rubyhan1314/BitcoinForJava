package cldy.hanru.blockchain.net;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
/**
 * @author hanru
 */
public class GetBlocks {
    //getblocks 意为 “给我看一下你有什么区块”（在比特币中，这会更加复杂）
    private String     addrFrom ;
}
