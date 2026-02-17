package io.github.flowentity.codec.identity;

import org.jspecify.annotations.NonNull;

import java.time.Month;
import java.time.chrono.IsoChronology;

/**
 * 身份证号码解析与格式化工具类。
 * <p>
 * 该类提供中国居民身份证号码的解析、格式化和校验功能。
 * 身份证号码由18位字符组成，包含以下信息：
 *
 * <pre>
 * 位置      长度     含义
 *  0- 5     6位     地址码（行政区划代码）
 *  6- 9     4位     出生年份
 * 10-11     2位     出生月份
 * 12-13     2位     出生日期
 * 14-16     3位     顺序码
 * 17-17     1位     校验码
 * </pre>
 *
 * <p>使用示例：
 *
 * <pre>{@code
 * // 从字符串解析身份证号码
 * IdentityNumber id = IdentityNumber.parse("110101199001011234");
 *
 * // 获取身份证各部分信息
 * int address = id.address();     // 地址码
 * int year = id.year();           // 出生年份
 * int month = id.month();         // 出生月份
 * int day = id.day();             // 出生日期
 * int sequence = id.sequence();   // 顺序码
 * char checksum = id.checksum();  // 校验码
 *
 * // 格式化生成身份证号码
 * IdentityNumber id2 = IdentityNumber.format(110101, 1990, 1, 1, 123);
 * }
 * </pre>
 *
 * @since 1.0
 */
public final class IdentityNumber {

