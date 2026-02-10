package io.github.nextentity.codec.identity;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * 简单身份编码器
 * 将 18 位身份证号码压缩为 64 位 long 类型
 * <pre>
 * 位域分配 (共 60 位):
 * [63-60]: 预留位 (4 位) - 保持为 0
 * [59-40]: 地址码 (20 位) - 行政区划代码
 * [39-18]: 天数偏移 (22 位) - 距离基准日期的天数
 * [17-8]:  顺序码 (10 位) - 同日出生人员序号
 * [3-0]:   版本号 (4 位) - 编码版本标识
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
     * 3. 按预定义的位域结构组合成 60 位有效数据的 long 值（不存储校验码）
     * </pre>
     *
     * @param identityNumber 18 位身份证号码字符串
     * @return 编码后的 long 值（使用 60 位，高位预留4位为 0）
     * @throws InvalidIdentityNumberException 当身份证格式不正确或出生日期超出范围时抛出
     * @see #decode(long)
     */
    @Override
    public long encode(String identityNumber) {
        IdentityCardUtils.validate(identityNumber);
        // 1. 解析 18 位身份证号码各组成部分（不存储校验码）
        int administrativeCode = Integer.parseInt(identityNumber.substring(0, 6)); // 6 位地址码
        int birthYear = Integer.parseInt(identityNumber.substring(6, 10)); // 4 位年份
        int birthMonth = Integer.parseInt(identityNumber.substring(10, 12)); // 2 位月份
        int birthDay = Integer.parseInt(identityNumber.substring(12, 14)); // 2 位日期
        int sequenceNumber = Integer.parseInt(identityNumber.substring(14, 17)); // 3 位顺序码

        // 2. 计算出生日期距离基准日期 (0000-01-01) 的天数偏移
        LocalDate birthDate = LocalDate.of(birthYear, birthMonth, birthDay);
        long daysOffset = ChronoUnit.DAYS.between(BASE_DATE, birthDate);

        // 3. 按位域结构组合各字段 (总共 60 位，不含校验码)
        long encodedResult = 0L;
        // 预留高位 [63-60] 保持为 0，确保兼容性
        encodedResult |= ((long) administrativeCode & 0xFFFFFL) << 40; // 20 位地址码 -> [59-40] 位
        encodedResult |= (daysOffset & 0x3FFFFFL) << 18; // 22 位天数偏移 -> [39-18] 位
        encodedResult |= ((long) sequenceNumber & 0x3FFL) << 8; // 10 位顺序码 -> [17-8] 位
        encodedResult |= (VERSION & 0xFL); // 4 位版本号 -> [3-0] 位 (最低位)
        return encodedResult;
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

        // 2. Version routing check (currently only supports version 1)
        if (version != 1) {
            throw new InvalidEncodingException(InvalidEncodingException.ErrorCode.UNSUPPORTED_VERSION, String.valueOf(version));
        }

        // 3. 校验预留位是否为0 ([63-60] 位)
        long reservedBits = (encoded >>> 60) & 0xFL;
        if (reservedBits != 0) {
            throw new InvalidEncodingException(InvalidEncodingException.ErrorCode.RESERVED_BITS_NOT_ZERO, String.valueOf(reservedBits));
        }

        // 4. 按位域分别提取各字段（不包含校验码）
        int administrativeCode = (int) ((encoded >>> 40) & 0xFFFFFL); // 提取20位地址码 [59-40]
        long daysOffset = (encoded >>> 18) & 0x3FFFFFL; // 提取22位天数 [39-18]
        int sequenceNumber = (int) ((encoded >>> 8) & 0x3FFL); // 提取10位顺序码 [17-8]

        // 5. 根据天数偏移计算出生日期
        LocalDate birthDate = BASE_DATE.plusDays(daysOffset);

        // 6. 校验年份是否为四位数（年份应在 0000-9999 范围内）
        int birthYear = birthDate.getYear();
        if (birthYear < 0 || birthYear > 9999) {
            throw new InvalidEncodingException(InvalidEncodingException.ErrorCode.INVALID_BIT_FIELD, "Birth year out of range: " + birthYear);
        }

        // 7. 组装前17位，然后计算校验码
        String first17Chars = String.format("%06d%04d%02d%02d%03d",
                administrativeCode, // 6位地址码
                birthDate.getYear(), // 4位年份
                birthDate.getMonthValue(), // 2位月份
                birthDate.getDayOfMonth(), // 2位日期
                sequenceNumber); // 3位顺序码

        // 8. 返回完整的18位身份证号码
        return IdentityCardUtils.appendCheckCode(first17Chars);
    }
}
