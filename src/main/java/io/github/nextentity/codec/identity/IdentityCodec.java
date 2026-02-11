package io.github.nextentity.codec.identity;

import org.jspecify.annotations.NonNull;

/**
 * 身份编码器接口
 *
 * <pre>
 * 定义身份证号码编码和解码的标准接口
 * 实现类应提供将18位身份证号码与long类型之间相互转换的功能
 * </pre>
 * <p>
 * <strong>获取实例的方式：</strong>
 * 加密编码使用 IdentityCodecs 工厂类获取实例
 * <pre>{@code
 * IdentityCodec codec = IdentityCodecs.speck64Encrypt(new int[]{1, 2, 3, 4});}
 * </pre>
 * <p>
 * 不加密编码直接实例化 SimpleIdentityCodec
 * <pre>{@code
 * IdentityCodec simpleCodec = new SimpleIdentityCodec();}
 * </pre>
 *
 * @version 1.0
 * @see IdentityCodecs
 * @see SimpleIdentityCodec
 * @see EncryptedIdentityCodec
 */
public interface IdentityCodec {

    /**
     * 将身份证号码编码为 long 类型
     *
     * @param identityNumber 18位身份证号码字符串
     * @return 编码后的 long 值
     * @throws IdentityNumberFormatException 当输入格式不正确时抛出
     * @throws IdentityCodecException        当编码过程中发生错误时抛出
     */
    long encode(@NonNull IdentityNumber identityNumber);


    /**
     * 将 long 类型的编码解码为身份证号码
     *
     * @param encoded 编码后的 long 值
     * @return 18 位身份证号码字符串
     * @throws IdentityCodecException 当解码失败时抛出
     */
    @NonNull IdentityNumber decode(long encoded);

}
