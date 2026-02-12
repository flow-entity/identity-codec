package io.github.nextentity.codec.identity;

import org.jspecify.annotations.NonNull;

import java.util.Objects;

/**
 * 加密身份编码器
 * 结合 SimpleIdentityCodec 的身份编码功能和各种加密器的加密功能
 * <p>
 * 工作流程：
 * <pre>
 * 1. 使用 SimpleIdentityCodec 将18位身份证编码为 long (56位有效数据)
 * 2. 直接使用指定加密器对 long 值进行加密
 * 3. 解密时逆向执行上述步骤
 * </pre>
 *
 * @version 1.0
 * @since 1.0
 */
public class EncryptedIdentityCodec implements IdentityCodec {

    /**
     * 身份编码器实例
     * <p>
     * 负责将18位身份证号码转换为64位long值
     */
    private final IdentityCodec codec;

    /**
     * 加密器实例
     * <p>
     * 负责对编码后的 long 值进行加密处理
     */
    private final Encryptor encryptor;

    /**
     * 构造函数
     *
     * @param codec     身份编码器实例
     * @param encryptor 加密器实例
     */
    public EncryptedIdentityCodec(@NonNull IdentityCodec codec, @NonNull Encryptor encryptor) {
        this.codec = Objects.requireNonNull(codec, "codec");
        this.encryptor = Objects.requireNonNull(encryptor, "encryptor");
    }

    /**
     * 将18位身份证号码加密编码为long类型
     * <pre>
     * 编码过程：
     * 1. 使用IdentityCodec将身份证编码为long值
     * 2. 使用加密器对long值进行加密
     * </pre>
     *
     * @param identityNumber 18位身份证号码字符串
     * @return 加密编码后的 long 值
     * @throws IdentityNumberFormatException 当身份证格式不正确时抛出
     * @throws IdentityCodecException        当加密过程中发生错误时抛出
     * @see IdentityCodec#encode(IdentityNumber)
     */
    @Override
    public long encode(@NonNull IdentityNumber identityNumber) {
        long encodedIdentity = codec.encode(identityNumber);
        try {
            return encryptor.encrypt(encodedIdentity);
        } catch (EncryptorException e) {
            throw new IdentityCodecException("Encryption failed", e);
        }
    }

    /**
     * 将加密编码的long值解密为18位身份证号码
     * <pre>
     * 解码过程：
     * 1. 使用加密器对加密的long值进行解密
     * 2. 使用IdentityCodec将解密后的long值解码为身份证号码
     * </pre>
     *
     * @param encryptedValue 加密编码后的 long 值
     * @return 18位身份证号码字符串
     * @throws IdentityCodecException 当解密失败或数据格式错误时抛出
     * @see IdentityCodec#encode(IdentityNumber)
     */
    @Override
    public @NonNull IdentityNumber decode(long encryptedValue) {
        long decryptedIdentity;
        try {
            decryptedIdentity = encryptor.decrypt(encryptedValue);
        } catch (EncryptorException e) {
            throw new IdentityCodecException("Decryption failed", e);
        }
        return codec.decode(decryptedIdentity);
    }
}
