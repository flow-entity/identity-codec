package io.github.nextentity.codec.identity;

import java.io.Serial;

/**
 * 无效编码异常
 * <p>
 * 当身份证解码过程中出现错误时抛出此异常。
 * 包括版本不支持、数据格式错误等情况。
 *
 * @version 1.0
 */
public class InvalidEncodingException extends IllegalArgumentException {

    @Serial
    private static final long serialVersionUID = -6273621541303090094L;

    /**
     * 错误码枚举
     */
    public enum ErrorCode {
        /**
         * 不支持的压缩版本
         */
        UNSUPPORTED_VERSION("IEC-001", "Unsupported compression version"),
        /**
         * 预留位必须为零
         */
        RESERVED_BITS_NOT_ZERO("IEC-002", "Reserved bits must be zero"),
        /**
         * 无效的位域提取
         */
        INVALID_BIT_FIELD("IEC-003", "Invalid bit field extraction"),
        /**
         * 解密操作失败
         */
        DECRYPTION_FAILED("IEC-004", "Decryption operation failed");

        private final String code;
        private final String description;

        ErrorCode(String code, String description) {
            this.code = code;
            this.description = description;
        }

        /**
         * 获取错误码
         *
         * @return 错误码
         */
        public String getCode() {
            return code;
        }

        /**
         * 获取错误描述
         *
         * @return 错误描述
         */
        public String getDescription() {
            return description;
        }
    }

    /**
     * 错误码
     */
    private final ErrorCode errorCode;

    /**
     * 构造函数
     *
     * @param message 异常信息
     */
    public InvalidEncodingException(String message) {
        super(message);
        this.errorCode = null;
    }

    /**
     * 构造函数（带错误码）
     *
     * @param errorCode 错误码枚举
     * @param detail    详细错误信息
     */
    public InvalidEncodingException(ErrorCode errorCode, String detail) {
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
    public InvalidEncodingException(ErrorCode errorCode, String detail, Throwable cause) {
        super(String.format("[%s] %s: %s", errorCode.getCode(), errorCode.getDescription(), detail), cause);
        this.errorCode = errorCode;
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
