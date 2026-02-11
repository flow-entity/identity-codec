package io.github.nextentity.codec.identity;

import java.io.Serial;

/**
 * 身份编码异常
 * <p>
 * 当身份证编码或解码过程中出现错误时抛出此异常。
 * 包括版本不支持、数据格式错误等情况。
 *
 * @version 1.0
 */
public class IdentityCodecException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -6273621541303090094L;

    /**
     * 错误码
     */
    private final ErrorCode errorCode;

    /**
     * 构造函数
     *
     * @param message 异常信息
     */
    public IdentityCodecException(String message) {
        super(message);
        this.errorCode = null;
    }

    /**
     * 构造函数（带错误码）
     *
     * @param errorCode 错误码枚举
     * @param detail    详细错误信息
     */
    public IdentityCodecException(ErrorCode errorCode, String detail) {
        super(String.format("[%s] %s: %s", errorCode.getCode(), errorCode.getDescription(), detail));
        this.errorCode = errorCode;
    }

    /**
     * 构造函数（带错误码和原因）
     *
     * @param errorCode 错误码枚举
     * @param detail    详细错误信息
     * @param cause     异常原因
     */
    public IdentityCodecException(ErrorCode errorCode, String detail, Throwable cause) {
        super(String.format("[%s] %s: %s", errorCode.getCode(), errorCode.getDescription(), detail), cause);
        this.errorCode = errorCode;
    }

    /**
     * 构造函数（带原因）
     *
     * @param message 异常信息
     * @param cause   异常原因
     */
    public IdentityCodecException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = null;
    }

    /**
     * 获取错误码
     *
     * @return 错误码枚举，可能为null
     */
    public ErrorCode getErrorCode() {
        return errorCode;
    }

    /**
     * 获取错误码字符串
     *
     * @return 错误码字符串，如果未设置则返回null
     */
    public String getErrorCodeString() {
        return errorCode != null ? errorCode.getCode() : null;
    }
}
