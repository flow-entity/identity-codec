package io.github.nextentity.codec.identity;

import org.jspecify.annotations.NonNull;

import java.time.Month;
import java.time.chrono.IsoChronology;

public final class IdentityNumber {

    /**
     * 身份证号码校验码计算权重系数
     * 对应前17位数字的加权因子
     */
    private static final int[] WEIGHTS = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};

    /**
     * 校验码映射表
     * 余数 0-10 分别对应的校验码
     */
    private static final byte[] CHECK_CODES = {'1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2'};

    private static final Segment ADDRESS = new Segment("address", 0, 6);
    private static final Segment YEAR = new Segment("year", 6, 10);
    private static final Segment MONTH = new Segment("month", 10, 12, 12);
    private static final Segment DAY = new Segment("day", 12, 14, 31);
    private static final Segment SEQUENCE = new Segment("sequence", 14, 17);

    private final String number;
    private final byte[] bytes;


    private IdentityNumber(String number, byte[] bytes) {
        this.number = number;
        this.bytes = bytes;
    }

    public static IdentityNumber parse(@NonNull String number) {
        number = number.toUpperCase();
        byte[] bytes = number.getBytes();
        return new IdentityNumber(number, bytes).validateParse();
    }


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

    public char checksum() {
        return (char) bytes[17];
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        IdentityNumber that = (IdentityNumber) obj;
        return number.equals(that.number);
    }

    @Override
    public int hashCode() {
        return number.hashCode();
    }

    @Override
    public @NonNull String toString() {
        return number;
    }

    private static int getSegment(byte[] buffer, Segment segment) {
        int result = 0;
        for (int i = segment.start; i < segment.end; i++) {
            byte c = buffer[i];
            result = result * 10 + (c - '0');
        }
        return result;
    }

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

    private void validateChecksumRange() {
        char checksum = checksum();
        if (checksum != 'X' && (checksum < '0' || checksum > '9')) {
            throw new IdentityNumberFormatException("Invalid checksum format: " + checksum);
        }
    }

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

    private record Segment(String name, int start, int end, int limit) {
        private Segment(String name, int start, int end) {
            this(name, start, end, (int) Math.pow(10, end - start) - 1);
        }
    }
}
