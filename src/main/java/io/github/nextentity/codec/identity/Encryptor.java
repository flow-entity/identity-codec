package io.github.nextentity.codec.identity;

/**
 * 加密器接口
 * <p>
 * 定义了基本的加密解密操作规范，所有具体的加密实现类都需要实现此接口。
 * 该接口专注于64位长整型数据的加密解密操作。
 * <p>
 * <strong>设计原则：</strong>
 * <pre>
 * 对称加密：加密和解密使用相同的密钥
 * 确定性：相同明文和密钥总是产生相同的密文
 * 可逆性：解密操作能够完全恢复原始明文
 * </pre>
 *
 * @version 1.0
 * @see Speck64Encryptor
 * @since 1.0
 */
public interface Encryptor {

    /**
     * 加密64位明文数据
     * <p>
     * 加密算法应该是确定性的，即相同明文和密钥总是产生相同密文。
     *
     * @param plaintext 64位明文数据
     * @return 64位加密后的密文数据
     * @throws RuntimeException 当加密过程中发生错误时抛出
     * @see #decrypt(long)
     */
    long encrypt(long plaintext);

    /**
     * 解密64位密文数据
     * <p>
     * 解密操作应该是加密操作的精确逆过程。
     *
     * @param ciphertext 64位密文数据
     * @return 64位解密后的明文数据
     * @throws RuntimeException 当解密过程中发生错误时抛出
     * @see #encrypt(long)
     */
    long decrypt(long ciphertext);

}