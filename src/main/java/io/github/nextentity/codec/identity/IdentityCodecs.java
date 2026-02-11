package io.github.nextentity.codec.identity;

import org.jspecify.annotations.NonNull;

/**
 * 身份编码器工厂类
 * <p>
 * 提供创建各种身份编码器实例的静态工厂方法。
 * 通过此类可以方便地获取预配置的身份编码器实例，
 * 包括加密和非加密版本。
 * <p>
 * <strong>使用示例：</strong>
 *
 * <pre>
 * {@code
 * // 创建SPECK64加密的身份编码器
 * IdentityCodec codec = IdentityCodecs.speck64Encrypt(new int[]{1, 2, 3, 4});
 *
 * // 编码身份证号码
 * long encoded = encryptedCodec.encode("110101199001011234");
 *
 * // 解码身份证号码
 * String decoded = encryptedCodec.decode(encoded);
 * }
 * </pre>
 * <p>
 * <strong>支持的编码器类型：</strong>
 * <pre>
 * - SPECK64加密编码器：提供高强度加密保护
 * - 简单编码器：基础的无加密编码功能
 * </pre>
 *
 * @author nextentity
 * @version 1.0
 * @see IdentityCodec
 * @see SimpleIdentityCodec
 * @see EncryptedIdentityCodec
 * @since 1.0
 */
public class IdentityCodecs {
    /**
     * 创建使用SPECK64加密的加密身份编码器（使用字节数组密钥）
     *
     * @param key SPECK64加密密钥字节数组，必须为16字节长度
     * @return EncryptedIdentityCodec实例，使用SPECK64加密
     * @throws IllegalArgumentException 当密钥为null或长度不为16字节时抛出
     * @see Speck64Encryptor
     * @see #speck64Encrypt(int[])
     */
    public static @NonNull IdentityCodec speck64Encrypt(byte @NonNull [] key) {
        IdentityCodec codec = new SimpleIdentityCodec();
        Encryptor encryptor = new Speck64Encryptor(key);
        return new EncryptedIdentityCodec(codec, encryptor);
    }

    /**
     * 创建使用SPECK64加密的加密身份编码器（使用int数组密钥）
     * <p>
     * 工作原理：
     * <pre>
     * 1. 使用SimpleIdentityCodec将18位身份证编码为long值
     * 2. 使用SPECK64加密器对编码结果进行加密
     * 3. 解密时执行相反的操作
     * </pre>
     * <p>
     * 算法特点
     * <pre>
     * - 分组大小：64位
     * - 密钥长度：128位（4个32位整数）
     * - 标准轮数：27轮
     * - 轻量级设计，适合移动设备
     * - 经过充分的安全性分析
     * </pre>
     *
     * @param key 128位加密密钥，必须包含4个32位整数
     * @return EncryptedIdentityCodec实例，使用SPECK64加密
     * @throws IllegalArgumentException 当密钥为长度不为4(16字节)时抛出
     * @see Speck64Encryptor
     */
    public static @NonNull IdentityCodec speck64Encrypt(int @NonNull [] key) {
        IdentityCodec codec = new SimpleIdentityCodec();
        Encryptor encryptor = new Speck64Encryptor(key);
        return new EncryptedIdentityCodec(codec, encryptor);
    }

    /**
     * 私有构造方法，防止实例化
     */
    private IdentityCodecs() {
    }


}
