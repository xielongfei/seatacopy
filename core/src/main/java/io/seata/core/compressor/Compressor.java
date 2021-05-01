package io.seata.core.compressor;

/**
 * @author: xielongfei
 * @date: 2021/04/30 11:10
 * @description:
 */
public interface Compressor {

    /**
     * compress byte[] to byte[].
     * @param bytes the bytes
     * @return the byte[]
     */
    byte[] compress(byte[] bytes);

    /**
     * decompress byte[] to byte[].
     * @param bytes the bytes
     * @return the byte[]
     */
    byte[] decompress(byte[] bytes);
}
