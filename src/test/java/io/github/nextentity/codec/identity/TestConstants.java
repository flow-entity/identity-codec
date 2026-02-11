package io.github.nextentity.codec.identity;

/**
 * 测试常量类 - 统一管理测试中使用的常量数据
 * 避免重复的硬编码值，提高测试维护性
 */
public final class TestConstants {

    private TestConstants() {
        // 工具类，禁止实例化
    }

    // ==================== 身份证号码常量 ====================
    
    /**
     * 常用的有效身份证号码（包含X校验码）
     */
    public static final String VALID_ID_CARD_WITH_X = "11010519491231002X";
    
    /**
     * 常用的有效身份证号码（数字校验码）
     */
    public static final String VALID_ID_CARD_NUMERIC = "110101199001011237";
    
    /**
     * 包含小写x的身份证号码（用于测试大小写处理）
     */
    public static final String VALID_ID_CARD_LOWER_X = "11010519900307109x";
    
    /**
     * 上海地区的身份证号码
     */
    public static final String SHANGHAI_ID_CARD = "310101198506152345";
    
    /**
     * 广州地区的身份证号码
     */
    public static final String GUANGZHOU_ID_CARD = "440101198012123455";
    
    /**
     * 成都地区的身份证号码
     */
    public static final String CHENGDU_ID_CARD = "510101197503214566";
    
    /**
     * 1900年出生的身份证号码
     */
    public static final String YEAR_1900_ID_CARD = "110101190001011236";
    
    /**
     * 1950年出生的身份证号码
     */
    public static final String YEAR_1950_ID_CARD = "110101195006152348";
    
    /**
     * 2000年出生的身份证号码
     */
    public static final String YEAR_2000_ID_CARD = "110101200012123459";
    
    /**
     * 2023年出生的身份证号码
     */
    public static final String YEAR_2023_ID_CARD = "110101202303214560";
    
    /**
     * 基准日期的身份证号码（0000-01-01）
     */
    public static final String BASE_DATE_ID_CARD = "110101000001011236";
    
    /**
     * 基准日期后一天的身份证号码
     */
    public static final String BASE_DATE_PLUS_ONE_ID_CARD = "110101000101021239";
    
    /**
     * 最大日期的身份证号码（9999-12-31）
     */
    public static final String MAX_DATE_ID_CARD = "110101999912311236";
    
    // ==================== 测试数据数组 ====================
    
    /**
     * 常用的有效身份证号码数组
     */
    public static final String[] VALID_ID_CARDS = {
            VALID_ID_CARD_WITH_X,
            VALID_ID_CARD_NUMERIC,
            SHANGHAI_ID_CARD,
            GUANGZHOU_ID_CARD,
            CHENGDU_ID_CARD
    };
    
    /**
     * 不同年份的身份证号码数组
     */
    public static final String[] DIFFERENT_YEAR_ID_CARDS = {
            YEAR_1900_ID_CARD,
            YEAR_1950_ID_CARD,
            YEAR_2000_ID_CARD,
            YEAR_2023_ID_CARD
    };
    
    /**
     * 边界日期的身份证号码数组
     */
    public static final String[] BOUNDARY_DATE_ID_CARDS = {
            BASE_DATE_ID_CARD,
            BASE_DATE_PLUS_ONE_ID_CARD,
            MAX_DATE_ID_CARD
    };
    
    /**
     * 性能测试使用的身份证号码数组
     */
    public static final String[] PERFORMANCE_TEST_ID_CARDS = {
            VALID_ID_CARD_WITH_X,
            VALID_ID_CARD_NUMERIC,
            SHANGHAI_ID_CARD,
            GUANGZHOU_ID_CARD,
            CHENGDU_ID_CARD
    };
    
    // ==================== 加密密钥常量 ====================
    
    /**
     * 默认的SPECK64加密密钥（128位）
     */
    public static final int[] DEFAULT_ENCRYPTION_KEY = {
            0x01234567, 0x89ABCDEF, 0xFEDCBA98, 0x76543210
    };
    
