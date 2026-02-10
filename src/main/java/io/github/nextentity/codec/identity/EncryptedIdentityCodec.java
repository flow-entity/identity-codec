package io.github.nextentity.codec.identity;

import java.util.Objects;

/**
 * 加密身份编码器
 * 结合 SimpleIdentityCodec 的身份编码功能和各种加密器的加密功能
 *
 * <p>核心设计理念：
 * <pre>
 * - 组合模式：将身份编码和加密功能分离
 * - 策略模式：支持多种加密算法切换
 * - 单一职责：每个组件专注自己的功能
 * - 可扩展性：易于添加新的加密算法
 * </pre>
 * </p>
 *
 * <p>工作流程：
 * <pre>
 * 1. 使用 SimpleIdentityCodec 将18位身份证编码为 long (56位有效数据)
 * 2. 直接使用指定加密器对 long 值进行加密
 * 3. 解密时逆向执行上述步骤
 * </pre>
 * </p>
 *
 * <p>安全等级说明：
 * <pre>
 * 高安全性：SPECK64加密器
 * 中等安全：AES/CTR加密器（注意IV管理）
 * 低安全性：XOR加密器（仅用于测试）
 * </pre>
 * </p>
 *
 * @author nextentity
 * @version 1.0
 * @since 1.0
 */
public class EncryptedIdentityCodec implements IdentityCodec {

    /**
     * 身份编码器实例
     * <p>负责将18位身份证号码转换为64位long值
     * <pre>
     * 功能特点：
     * - 验证身份证格式合法性
     * - 提取有效信息并压缩编码
     * - 支持校验码验证
     * - 处理日期有效性检查
     * </pre>
     * </p>
     */
    private final IdentityCodec codec;

    /**
     * 加密器实例
     * <p>负责对编码后的 long 值进行加密处理
     * <pre>
     * 支持的加密算法：
     * - SPECK64：轻量级分组密码（推荐）
     * - AES/CTR：工业标准加密
     * - XOR：简单异或加密（测试用）
     * </pre>
     * </p>
     */
    private final Encryptor encryptor;

    /**
     * 创建使用SPECK64加密的加密身份编码器（使用字节数组密钥）
     *
     * <p>工作原理：
     * <pre>
     * 1. 使用SimpleIdentityCodec将18位身份证编码为long值
     * 2. 使用SPECK64加密器对编码结果进行加密
     * 3. 解密时执行相反的操作
     * </pre>
     * </p>
     *
     * <p>算法特点：
     * <pre>
     * - 分组大小：64位
     * - 密钥长度：128位（16字节）
     * - 标准轮数：27轮
     * - 轻量级设计，适合移动设备
     * - 经过充分的安全性分析
     * </pre>
     * </p>
     *
     * @param key SPECK64加密密钥字节数组，必须为16字节长度
     * @return EncryptedIdentityCodec实例，使用SPECK64加密
     * @throws IllegalArgumentException 当密钥为null或长度不为16字节时抛出
     * @see Speck64Encryptor
     */
    public static EncryptedIdentityCodec speck64Encrypt(byte[] key) {
        IdentityCodec codec = new SimpleIdentityCodec();
        Encryptor encryptor = new Speck64Encryptor(key);
        return new EncryptedIdentityCodec(codec, encryptor);
    }

    /**
     * 创建使用SPECK64加密的加密身份编码器（使用int数组密钥）
     *
     * <p>工作原理：
     * <pre>
     * 1. 使用SimpleIdentityCodec将18位身份证编码为long值
     * 2. 使用SPECK64加密器对编码结果进行加密
     * 3. 解密时执行相反的操作
     * </pre>
     * </p>
     *
     * <p>算法特点：
     * <pre>
     * - 分组大小：64位
     * - 密钥长度：128位（4个32位整数）
     * - 标准轮数：27轮
     * - 轻量级设计，适合移动设备
     * - 经过充分的安全性分析
     * </pre>
     * </p>
     *
     * @param key 128位加密密钥，必须包含4个32位整数
     * @return EncryptedIdentityCodec实例，使用SPECK64加密
     * @throws IllegalArgumentException 当密钥为null或长度不为16字节时抛出
     * @see Speck64Encryptor
     */
    public static EncryptedIdentityCodec speck64Encrypt(int[] key) {
        IdentityCodec codec = new SimpleIdentityCodec();
        Encryptor encryptor = new Speck64Encryptor(key);
        return new EncryptedIdentityCodec(codec, encryptor);
    }

    public EncryptedIdentityCodec(IdentityCodec codec, Encryptor encryptor) {
        this.codec = Objects.requireNonNull(codec, "codec");
        this.encryptor = Objects.requireNonNull(encryptor, "encryptor");
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
        long encodedIdentity = codec.encode(identityNumber);
        return encryptor.encrypt(encodedIdentity);
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
        long decryptedIdentity = encryptor.decrypt(encryptedValue);
        return codec.decode(decryptedIdentity);
    }
}