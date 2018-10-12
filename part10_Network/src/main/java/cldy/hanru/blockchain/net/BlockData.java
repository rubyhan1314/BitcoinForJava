package cldy.hanru.blockchain.net;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
/**
 * @author hanru
 */
public class BlockData {
    String addrFrom;
    byte[] blockData;
}