    /**
     * 替代的SPECK64加密密钥1
     */
    public static final int[] ALTERNATIVE_ENCRYPTION_KEY_1 = {
            0x11111111, 0x22222222, 0x33333333, 0x44444444
    };
    
    /**
     * 替代的SPECK64加密密钥2
     */
    public static final int[] ALTERNATIVE_ENCRYPTION_KEY_2 = {
            0xFFFFFFFF, 0xEEEEEEEE, 0xDDDDDDDD, 0xCCCCCCCC
    };
    
    /**
     * 默认密钥对应的字节数组（小端字节序）
     */
    public static final byte[] DEFAULT_ENCRYPTION_KEY_BYTES = {
            0x67, 0x45, 0x23, 0x01,  // 0x01234567 的小端字节序
            (byte) 0xEF, (byte) 0xCD, (byte) 0xAB, (byte) 0x89,  // 0x89ABCDEF 的小端字节序
            (byte) 0x98, (byte) 0xBA, (byte) 0xDC, (byte) 0xFE,  // 0xFEDCBA98 的小端字节序
            0x10, 0x32, 0x54, 0x76   // 0x76543210 的小端字节序
    };
    
    // ==================== 错误测试数据 ====================
    
    /**
     * 无效长度的身份证号码（过短）
     */
    public static final String TOO_SHORT_ID = "1234567890123456"; // 16位
    
    /**
     * 无效长度的身份证号码（过长）
     */
    public static final String TOO_LONG_ID = "12345678901234567890"; // 20位
    
    /**
     * 包含非法字符的身份证号码
     */
    public static final String INVALID_CHAR_ID = "1101011990010112A"; // 包含字母A
    
    /**
     * 无效月份的身份证号码
     */
    public static final String INVALID_MONTH_ID = "110101199013011234"; // 月份13
    
    /**
     * 无效校验码的身份证号码
     */
    public static final String INVALID_CHECK_CODE_ID = "110101199001011236"; // 假设正确校验码是7
    
    // ==================== 边界值常量 ====================
    
    /**
     * 长数据类型的边界值数组（用于加密测试）
     */
    public static final long[] BOUNDARY_LONG_VALUES = {
            0L,                          // 最小值
            -1L,                         // 全1 (0xFFFFFFFFFFFFFFFF)
            Long.MAX_VALUE,              // 0x7FFFFFFFFFFFFFFF
            Long.MIN_VALUE,              // 0x8000000000000000
            0x0000000000000001L,         // 只有最低位为1
            0x8000000000000000L,         // 只有最高位为1
            0x5555555555555555L,         // 交替位
            0xAAAAAAAAAAAAAAAAL          // 交替位（反）
    };
    
    // ==================== 错误码常量 ====================
    
    /**
     * InvalidIdentityNumberException的错误码数组
     */
    public static final InvalidIdentityNumberException.ErrorCode[] ID_NUMBER_ERROR_CODES = {
            InvalidIdentityNumberException.ErrorCode.INVALID_LENGTH,
            InvalidIdentityNumberException.ErrorCode.INVALID_CHECK_CODE,
            InvalidIdentityNumberException.ErrorCode.INVALID_CHARACTER,
            InvalidIdentityNumberException.ErrorCode.INVALID_DATE
    };
    
    /**
     * InvalidEncodingException的错误码数组
     */
    public static final InvalidEncodingException.ErrorCode[] ENCODING_ERROR_CODES = {
            InvalidEncodingException.ErrorCode.UNSUPPORTED_VERSION,
            InvalidEncodingException.ErrorCode.RESERVED_BITS_NOT_ZERO,
            InvalidEncodingException.ErrorCode.INVALID_BIT_FIELD
    };
    
    // ==================== 性能测试常量 ====================
    
    /**
     * 性能测试的迭代次数
     */
    public static final int PERFORMANCE_ITERATIONS = 1000000;
    
    /**
     * 性能测试的时间限制（毫秒）
     */
    public static final long PERFORMANCE_TIMEOUT_MS = 5000L;
    
    /**
     * 线程测试的线程数量
     */
    public static final int THREAD_COUNT = 10;
    
    /**
     * 线程测试的迭代次数
     */
    public static final int THREAD_ITERATIONS = 1000;
}