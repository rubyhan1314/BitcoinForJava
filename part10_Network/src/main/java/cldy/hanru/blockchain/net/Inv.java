package cldy.hanru.blockchain.net;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
/**
 * @author hanru
 */
public class Inv {

    String addrFrom; //自己的地址
    String type; //类型 block tx
    List<String> items; //hash的列表
}
