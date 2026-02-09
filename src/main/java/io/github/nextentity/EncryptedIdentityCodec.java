package io.github.nextentity;

import java.nio.ByteBuffer;

/**
 * 加密身份编码器
 * 结合 SimpleIdentityCodec 的身份编码功能和 XorEncryptor 的加密功能
 * <pre>
 * 工作流程:
 * 1. 使用 SimpleIdentityCodec 将18位身份证编码为 long (56位有效数据)
 * 2. 将 long 转换为8字节数组
 * 3. 使用 XorEncryptor 对8字节数据进行加密
 * 4. 解密时逆向执行上述步骤
 * </pre>
 */
public class EncryptedIdentityCodec implements IdentityCodec {

    private final SimpleIdentityCodec identityCodec;
    private final XorEncryptor xorEncryptor;

    /**
     * 构造函数 - 使用指定的密钥流
     *
     * @param keystream，用于 XOR 加密，不能为空且长度必须为8
     * @throws IllegalArgumentException 当密钥流为空或长度不为8字节时抛出
     */
    public EncryptedIdentityCodec(byte[] keystream) {
        if (keystream == null || keystream.length != 8) {
            throw new IllegalArgumentException("Keystream must be exactly8bytes");
        }
        this.identityCodec = new SimpleIdentityCodec();
        this.xorEncryptor = new XorEncryptor(keystream);
    }

    /**
     * 将18位身份证号码加密编码为long类型
     * <pre>
     * 编码过程：
     * 1. 使用SimpleIdentityCodec将身份证编码为long值
     * 2. 将long值转换为8字节数组
     * 3. 使用XorEncryptor对字节数组进行加密
     * 4. 将加密后的字节转换回long值返回
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
        // 1. 使用SimpleIdentityCodec编码身份证
        long identityLong = identityCodec.encode(identityNumber);

        // 2. 将long转换为8字节数组
        byte[] longBytes = longToByteArray(identityLong);

        // 3. 使用XOR加密
        byte[] encryptedBytes = xorEncryptor.encrypt(longBytes);

        // 4. 将加密后的字节转换回long返回
        return byteArrayToLong(encryptedBytes);
    }

    /**
     * 将加密编码的long值解密为18位身份证号码
     * <pre>
     * 解码过程：
     * 1. 将加密的long值转换为8字节数组
     * 2. 使用XorEncryptor对字节数组进行解密
     * 3. 将解密后的字节转换回long值
     * 4. 使用SimpleIdentityCodec将long值解码为身份证号码
     * </pre>
     *
     * @param encoded 加密编码后的 long 值
     * @return 18位身份证号码字符串
     * @throws IllegalArgumentException 当解密失败或数据格式错误时抛出
     * @see #encode(String)
     * @see SimpleIdentityCodec#decode(long)
     */
    @Override
    public String decode(long encoded) {
        // 1. 将加密的long转换为8字节数组
        byte[] encryptedBytes = longToByteArray(encoded);

        // 2. 使用XOR解密
        byte[] decryptedBytes = xorEncryptor.decrypt(encryptedBytes);

        // 3. 将解密后的字节转换回long
        long identityLong = byteArrayToLong(decryptedBytes);

        // 4. 使用SimpleIdentityCodec解码身份证
        return identityCodec.decode(identityLong);
    }

    /**
     * 将 long 转换为8字节数组(大端序)
     *
     * @param value 要转换的 long 值
     * @return 8字节数组
     */
    private byte[] longToByteArray(long value) {
        return ByteBuffer.allocate(Long.BYTES).putLong(value).array();
    }

    /**
     * 将8字节数组转换为 long (大端序)
     *
     * @param bytes 8字节数组
     * @return long 值
     */
    private long byteArrayToLong(byte[] bytes) {
        if (bytes.length != 8) {
            throw new IllegalArgumentException("Byte array must be exactly 8 bytes");
        }
        return ByteBuffer.wrap(bytes).getLong();
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