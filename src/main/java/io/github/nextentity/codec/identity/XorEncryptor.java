package io.github.nextentity.codec.identity;

/**
 * XOR加密器
 * 使用固定密钥对64位数据进行 XOR 加密和解密
 * <pre>
 * 该类实现了简单的 XOR 流加密算法，适用于long类型数据的快速加密
 * XOR加密具有以下特点：
 * - 加密和解密使用相同的算法
 * - 性能优异，适合高频调用场景
 * - 安全性依赖于密钥的保密性
 * </pre>
 * 
 * @version 1.0
 */
public class XorEncryptor {
    /** 64位加密密钥 */
    private final long encryptionKey;

    /**
     * 构造函数 - 初始化加密器
     *
     * @param encryptionKey 64位加密密钥
     */
    public XorEncryptor(long encryptionKey) {
        this.encryptionKey = encryptionKey;
    }


    /**
     * 加密数据
     * 对输入的64位数据进行 XOR 加密
     *
     * @param plaintextData 待加密的原始数据
     * @return 加密后的数据
     */
    public long encrypt(long plaintextData) {
        return plaintextData ^ encryptionKey;
    }

    /**
     * 解密数据
     * 对输入的64位密文进行 XOR 解密
     *
     * @param encryptedData 待解密的密文数据
     * @return 解密后的明文数据
     */
    public long decrypt(long encryptedData) {
        return encryptedData ^ encryptionKey;
    }

}