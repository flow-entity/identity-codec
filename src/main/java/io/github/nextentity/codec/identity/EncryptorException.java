package io.github.nextentity.codec.identity;

import java.io.Serial;

/**
 * 加密器异常
 * <p>
 * 当加密或解密过程中发生错误时抛出此异常。
 *
 * @version 1.0
 */
public class EncryptorException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 6749382019357218932L;

    /**
     * 构造函数（带原因）
     *
     * @param message 异常信息
     * @param cause   异常原因
     */
    public EncryptorException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 构造函数
     *
     * @param message 异常信息
     */
    public EncryptorException(String message) {
        super(message);
    }
}
