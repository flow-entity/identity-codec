package io.github.nextentity.codec.identity;

import java.io.Serial;

/**
 * 加密器异常
 * <p>
 * 当加密或解密过程中发生错误时抛出此异常。
 * 包括加密算法错误、密钥错误、数据格式错误等情况。
 *
 * @version 1.0
 */
public class EncryptorException extends IdentityCodecException {

    @Serial
    private static final long serialVersionUID = 6749382019357218932L;

    public EncryptorException(String message, Throwable cause) {
        super(message, cause);
    }

    public EncryptorException(String message) {
        super(message);
    }
}
