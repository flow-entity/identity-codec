package io.github.nextentity.codec.identity;

import java.nio.charset.StandardCharsets;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.Objects;

/**
 * 身份证号码工具类
 * <p>
 * 根据国家标准 GB 11643-1999，18位身份证号码的最后一位是校验码，
 * 通过前17位数字按照特定算法计算得出。
 *
 * @version 1.0
 */
public class IdentityCardUtils {

    /**
     * 身份证号码校验码计算权重系数
     * 对应前17位数字的加权因子
     */
    private static final int[] WEIGHTS = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};

    /**
     * 校验码映射表
     * 余数 0-10 分别对应的校验码
     */
    private static final char[] CHECK_CODES = {'1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2'};

    /**
     * 私有构造方法，防止实例化
     */
    private IdentityCardUtils() {
        // 工具类，禁止实例化
    }

    /**
     * 计算18位身份证号码的校验码
     *
     * @param identityNumber 18位身份证号码或前17位（前17位有效，第18位会被忽略）
     * @return 计算得到的校验码字符（'0'-'9' 或 'X'）
     * @throws InvalidIdentityNumberException 当身份证号码格式不正确时抛出
     */
    public static char calculateCheckCode(String identityNumber) {
        byte[] chars = Objects.requireNonNull(identityNumber).getBytes(StandardCharsets.ISO_8859_1);
        return calculateCheckCode(chars);
    }

    public static char calculateCheckCode(byte[] chars) {
        if (chars.length < 17 || chars.length > 18) {
            throw new InvalidIdentityNumberException(InvalidIdentityNumberException.ErrorCode.INVALID_LENGTH, "Input length must be 17 or 18");
        }

        int sum = 0;
        for (int i = 0; i < 17; i++) {
            int digit = chars[i] - '0';
            if (digit < 0 || digit > 9) {
                throw new InvalidIdentityNumberException(InvalidIdentityNumberException.ErrorCode.INVALID_CHARACTER,
                        "Non-numeric character at position " + i + ": " + chars[i]);
            }
            sum += digit * WEIGHTS[i];
        }

        int remainder = sum % 11;
        return CHECK_CODES[remainder];
    }

    /**
     * 验证身份证号码的校验码是否正确
     *
     * @param identityNumber 18位身份证号码
     * @return true 如果校验码正确，false 否则
     */
    public static boolean isValid(String identityNumber) {
        try {
            validate(identityNumber);
            return true;
        } catch (InvalidIdentityNumberException | NullPointerException e) {
            return false;
        }
    }

    /**
     * 验证身份证号码的校验码，如果不正确则抛出异常
     *
     * @param identityNumber 18位身份证号码
     * @throws InvalidIdentityNumberException 当身份证号码格式不正确或校验码不正确时抛出
     */
    public static void validate(String identityNumber) {
        byte[] bytes = Objects.requireNonNull(identityNumber).getBytes(StandardCharsets.ISO_8859_1);
        validate(bytes);
    }

    public static void validate(byte[] bytes) {
        if (bytes.length != 18) {
            throw new InvalidIdentityNumberException(InvalidIdentityNumberException.ErrorCode.INVALID_LENGTH, "ID number must be exactly 18 digits");
        }

        char expected = calculateCheckCode(bytes);
        char actual = Character.toUpperCase((char) (bytes[17] & 0xFF));

        if (expected != actual) {
            throw new InvalidIdentityNumberException(InvalidIdentityNumberException.ErrorCode.INVALID_CHECK_CODE,
                    String.format("Expected '%c', actual '%c'", expected, actual));
        }

        try {
            int year = parseInt(bytes, 6, 10);
            int month = parseInt(bytes, 10, 12);
            int day = parseInt(bytes, 12, 14);

            var _ = LocalDate.of(year, month, day);

        } catch (DateTimeException e) {
            throw new InvalidIdentityNumberException(InvalidIdentityNumberException.ErrorCode.INVALID_DATE,
                    "Invalid birth date value", e);
        }
    }

    public static int parseInt(byte[] str, int start, int end) {
        int result = 0;
        for (int i = start; i < end; i++) {
            result = result * 10 + (str[i] - '0');
        }
        return result;
    }

    /**
     * 根据前17位数字生成完整的18位身份证号码（包含计算出的校验码）
     *
     * @param first17Chars 身份证号码前17位数字
     * @return 完整的18位身份证号码
     * @throws InvalidIdentityNumberException 当输入格式不正确时抛出
     */
    public static String appendCheckCode(String first17Chars) {
        if (first17Chars == null || first17Chars.length() != 17) {
            throw new InvalidIdentityNumberException(InvalidIdentityNumberException.ErrorCode.INVALID_LENGTH,
                    "Input must be exactly 17 digits");
        }

        // 临时拼接一个占位符作为第18位，用于调用 calculate 方法
        char checkCode = calculateCheckCode(first17Chars);

        return first17Chars + checkCode;
    }
}
