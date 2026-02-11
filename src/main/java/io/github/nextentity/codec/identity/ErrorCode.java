package io.github.nextentity.codec.identity;

/**
 * 身份编码错误码枚举
 * <p>
 * 统一定义所有与身份证编码、解码、加密、解密相关的错误码。
 * 错误码格式：[类别]-[序号]，如 IIN-001、ENC-001、IDC-001
 *
 * @version 1.0
 */
public enum ErrorCode {

    // ========== 身份证号码错误码 (IIN: Identity ID Number) ==========
    /**
     * 无效的身份证号码长度
     */
    INVALID_ID_LENGTH("IIN-001", "Invalid ID number length"),
    /**
     * 无效的校验码
     */
    INVALID_CHECK_CODE("IIN-002", "Invalid check code"),
    /**
     * 身份证号码中包含无效字符
     */
    INVALID_CHARACTER("IIN-003", "Invalid character in ID number"),
    /**
     * 无效的出生日期
     */
    INVALID_DATE("IIN-004", "Invalid birth date"),

    // ========== 加密器错误码 (ENC: Encryption) ==========
    /**
     * 加密过程中发生错误
     */
    ENCRYPTION_ERROR("ENC-001", "Encryption error occurred"),
    /**
     * 解密过程中发生错误
     */
    DECRYPTION_ERROR("ENC-002", "Decryption error occurred"),
    /**
     * 无效的密钥
     */
    INVALID_KEY("ENC-003", "Invalid encryption key"),
    /**
     * 加密算法未初始化
     */
    ALGORITHM_NOT_INITIALIZED("ENC-004", "Encryption algorithm not initialized"),

    // ========== 编码器错误码 (IDC: Identity Codec) ==========
    /**
     * 不支持的压缩版本
     */
    UNSUPPORTED_VERSION("IDC-001", "Unsupported compression version"),
    /**
     * 预留位必须为零
     */
    RESERVED_BITS_NOT_ZERO("IDC-002", "Reserved bits must be zero"),
    /**
     * 无效的位域提取
     */
    INVALID_BIT_FIELD("IDC-003", "Invalid bit field extraction"),
    /**
     * 无效的长度
     */
    INVALID_LENGTH("IDC-004", "Invalid length"),
    ;

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
