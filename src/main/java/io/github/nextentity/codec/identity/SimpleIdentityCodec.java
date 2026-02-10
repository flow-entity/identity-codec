package io.github.nextentity.codec.identity;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * 简单身份编码器
 * 将 18 位身份证号码压缩为 64 位 long 类型
 * <pre>
 * 位域分配 (共 56 位):
 * [63-56]: 预留位 ( 8 位) - 保持为 0
 * [55-36]: 地址码 (20 位) - 行政区划代码
 * [35-14]: 生日码 (22 位) - 距离基准日期的天数
 * [13- 4]: 顺序码 (10 位) - 同日出生人员序号
 * [ 3- 0]: 版本号 ( 4 位) - 编码版本标识
 * </pre>
 *
 * @version 1.0
 */
public class SimpleIdentityCodec implements IdentityCodec {
    /**
     * 基准日期：0000年1月1日
     */
    private static final LocalDate BASE_DATE = LocalDate.of(0, 1, 1);
    /**
     * 编码版本号
     */
    private static final int VERSION = 1;

    /**
     * 将 18 位身份证号码编码为 long 类型
     * <pre>
     * 编码过程：
     * 1. 解析身份证各组成部分（地址码、出生日期、顺序码）
     * 2. 计算出生日期距离基准日期的天数偏移
     * 3. 按预定义的位域结构组合成 56 位有效数据的 long 值（不存储校验码）
     * </pre>
     *
     * @param identityNumber 18 位身份证号码字符串
     * @return 编码后的 long 值（使用 56 位，高位预留8位为 0）
     * @throws InvalidIdentityNumberException 当身份证格式不正确或出生日期超出范围时抛出
     * @see #decode(long)
     */
    @Override
    public long encode(String identityNumber) {
        byte[] buffer = identityNumber.getBytes(StandardCharsets.ISO_8859_1);
        IdentityCardUtils.validate(buffer);
        // 1. 解析 18 位身份证号码各组成部分（不存储校验码）
        int administrativeCode = IdentityCardUtils.parseInt(buffer, 0, 6); // 6 位地址码
        int year = IdentityCardUtils.parseInt(buffer, 6, 10); // 4 位年份
        int month = IdentityCardUtils.parseInt(buffer, 10, 12); // 2 位月份
        int day = IdentityCardUtils.parseInt(buffer, 12, 14); // 2 位日期
        int sequenceNumber = IdentityCardUtils.parseInt(buffer, 14, 17); // 3 位顺序码

        // 2. 计算出生日期距离基准日期 (0000-01-01) 的天数偏移
        LocalDate birth = LocalDate.of(year, month, day);
        long daysOffset = ChronoUnit.DAYS.between(BASE_DATE, birth);

        // 3. 按位域结构组合各字段 (总共 56 位，不含校验码)
        long result = 0L;
        // 预留高位 [63-56] 保持为 0，确保兼容性
        result |= ((long) administrativeCode & 0xFFFFFL) << 36; // 20 位地址码 -> [55-36] 位
        result |= (daysOffset & 0x3FFFFFL) << 14;               // 22 位生日码 -> [35-14] 位
        result |= ((long) sequenceNumber & 0x3FFL) << 4;        // 10 位顺序码 -> [13- 4] 位
        result |= (VERSION & 0xFL);                             //  4 位版本号 -> [ 3- 0] 位
        return result;
    }

    /**
     * 将编码后的 long 值解码为 18 位身份证号码
     * <pre>
     * 解码过程：
     * 1. 提取版本号并进行版本检查
     * 2. 按位域分别提取各字段（地址码、天数、顺序码）
     * 3. 根据天数偏移计算出生日期
     * 4. 计算校验码
     * 5. 组装成标准的18位身份证号码格式
     * </pre>
     *
     * @param encoded 编码后的 long 值
     * @return 18 位身份证号码字符串
     * @throws InvalidEncodingException 当版本不支持或数据格式错误时抛出
     * @see #encode(String)
     */
    @Override
    public String decode(long encoded) {
        // 1. 提取版本号 ([3-0] 位)
        int version = (int) (encoded & 0xFL);

        // 2. 版本路由检查（当前仅支持版本1）
        if (version != 1) {
            throw new InvalidEncodingException(InvalidEncodingException.ErrorCode.UNSUPPORTED_VERSION, String.valueOf(version));
        }

        // 3. 校验预留位是否为0 ([63-56] 位)
        long reservedBits = (encoded >>> 56) & 0xFF;
        if (reservedBits != 0) {
            throw new InvalidEncodingException(InvalidEncodingException.ErrorCode.RESERVED_BITS_NOT_ZERO, String.valueOf(reservedBits));
        }

        // 4. 按位域分别提取各字段（不包含校验码）
        int administrativeCode = (int) ((encoded >>> 36) & 0xFFFFFL); // 提取20位地址码 [55-36]
        long daysOffset = (encoded >>> 14) & 0x3FFFFFL; // 提取22位天数 [35-14]
        int sequenceNumber = (int) ((encoded >>> 4) & 0x3FFL); // 提取10位顺序码 [13-4]

        // 5. 根据天数偏移计算出生日期
        LocalDate birth = BASE_DATE.plusDays(daysOffset);

        // 6. 校验年份是否为四位数（年份应在 0000-9999 范围内）
        int year = birth.getYear();
        if (year < 0 || year > 9999) {
            throw new InvalidEncodingException(InvalidEncodingException.ErrorCode.INVALID_BIT_FIELD, "Birth year out of range: " + year);
        }

        // 7. 使用优化的方法组装前17位，避免 String.format() 开销
        byte[] buffer = new byte[18];
        
        // 直接填充字符数组，避免字符串格式化开销
        appendNumber(buffer, administrativeCode, 0, 6);  // 6位地址码
        appendNumber(buffer, birth.getYear(), 6, 4); // 4位年份
        appendNumber(buffer, birth.getMonthValue(), 10, 2);  // 2位月份
        appendNumber(buffer, birth.getDayOfMonth(), 12, 2);  // 2位日期
        appendNumber(buffer, sequenceNumber, 14, 3);     // 3位顺序码

        // 8. 转换为字节数组并计算校验码
        char checkCode = IdentityCardUtils.calculateCheckCode(buffer);
        buffer[17] = (byte) checkCode;
        
        // 9. 返回完整的18位身份证号码
        return new String(buffer, StandardCharsets.ISO_8859_1);
    }
    
    /**
     * 将数字追加到字符数组指定位置
     * 使用高效的数字转字符串算法
     *
     * @param buffer 字符数组缓冲区
     * @param number 要转换的数字
     * @param offset 起始位置
     * @param width  固定宽度（不足补0）
     */
    private void appendNumber(byte[] buffer, int number, int offset, int width) {
        int pos = offset + width - 1;
        while (pos >= offset) {
            buffer[pos--] = (byte) ('0' + (number % 10));
            number /= 10;
        }
    }
}
