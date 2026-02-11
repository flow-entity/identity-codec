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

    private final String number;
    private final byte[] bytes;
    private final int address;
    private final short year;
    private final byte month;
    private final byte day;
    private final short sequence;


    private IdentityNumber(String number, byte[] bytes, int address, short year, byte month, byte day, short sequence) {
        this.number = number;
        this.bytes = bytes;
        this.address = address;
        this.year = year;
        this.month = month;
        this.day = day;
        this.sequence = sequence;
    }

    public static IdentityNumber parse(@NonNull String number) {
        if (number.length() != 18) {
            throw new IdentityNumberFormatException("Invalid identity number length: " + number.length());
        }
        number = number.toUpperCase();
        byte[] bytes = number.getBytes();
        int address = parse(bytes, 0, 6);
        int year = parse(bytes, 6, 10);
        int month = parse(bytes, 10, 12);
        int day = parse(bytes, 12, 14);
        int sequence = parse(bytes, 14, 17);
        return new IdentityNumber(number, bytes, address, (short) year, (byte) month, (byte) day, (short) sequence).validateParse();
    }


    public static IdentityNumber format(int address, short year, byte month, byte day, short sequence) {
        byte[] bytes = new byte[18];
        format(bytes, address, 0, 6);
        format(bytes, year, 6, 4);
        format(bytes, month, 10, 2);
        format(bytes, day, 12, 2);
        format(bytes, sequence, 14, 3);
        bytes[17] = calculateChecksum(bytes);
        String number = new String(bytes);

        return new IdentityNumber(number, bytes, address, year, month, day, sequence).validateFormat();
    }

    public String number() {
        return number;
    }

    public int address() {
        return address;
    }

    public short year() {
        return year;
    }

    public byte month() {
        return month;
    }

    public byte day() {
        return day;
    }

    public short sequence() {
        return sequence;
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

    private static int parse(byte[] number, int start, int end) {
        int result = 0;
        for (int i = start; i < end; i++) {
            byte c = number[i];
            result = result * 10 + (c - '0');
        }
        return result;
    }

    private static void format(byte[] buffer, int number, int offset, int width) {
        int pos = offset + width - 1;
        while (pos >= offset) {
            buffer[pos--] = (byte) ('0' + (number % 10));
            number /= 10;
        }
    }


    private IdentityNumber validateParse() {
        for (int i = 0; i < 17; i++) {
            if (bytes[i] < '0' || bytes[i] > '9') {
                throw new IdentityNumberFormatException("Invalid character at position " + i + ": " + bytes[i]);
            }
        }
        byte checksum = bytes[17];
        if (checksum != 'X' && (checksum < '0' || checksum > '9')) {
            throw new IdentityNumberFormatException("Invalid checksum character: " + (char) checksum);
        }
        byte[] bytes = this.bytes;
        byte expected = calculateChecksum(bytes);
        if (bytes[17] != expected) {
            throw new IdentityNumberFormatException("Invalid checksum: expected " + (char) expected + ", but got " + (char)checksum);
        }
        return this;
    }

    private IdentityNumber validateFormat() {
        if (address < 0 || address > 999999) {
            throw new IdentityNumberFormatException("Invalid address format: " + address);
        }
        if (year < 0 || year > 9999) {
            throw new IdentityNumberFormatException("Invalid year format: " + year);
        }
        if (month < 0 || month > 12) {
            throw new IdentityNumberFormatException("Invalid month format: " + month);
        }
        if (day < 0 || day > 31) {
            throw new IdentityNumberFormatException("Invalid day format: " + day);
        }
        if (day > 28) {
            int dom = switch (month) {
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
        if (sequence < 0 || sequence > 999) {
            throw new IdentityNumberFormatException("Invalid sequence format: " + sequence);
        }
        byte checksum = bytes[17];
        if (checksum != 'X' && (checksum < '0' || checksum > '9')) {
            throw new IdentityNumberFormatException("Invalid checksum format: " + checksum);
        }

        return this;
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
}
