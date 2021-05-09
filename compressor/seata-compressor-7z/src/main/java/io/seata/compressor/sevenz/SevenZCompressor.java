package io.seata.compressor.sevenz;

import io.seata.common.loader.LoadLevel;
import io.seata.core.compressor.Compressor;

/**
 * @author: xielongfei
 * @date: 2021/05/08 09:15
 * @description:
 */
@LoadLevel(name = "SEVENZ")
public class SevenZCompressor implements Compressor {

    @Override
    public byte[] compress(byte[] bytes) {
        return SevenZUtil.compress(bytes);
    }

    @Override
    public byte[] decompress(byte[] bytes) {
        return SevenZUtil.decompress(bytes);
    }
}
