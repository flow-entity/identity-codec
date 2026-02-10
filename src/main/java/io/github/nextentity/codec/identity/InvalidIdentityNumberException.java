package io.github.nextentity.codec.identity;

import java.io.Serial;

/**
 * 无效身份证号码异常
 * <p>
 * 当身份证号码格式不正确或校验码验证失败时抛出此异常。
 * </p>
 *
 * @version 1.0
 */
public class InvalidIdentityNumberException extends IllegalArgumentException {

    @Serial
    private static final long serialVersionUID = 4270343855622804394L;

    /**
     * 错误码枚举
     */
    public enum ErrorCode {
        INVALID_LENGTH("IIN-001", "Invalid ID number length"),
        INVALID_CHECK_CODE("IIN-002", "Invalid check code"),
        INVALID_CHARACTER("IIN-003", "Invalid character in ID number"),
        INVALID_DATE("IIN-004", "Invalid birth date");

        private final String code;
        private final String description;

        ErrorCode(String code, String description) {
            this.code = code;
            this.description = description;
        }

        public String getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }
    }

    private final ErrorCode errorCode;

    /**
     * 构造函数
     *
     * @param message 异常信息
     */
    public InvalidIdentityNumberException(String message) {
        super(message);
        this.errorCode = null;
    }

    /**
     * 构造函数（带错误码）
     *
     * @param errorCode 错误码枚举
     * @param detail    详细错误信息
     */
    public InvalidIdentityNumberException(ErrorCode errorCode, String detail) {
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
    public InvalidIdentityNumberException(ErrorCode errorCode, String detail, Throwable cause) {
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
