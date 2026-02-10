package io.github.nextentity.codec.identity;

import java.io.Serial;

/**
 * 无效编码异常
 * <p>
 * 当身份证编码/解码过程中出现错误时抛出此异常。
 * 包括版本不支持、数据格式错误等情况。
 * </p>
 *
 * @version 1.0
 */
public class InvalidEncodingException extends IllegalArgumentException {

    @Serial
    private static final long serialVersionUID = -6273621541303090094L;

    /**
     * 构造函数
     *
     * @param message 异常信息
     */
    public InvalidEncodingException(String message) {
        super(message);
    }

}
