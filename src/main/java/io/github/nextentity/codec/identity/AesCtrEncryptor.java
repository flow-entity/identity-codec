package io.github.nextentity.codec.identity;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;

/**
 * AES/CTR模式加密器实现
 * <pre>
 * 该类实现了基于AES算法的CTR（计数器）模式加密解密功能。
 * CTR模式将块密码转换为流密码，具有以下特点：
 * - 支持并行加密/解密
 * - 加密和解密使用相同的操作
 * - 对相同明文使用相同密钥和IV会产生相同密文
 * - 不需要填充（NoPadding）
 *
 * 注意事项：
 * - 密钥长度必须为16字节（AES-128）
 * - 使用固定的全零IV向量
 * - 适用于需要高性能的场景
 * </pre>
 */
public class AesCtrEncryptor implements Encryptor {
    private static final String ALGORITHM = "AES/CTR/NoPadding";
    private final SecretKeySpec keySpec;
    private final IvParameterSpec ivSpec = new IvParameterSpec(new byte[16]);

    /**
     * 构造函数
     *
     * @param key AES密钥，必须为16字节长度
     * @throws IllegalArgumentException 当密钥为null或长度不为16字节时抛出
     */
    public AesCtrEncryptor(byte[] key) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
        if (key.length != 16) {
            throw new IllegalArgumentException("AES key length must be 16 bytes, but got " + key.length + " bytes");
        }
        this.keySpec = new SecretKeySpec(key, "AES");
    }

    /**
     * 加密长整型数据
     * <p>
     * 将长整型数据转换为字节数组进行AES/CTR加密，然后将结果转换回长整型
     *
     * @param plaintext 明文数据
     * @return 加密后的数据
     * @throws RuntimeException 当加密过程中发生安全异常时抛出
     */
    @Override
    public long encrypt(long plaintext) {
        byte[] bytes = new byte[Long.BYTES];
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        buffer.putLong(plaintext);
        try {
            byte[] encryptedBytes = encrypt(bytes);
            return ByteBuffer.wrap(encryptedBytes).getLong();
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 解密长整型数据
     * <p>
     * 将加密的长整型数据转换为字节数组进行AES/CTR解密，然后将结果转换回长整型
     *
     * @param encrypted 加密数据
     * @return 解密后的明文数据
     * @throws RuntimeException 当解密过程中发生安全异常时抛出
     */
    @Override
    public long decrypt(long encrypted) {
        byte[] bytes = new byte[Long.BYTES];
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        buffer.putLong(encrypted);
        try {
            byte[] decryptedBytes = decrypt(bytes);
            return ByteBuffer.wrap(decryptedBytes).getLong();
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * 加密字节数组
     * <p>
     * 使用AES/CTR模式加密输入的字节数组
     *
     * @param plaintext 待加密的字节数组
     * @return 加密后的字节数组
     * @throws GeneralSecurityException 当加密过程中发生安全异常时抛出
     */
    public byte[] encrypt(byte[] plaintext) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
        return cipher.doFinal(plaintext);
    }

    /**
     * 解密字节数组
     * <p>
     * 使用AES/CTR模式解密输入的字节数组
     *
     * @param ciphertext 待解密的字节数组
     * @return 解密后的字节数组
     * @throws GeneralSecurityException 当解密过程中发生安全异常时抛出
     */
    public byte[] decrypt(byte[] ciphertext) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
        return cipher.doFinal(ciphertext);
    }

}
