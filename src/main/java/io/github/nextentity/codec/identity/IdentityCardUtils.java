package io.github.nextentity.codec.identity;

import java.time.DateTimeException;
import java.time.LocalDate;

/**
 * 身份证号码工具类
 * <p>
 * 根据国家标准 GB 11643-1999，18位身份证号码的最后一位是校验码，
 * 通过前17位数字按照特定算法计算得出。
 * </p>
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
        if (identityNumber == null || identityNumber.length() < 17 || identityNumber.length() > 18) {
            throw new InvalidIdentityNumberException("Input number length must be 17 or 18");
        }

        // 计算前17位数字的加权和
        int sum = 0;
        for (int i = 0; i < 17; i++) {
            char c = identityNumber.charAt(i);
            int digit = c - '0';
            if (digit < 0 || digit > 9) {
                throw new InvalidIdentityNumberException(
                        "The first 17 digits of ID number must be numeric, illegal character at position: " + i);
            }
            sum += digit * WEIGHTS[i];
        }

        // 计算余数并查表得到校验码
        int remainder = sum % 11;
        return CHECK_CODES[remainder];
    }

    /**
     * 验证身份证号码的校验码是否正确
     *
     * @param identityNumber 18位身份证号码
     * @return true 如果校验码正确，false 否则
     * @throws InvalidIdentityNumberException 当身份证号码格式不正确时抛出
     */
    public static boolean isValid(String identityNumber) {
        try {
            validate(identityNumber);
            return true;
        } catch (InvalidIdentityNumberException e) {
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
        if (identityNumber == null || identityNumber.length() != 18) {
            throw new InvalidIdentityNumberException("ID number must be 18 digits");
        }

        // 1. 校验码验证
        char expected = calculateCheckCode(identityNumber);
        char actual = Character.toUpperCase(identityNumber.charAt(17));

        if (expected != actual) {
            throw new InvalidIdentityNumberException(String.format(
                    "ID number check code is invalid: expected '%c', actual '%c'",
                    expected,
                    actual
            ));
        }

        // 2. 日期格式和有效性验证
        try {
            int year = Integer.parseInt(identityNumber.substring(6, 10));
            int month = Integer.parseInt(identityNumber.substring(10, 12));
            int day = Integer.parseInt(identityNumber.substring(12, 14));

            if (month < 1 || month > 12) {
                throw new InvalidIdentityNumberException("ID number birth month is invalid");
            }
            if (day < 1 || day > 31) {
                throw new InvalidIdentityNumberException("ID number birth day is invalid");
            }

            // 详细日期验证（包括闰年、每月天数等）
            var _ = LocalDate.of(year, month, day);

        } catch (NumberFormatException | DateTimeException e) {
            throw new InvalidIdentityNumberException("ID number birth date is invalid", e);
        }
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
            throw new InvalidIdentityNumberException("Input must be 17 digits");
        }

        // 临时拼接一个占位符作为第18位，用于调用 calculate 方法
        char checkCode = calculateCheckCode(first17Chars);

        return first17Chars + checkCode;
    }
}
