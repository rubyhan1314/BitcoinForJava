package cldy.hanru.blockchain.util;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.stream.Stream;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.ArrayUtils;

/**
 * 字节数组工具类
 * @author hanru
 *
 */
public class ByteUtils {

	public static final String ZERO_HASH = Hex.encodeHexString(new byte[32]);


	/**
	 * 将多个字节数组合并成一个字节数组
	 * @param bytes
	 * @return
	 */
	public static byte[] merge(byte[]... bytes) {
        Stream<Byte> stream = Stream.of();
        for (byte[] b : bytes) {
            stream = Stream.concat(stream, Arrays.stream(ArrayUtils.toObject(b)));
        }
        return ArrayUtils.toPrimitive(stream.toArray(Byte[]::new));
    }
	
	/**
	 * long 转化为 byte[]
	 * @param val
	 * @return
	 */
	public static byte[] toBytes(long val) {


        return ByteBuffer.allocate(Long.BYTES).putLong(val).array();
    }


	/**
	 * int 类型转 byte[]
	 *
	 * @param val
	 * @return
	 */
	public static byte[] toBytes(int val) {
		return ByteBuffer.allocate(Integer.BYTES).putInt(val).array();
	}

	/**
	 * byte[] 转化为 int
	 *
	 * @param bytes
	 * @return
	 */
	public static int toInt(byte[] bytes) {
		return ByteBuffer.wrap(bytes).getInt();
	}


	/**
	 * byte[] 转为16进制，String
	 * @param bytes
	 * @return
	 */
	public static String bytesToHexString(byte[] bytes) {
		StringBuilder buf = new StringBuilder(bytes.length * 2);
		for(byte b : bytes) { // 使用String的format方法进行转换
			buf.append(String.format("%02x", new Integer(b & 0xff)));
		}

		return buf.toString();
	}


}
