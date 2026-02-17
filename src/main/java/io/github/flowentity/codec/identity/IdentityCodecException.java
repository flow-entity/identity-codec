package io.github.flowentity.codec.identity;

import java.io.Serial;

/**
 * 身份编码异常
 * <p>
 * 当身份证编码或解码过程中出现错误时抛出此异常。
 *
 * @version 1.0
 */
public class IdentityCodecException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -6273621541303090094L;

    /**
     * 构造函数
     *
     * @param message 异常信息
     */
    public IdentityCodecException(String message) {
        super(message);
    }

    /**
     * 构造函数（带原因）
     *
     * @param message 异常信息
     * @param cause   异常原因
     */
    public IdentityCodecException(String message, Throwable cause) {
        super(message, cause);
    }
}
