package cldy.hanru.blockchain.net;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
/**
 * author hanru
 */
public class GetData {

    //用于某个块或交易的请求，它可以仅包含一个块或交易的 ID。
    String addrFrom;
    String type;
    String hash;

}
