package io.github.nextentity.codec.identity;

import org.jspecify.annotations.NonNull;

import java.nio.charset.StandardCharsets;
import java.time.DateTimeException;
import java.time.Month;
import java.time.chrono.IsoChronology;
import java.time.temporal.ChronoField;

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
    }

    /**
     * 计算18位身份证号码的校验码
     *
     * @param identityNumber 18位身份证号码或前17位（前17位有效，第18位会被忽略）
     * @return 计算得到的校验码字符（'0'-'9' 或 'X'）
     * @throws InvalidIdentityNumberException 当身份证号码格式不正确时抛出
     */
    public static char calculateCheckCode(@NonNull String identityNumber) {
        byte[] chars = identityNumber.getBytes(StandardCharsets.ISO_8859_1);
        return calculateCheckCode(chars);
    }

    /**
     * 计算18位身份证号码的校验码（字节数组版本）
     *
     * @param chars 身份证号码字节数组，长度必须为17或18字节
     * @return 计算得到的校验码字符（'0'-'9' 或 'X'）
     * @throws InvalidIdentityNumberException 当身份证号码格式不正确时抛出
     */
    public static char calculateCheckCode(byte[] chars) {
        if (chars.length < 17 || chars.length > 18) {
            throw new InvalidIdentityNumberException(
                    ErrorCode.INVALID_ID_LENGTH,
                    "Input length must be 17 or 18"
            );
        }

        int sum = 0;
        for (int i = 0; i < 17; i++) {
            int digit = chars[i] - '0';
            if (digit < 0 || digit > 9) {
                throw new InvalidIdentityNumberException(
                        ErrorCode.INVALID_CHARACTER,
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
    public static boolean isValid(@NonNull String identityNumber) {
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
    public static void validate(@NonNull String identityNumber) {
        byte[] bytes = identityNumber.getBytes(StandardCharsets.ISO_8859_1);
        validate(bytes);
    }

    /**
     * 验证身份证号码的校验码，如果不正确则抛出异常（字节数组版本）
     *
     * @param bytes 身份证号码字节数组，长度必须为18字节
     * @throws InvalidIdentityNumberException 当身份证号码格式不正确或校验码不正确时抛出
     */
    public static void validate(byte[] bytes) {
        if (bytes.length != 18) {
            throw new InvalidIdentityNumberException(
                    ErrorCode.INVALID_ID_LENGTH,
                    "ID number must be exactly 18 digits"
            );
        }

        char expected = calculateCheckCode(bytes);
        char actual = Character.toUpperCase((char) (bytes[17] & 0xFF));

        if (expected != actual) {
            throw new InvalidIdentityNumberException(
                    ErrorCode.INVALID_CHECK_CODE,
                    String.format("Expected '%c', actual '%c'", expected, actual)
            );
        }

        try {
            int year = parseInt(bytes, 6, 10);
            int month = parseInt(bytes, 10, 12);
            int day = parseInt(bytes, 12, 14);

            ChronoField.YEAR.checkValidValue(year);
            ChronoField.MONTH_OF_YEAR.checkValidValue(month);
            ChronoField.DAY_OF_MONTH.checkValidValue(day);

            if (day > 28) {
                int dom = switch (month) {
                    case 2 -> (IsoChronology.INSTANCE.isLeapYear(year) ? 29 : 28);
                    case 4, 6, 9, 11 -> 30;
                    default -> 31;
                };
                if (day > dom) {
                    if (day == 29) {
                        throw new DateTimeException("Invalid date 'February 29' as '" + year + "' is not a leap year");
                    } else {
                        throw new DateTimeException("Invalid date '" + Month.of(month).name() + " " + day + "'");
                    }
                }
            }

        } catch (DateTimeException e) {
            throw new InvalidIdentityNumberException(
                    ErrorCode.INVALID_DATE,
                    "Invalid birth date value",
                    e
            );
        }
    }

    /**
     * 将字节数组中指定范围的数字字符解析为整数
     *
     * @param str   包含数字字符的字节数组
     * @param start 起始位置（包含）
     * @param end   结束位置（不包含）
     * @return 解析得到的整数值
     */
    static int parseInt(byte[] str, int start, int end) {
        int result = 0;
        for (int i = start; i < end; i++) {
            result = result * 10 + (str[i] - '0');
        }
        return result;
    }
}
