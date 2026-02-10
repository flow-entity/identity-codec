package io.github.nextentity.codec.identity;

/**
 * 身份编码器接口
 *
 * <pre>
 * 定义身份证号码编码和解码的标准接口
 * 实现类应提供将18位身份证号码与long类型之间相互转换的功能
 * </pre>
 *
 * @version 1.0
 */
public interface IdentityCodec {

    /**
     * 将身份证号码编码为 long 类型
     *
     * @param identityNumber 18位身份证号码字符串
     * @return 编码后的 long 值
     * @throws IllegalArgumentException 当输入格式不正确时抛出
     */
    long encode(String identityNumber);

    /**
     * 将 long 类型的编码解码为身份证号码
     *
     * @param encoded 编码后的 long 值
     * @return 18 位身份证号码字符串
     * @throws IllegalArgumentException 当解码失败时抛出
     */
    String decode(long encoded);
}
