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
    INVALID_ID_LENGTH("IIN-001", "无效的身份证号码长度"),
    /**
     * 无效的校验码
     */
    INVALID_CHECK_CODE("IIN-002", "无效的校验码"),
    /**
     * 身份证号码中包含无效字符
     */
    INVALID_CHARACTER("IIN-003", "身份证号码中包含无效字符"),
    /**
     * 无效的出生日期
     */
    INVALID_DATE("IIN-004", "无效的出生日期"),

    // ========== 加密器错误码 (ENC: Encryption) ==========
    /**
     * 加密过程中发生错误
     */
    ENCRYPTION_ERROR("ENC-001", "加密过程中发生错误"),
    /**
     * 解密过程中发生错误
     */
    DECRYPTION_ERROR("ENC-002", "解密过程中发生错误"),
    /**
     * 无效的密钥
     */
    INVALID_KEY("ENC-003", "无效的密钥"),
    /**
     * 加密算法未初始化
     */
    ALGORITHM_NOT_INITIALIZED("ENC-004", "加密算法未初始化"),

    // ========== 编码器错误码 (IDC: Identity Codec) ==========
    /**
     * 不支持的编码版本
     */
    UNSUPPORTED_VERSION("IDC-001", "不支持的编码版本"),
    /**
     * 预留位必须为零
     */
    RESERVED_BITS_NOT_ZERO("IDC-002", "预留位必须为零"),
    /**
     * 无效的位域提取
     */
    INVALID_BIT_FIELD("IDC-003", "无效的位域提取"),
    /**
     * 加密失败
     */
    ENCRYPTION_FAILED("IDC-004", "加密失败"),
    /**
     * 解密失败
     */
    DECRYPTION_FAILED("IDC-005", "解密失败");

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

    /**
     * 根据错误码字符串查找对应的 ErrorCode
     *
     * @param code 错误码字符串
     * @return 对应的 ErrorCode，如果未找到则返回 null
     */
    public static ErrorCode fromCode(String code) {
        for (ErrorCode errorCode : values()) {
            if (errorCode.code.equals(code)) {
                return errorCode;
            }
        }
        return null;
    }
}
