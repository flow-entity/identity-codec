package io.github.nextentity.codec.identity;

import java.io.Serial;

/**
 * 无效身份证号码异常
 * <p>
 * 当身份证号码格式不正确或校验码验证失败时抛出此异常。
 *
 * @version 1.0
 */
public class InvalidIdentityNumberException extends IdentityCodecException {

    @Serial
    private static final long serialVersionUID = 4270343855622804394L;

    public InvalidIdentityNumberException(String message) {
        super(message);
    }

    public InvalidIdentityNumberException(ErrorCode errorCode, String detail) {
        super(errorCode, detail);
    }

    public InvalidIdentityNumberException(ErrorCode errorCode, String detail, Throwable cause) {
        super(errorCode, detail, cause);
    }

    public InvalidIdentityNumberException(String message, Throwable cause) {
        super(message, cause);
    }
}
