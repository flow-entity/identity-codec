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
     * 构造函数
     *
     * @param message 异常信息
     */
    public InvalidIdentityNumberException(String message) {
        super(message);
    }

}
