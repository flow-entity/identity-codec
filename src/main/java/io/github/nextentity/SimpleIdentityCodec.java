package io.github.nextentity;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * 简单身份编码器
 * 将 18 位身份证号码压缩为 64 位 long 类型
 * <pre>
 * 位域分配 (共 56 位):
 * [63-56]: 预留位 (8 位) - 保持为 0
 * [55-36]: 地址码 (20 位) - 行政区划代码
 * [35-18]: 天数偏移 (18 位) - 距离基准日期的天数
 * [17-8]:  顺序码 (10 位) - 同日出生人员序号
 * [7-4]:   校验码 (4 位) - 身份证校验位
 * [3-0]:   版本号 (4 位) - 编码版本标识
 * </pre>
 */
public class SimpleIdentityCodec implements IdentityCodec {
    private static final LocalDate BASE_DATE = LocalDate.of(1900, 1, 1);
    private static final int VERSION = 1; // 4 位版本号，范围 0-15

    /**
     * 将 18 位身份证号码编码为 long 类型
     * <pre>
     * 编码过程：
     * 1. 解析身份证各组成部分（地址码、出生日期、顺序码、校验码）
     * 2. 计算出生日期距离基准日期的天数偏移
     * 3. 按预定义的位域结构组合成 56 位有效数据的 long 值
     * </pre>
     * 
     * @param identityNumber 18 位身份证号码字符串
     * @return 编码后的 long 值（使用 56 位，高位预留8位为 0）
     * @throws IllegalArgumentException 当身份证格式不正确或出生日期超出范围时抛出
     * @see #decode(long)
     */
    @Override
    public long encode(String identityNumber) {
        // 1. 解析 18 位身份证号码各组成部分
        int address = Integer.parseInt(identityNumber.substring(0, 6)); // 6 位地址码
        int year = Integer.parseInt(identityNumber.substring(6, 10)); // 4 位年份
        int month = Integer.parseInt(identityNumber.substring(10, 12)); // 2 位月份
        int day = Integer.parseInt(identityNumber.substring(12, 14)); // 2 位日期
        int sequence = Integer.parseInt(identityNumber.substring(14, 17)); // 3 位顺序码
        char checkChar = identityNumber.charAt(17); // 1 位校验码
        int check = (checkChar == 'X' || checkChar == 'x') ? 10 : (checkChar - '0');

        // 2. 计算出生日期距离基准日期 (1900-01-01) 的天数偏移
        LocalDate birthDate = LocalDate.of(year, month, day);
        long days = ChronoUnit.DAYS.between(BASE_DATE, birthDate);
        if (days < 0 || days >= (1L << 18)) {
            throw new IllegalArgumentException("Birth date out of range");
        }

        // 3. 按位域结构组合各字段 (总共 56 位)
        long result = 0L;
        // 预留高位 [63-56] 保持为 0，确保兼容性
        result |= ((long) address & 0xFFFFFL) << 36; // 20 位地址码 -> [55-36] 位
        result |= (days & 0x3FFFFL) << 18; // 18 位天数偏移 -> [35-18] 位
        result |= ((long) sequence & 0x3FFL) << 8; // 10 位顺序码 -> [17-8] 位
        result |= ((long) check & 0xFL) << 4; // 4 位校验码 -> [7-4] 位
        result |= (VERSION & 0xFL); // 4 位版本号 -> [3-0] 位 (最低位)
        return result;
    }

    /**
     * 将编码后的 long 值解码为 18 位身份证号码
     * <pre>
     * 解码过程：
     * 1. 提取版本号并进行版本检查
     * 2. 按位域分别提取各字段（地址码、天数、顺序码、校验码）
     * 3. 根据天数偏移计算出生日期
     * 4. 组装成标准的18位身份证号码格式
     * </pre>
     * 
     * @param encoded 编码后的 long 值
     * @return 18 位身份证号码字符串
     * @throws IllegalArgumentException 当版本不支持或数据格式错误时抛出
     * @see #encode(String)
     */
    @Override
    public String decode(long encoded) {
        // 1. 提取版本号 ([3-0] 位)
        int version = (int) (encoded & 0xFL);

        // 2. Version routing check (currently only supports version 1)
        if (version != 1) {
            throw new IllegalArgumentException("Unsupported compression version: " + version);
        }

        // 3. 按位域分别提取各字段
        int address = (int) ((encoded >>> 36) & 0xFFFFFL); // 提取20位地址码 [55-36]
        long days = (encoded >>> 18) & 0x3FFFFL; // 提取18位天数 [35-18]
        int sequence = (int) ((encoded >>> 8) & 0x3FFL); // 提取10位顺序码 [17-8]
        int check = (int) ((encoded >>> 4) & 0xFL); // 提取4位校验码 [7-4]
        String checkChar = check == 10 ? "X" : String.valueOf(check);

        // 4. 根据天数偏移计算出生日期
        LocalDate birthDate = BASE_DATE.plusDays(days);

        // 5. 组装并返回18位身份证号码
        return String.format("%06d%04d%02d%02d%03d%s",
                address, // 6位地址码
                birthDate.getYear(), // 4位年份
                birthDate.getMonthValue(), // 2位月份
                birthDate.getDayOfMonth(), // 2位日期
                sequence, // 3位顺序码
                checkChar); // 1位校验码
    }
}