    /**
     * 身份证号码校验码计算权重系数。
     * 对应前17位数字的加权因子，用于计算第18位校验码。
     */
    private static final int[] WEIGHTS = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};

    /**
     * 校验码映射表。
     * 根据前17位加权求和后的余数（0-10）分别对应的校验码。
     */
    private static final byte[] CHECK_CODES = {'1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2'};

    /**
     * 地址码段（第0-6位）
     */
    private static final Segment ADDRESS = new Segment("address", 0, 6, 999999);

    /**
     * 年份段（第6-10位）
     */
    private static final Segment YEAR = new Segment("year", 6, 10, 9999);

    /**
     * 月份段（第10-12位）
     */
    private static final Segment MONTH = new Segment("month", 10, 12, 12);

    /**
     * 日期段（第12-14位）
     */
    private static final Segment DAY = new Segment("day", 12, 14, 31);

    /**
     * 顺序码段（第14-17位）
     */
    private static final Segment SEQUENCE = new Segment("sequence", 14, 17, 999);

    /**
     * 身份证号码字符串表示
     */
    private final String number;

    /**
     * 身份证号码字节数组表示
     */
    private final byte[] bytes;

    /**
     * 私有构造方法，通过字符串和字节数组创建实例。
     *
     * @param number 身份证号码字符串
     * @param bytes  身份证号码字节数组
     */
    private IdentityNumber(String number, byte[] bytes) {
        this.number = number;
        this.bytes = bytes;
    }

    /**
     * 从字符串解析身份证号码。
     * <p>
     * 该方法会验证身份证号码的格式和校验码，如果验证失败将抛出
     * {@link IdentityNumberFormatException} 异常。
     *
     * @param number 身份证号码字符串，必须为18位
     * @return 解析后的 {@link IdentityNumber} 实例
     * @throws IdentityNumberFormatException 如果号码格式无效或校验码错误
     */
    public static IdentityNumber parse(@NonNull String number) {
        number = number.toUpperCase();
        byte[] bytes = number.getBytes();
        return new IdentityNumber(number, bytes).validateParse();
    }


    /**
     * 根据各字段值格式化生成身份证号码。
     * <p>
     * 该方法会根据提供的地址码、出生年月日和顺序码生成一个有效的身份证号码，
     * 并自动计算校验码。同时会验证日期的有效性（包括闰年判断）。
     *
     * @param address  地址码，6位数字（000000-999999）
     * @param year     出生年份（0000-9999）
     * @param month    出生月份（1-12）
     * @param day      出生日期（1-31，会根据月份和闰年进行有效性验证）
     * @param sequence 顺序码，3位数字（000-999）
     * @return 格式化后的 {@link IdentityNumber} 实例
     * @throws IdentityNumberFormatException 如果任何字段超出有效范围或日期无效
     */
    public static IdentityNumber format(int address, int year, int month, int day, int sequence) {
        byte[] bytes = new byte[18];
        setSegment(bytes, ADDRESS, address);
        setSegment(bytes, YEAR, year);
        setSegment(bytes, MONTH, month);
        setSegment(bytes, DAY, day);
        setSegment(bytes, SEQUENCE, sequence);
        bytes[17] = calculateChecksum(bytes);
        String number = new String(bytes);
        return new IdentityNumber(number, bytes).validateFormat();
    }

    /**
     * 获取完整的身份证号码字符串
     *
     * @return 18位身份证号码字符串
     */
    public String number() {
        return number;
    }

    /**
     * 获取地址码（行政区划代码）
     *
     * @return 6位地址码
     */
    public int address() {
        return getSegment(bytes, ADDRESS);
    }

    /**
     * 获取出生年份
     *
     * @return 出生年份（0000-9999）
     */
    public int year() {
        return getSegment(bytes, YEAR);
    }

    /**
     * 获取出生月份
     *
     * @return 出生月份（1-12）
     */
    public int month() {
        return getSegment(bytes, MONTH);
    }

    /**
     * 获取出生日期
     *
     * @return 出生日期（1-31）
     */
    public int day() {
        return getSegment(bytes, DAY);
    }

    /**
     * 获取顺序码
     *
     * @return 3位顺序码（000-999）
     */
    public int sequence() {
        return getSegment(bytes, SEQUENCE);
    }

    /**
     * 获取校验码（第18位）。
     *
     * @return 校验码字符，可能为数字或 'X'
     */
    public char checksum() {
        return (char) bytes[17];
    }

    /**
     * 判断两个 {@link IdentityNumber} 实例是否相等。
     * <p>
     * 基于身份证号码字符串进行相等性比较。
     *
     * @param obj 要比较的对象
     * @return 如果身份证号码相同则返回 true，否则返回 false
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        IdentityNumber that = (IdentityNumber) obj;
        return number.equals(that.number);
    }

    /**
     * 返回身份证号码的哈希码。
     *
     * @return 基于身份证号码字符串的哈希码
     */
    @Override
    public int hashCode() {
        return number.hashCode();
    }

    /**
     * 返回身份证号码字符串表示（已掩码）。
     * <p>
     * 为了保护个人隐私，该方法返回的身份证号码会对敏感信息进行掩码处理，
     * 格式为：前4位 + ********** + 后4位。例如：1101**********1234
     *
     * @return 掩码后的身份证号码字符串
     */
    @Override
    public @NonNull String toString() {
        return maskNumber();
    }

    /**
     * 对身份证号码进行掩码处理。
     * <p>
     * 掩码规则：保留前4位和后4位，中间10位用星号(*)替代。
     * 例如：110101199001011234 → 1101**********1234
     *
     * @return 掩码后的身份证号码
     */
    private String maskNumber() {
        StringBuilder masked = new StringBuilder(18);
        // 添加前4位
        for (int i = 0; i < 4; i++) {
            masked.append((char) bytes[i]);
        }
        // 添加10个星号作为掩码
        masked.append("**********");
        // 添加最后4位
        for (int i = 14; i < 18; i++) {
            masked.append((char) bytes[i]);
        }
        return masked.toString();
    }

    /**
     * 从字节数组中提取指定段的数值。
     *
     * @param buffer  身份证号码字节数组
     * @param segment 要提取的段信息
     * @return 该段对应的整数值
     */
    private static int getSegment(byte[] buffer, Segment segment) {
        int result = 0;
        for (int i = segment.start; i < segment.end; i++) {
            byte c = buffer[i];
            result = result * 10 + (c - '0');
        }
        return result;
    }

    /**
     * 将数值设置到字节数组的指定段位置。
     *
     * @param buffer  身份证号码字节数组
     * @param segment 目标段信息
     * @param number  要设置的数值
     * @throws IdentityNumberFormatException 如果数值超出该段的有效范围
     */
    private static void setSegment(byte[] buffer, Segment segment, int number) {
        if (number < 0 || number > segment.limit) {
            throw new IdentityNumberFormatException("Invalid " + segment.name + " format: " + number);
        }
        int cur = segment.end - 1;
        while (cur >= segment.start) {
            buffer[cur--] = (byte) ('0' + (number % 10));
            number /= 10;
        }
    }


    /**
     * 验证解析的身份证号码格式。
     * <p>
     * 检查内容包括：
     * <pre>
     * - 长度必须为18位
     * - 前17位必须为数字
     * - 校验码必须在有效范围内
     * - 校验码计算结果必须匹配
     * </pre>
     *
     * @return 验证通过返回当前实例
     * @throws IdentityNumberFormatException 如果任何验证失败
     */
    private IdentityNumber validateParse() {
        if (number.length() != 18) {
            throw new IdentityNumberFormatException("Invalid identity number length: " + number.length());
        }
        for (int i = 0; i < 17; i++) {
            if (bytes[i] < '0' || bytes[i] > '9') {
                throw new IdentityNumberFormatException("Invalid character at position " + i + ": " + bytes[i]);
            }
        }
        validateChecksumRange();
        byte expected = calculateChecksum(this.bytes);
        char checksum = checksum();
        if (checksum != expected) {
            throw new IdentityNumberFormatException("Invalid checksum: expected " + (char) expected + ", but got " + checksum);
        }
        return this;
    }

    /**
     * 验证格式化的身份证号码日期有效性。
     * <p>
     * 检查内容包括：
     * <pre>
     * - 校验码必须在有效范围内
     * - 日期必须有效（考虑闰年）
     * </pre>
     *
     * @return 验证通过返回当前实例
     * @throws IdentityNumberFormatException 如果日期无效
     */
    private IdentityNumber validateFormat() {
        validateChecksumRange();
        int year = year();
        int month = month();
        int day = day();
        if (day > 28) {
            int dom = switch (month()) {
                case 2 -> (IsoChronology.INSTANCE.isLeapYear(year) ? 29 : 28);
                case 4, 6, 9, 11 -> 30;
                default -> 31;
            };
            if (day > dom) {
                if (day == 29) {
                    throw new IdentityNumberFormatException("Invalid day format: invalid date 'February 29' as '" + year + "' is not a leap year");
                } else {
                    throw new IdentityNumberFormatException("Invalid day format: invalid date '" + Month.of(month).name() + " " + day + "'");
                }
            }
        }
        return this;
    }

    /**
     * 验证校验码是否在有效范围内。
     *
     * @throws IdentityNumberFormatException 如果校验码不是数字或 'X'
     */
    private void validateChecksumRange() {
        char checksum = checksum();
        if (checksum != 'X' && (checksum < '0' || checksum > '9')) {
            throw new IdentityNumberFormatException("Invalid checksum format: " + checksum);
        }
    }

    /**
     * 计算身份证号码的校验码。
     * <p>
     * 根据 GB 11643-1999 标准，校验码计算方法为：
     * <pre>
     * 1. 对前17位数字分别乘以对应的权重系数
     * 2. 将乘积求和
     * 3. 对11取余
     * 4. 根据余数查表得到校验码
     * </pre>
     *
     * @param bytes 身份证号码前17位的字节数组
     * @return 计算得到的校验码字符
     */
    private static byte calculateChecksum(byte[] bytes) {
        int sum = 0;
        for (int i = 0; i < 17; i++) {
            byte v = bytes[i];
            int digit = v - '0';
            sum += digit * WEIGHTS[i];
        }

        int remainder = sum % 11;
        return CHECK_CODES[remainder];
    }

    /**
     * 身份证号码字段段定义记录类。
     * <p>
     * 用于定义身份证号码中各个字段的位置和范围。
     *
     * @param name  字段名称
     * @param start 起始位置（包含）
     * @param end   结束位置（不包含）
     * @param limit 该字段的最大值
     */
    private record Segment(String name, int start, int end, int limit) {
    }
}
