package io.github.nextentity.codec.identity;

/**
 * 加密身份编码器
 * 结合 SimpleIdentityCodec 的身份编码功能和 XorEncryptor 的加密功能
 * <pre>
 * 工作流程:
 * 1. 使用 SimpleIdentityCodec 将18位身份证编码为 long (56位有效数据)
 * 2. 直接使用 XOR 加密器对 long 值进行加密
 * 3. 解密时逆向执行上述步骤
 * </pre>
 *
 * @version 1.0
 */
public class EncryptedIdentityCodec implements IdentityCodec {

    /**
     * 身份编码器实例
     */
    private final SimpleIdentityCodec identityCodec;
    /**
     * XOR 加密器实例
     */
    private final XorEncryptor xorEncryptor;

    /**
     * 构造函数 - 使用指定的加密密钥
     *
     * @param encryptionKey 用于 XOR 加密的64位密钥
     * @throws IllegalArgumentException 当密钥无效时抛出
     */
    public EncryptedIdentityCodec(long encryptionKey) {
        this.identityCodec = new SimpleIdentityCodec();
        this.xorEncryptor = new XorEncryptor(encryptionKey);
    }

    /**
     * 将18位身份证号码加密编码为long类型
     * <pre>
     * 编码过程：
     * 1. 使用SimpleIdentityCodec将身份证编码为long值
     * 2. 直接使用XOR加密器对long值进行加密
     * </pre>
     *
     * @param identityNumber 18位身份证号码字符串
     * @return 加密编码后的 long 值
     * @throws IllegalArgumentException 当身份证格式不正确时抛出
     * @see #decode(long)
     * @see SimpleIdentityCodec#encode(String)
     */
    @Override
    public long encode(String identityNumber) {
        long encodedIdentity = identityCodec.encode(identityNumber);
        return xorEncryptor.encrypt(encodedIdentity);
    }

    /**
     * 将加密编码的long值解密为18位身份证号码
     * <pre>
     * 解码过程：
     * 1. 使用XOR加密器对加密的long值进行解密
     * 2. 使用SimpleIdentityCodec将解密后的long值解码为身份证号码
     * </pre>
     *
     * @param encryptedValue 加密编码后的 long 值
     * @return 18位身份证号码字符串
     * @throws IllegalArgumentException 当解密失败或数据格式错误时抛出
     * @see #encode(String)
     * @see SimpleIdentityCodec#decode(long)
     */
    @Override
    public String decode(long encryptedValue) {
        long decryptedIdentity = xorEncryptor.decrypt(encryptedValue);
        return identityCodec.decode(decryptedIdentity);
    }

    /**
     * 获取内部的SimpleIdentityCodec实例（用于测试或其他用途）
     *
     * @return SimpleIdentityCodec 实例
     */
    SimpleIdentityCodec getIdentityCodec() {
        return identityCodec;
    }

    /**
     * 获取内部的 XorEncryptor 实例（用于测试或其他用途）
     *
     * @return XorEncryptor 实例
     */
    XorEncryptor getXorEncryptor() {
        return xorEncryptor;
    }
}