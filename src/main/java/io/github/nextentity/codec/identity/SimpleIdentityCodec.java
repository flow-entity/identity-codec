package io.github.nextentity.codec.identity;

import org.jspecify.annotations.NonNull;

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

    public static final IdentityCodec INSTANCE = new SimpleIdentityCodec();

    /**
     * 获取单例实例
     *
     * @return SimpleIdentityCodec 单例实例
     */
    public static IdentityCodec of() {
        return INSTANCE;
    }

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
     * @throws IdentityNumberFormatException 当身份证格式不正确或出生日期超出范围时抛出
     */
    @Override
    public long encode(@NonNull IdentityNumber identityNumber) {

        // 2. 计算出生日期距离基准日期 (0000-01-01) 的天数偏移
        LocalDate birth = LocalDate.of(identityNumber.year(), identityNumber.month(), identityNumber.day());
        long daysOffset = ChronoUnit.DAYS.between(BASE_DATE, birth);

        // 3. 按位域结构组合各字段 (总共 56 位，不含校验码)
        long result = 0L;
        // 预留高位 [63-56] 保持为 0，确保兼容性
        result |= (identityNumber.address() & 0xFFFFFL) << 36; // 20 位地址码 -> [55-36] 位
        result |= (daysOffset & 0x3FFFFFL) << 14;              // 22 位生日码 -> [35-14] 位
        result |= (identityNumber.sequence() & 0x3FFL) << 4;   // 10 位顺序码 -> [13- 4] 位
        result |= (VERSION & 0xFL);                            //  4 位版本号 -> [ 3- 0] 位
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
     * @throws IdentityCodecException 当版本不支持或数据格式错误时抛出
     * @see IdentityCodec#encode(IdentityNumber)
     */
    @Override
    public @NonNull IdentityNumber decode(long encoded) {
        // 1. 提取版本号 ([3-0] 位)
        int version = (int) (encoded & 0xFL);

        // 2. 版本路由检查（当前仅支持版本1）
        if (version != 1) {
            throw new IdentityCodecException(
                    "Unsupported encoding version: " + version
            );
        }

        // 3. 校验预留位是否为0 ([63-56] 位)
        long reservedBits = (encoded >>> 56) & 0xFFL;
        if (reservedBits != 0) {
            throw new IdentityCodecException(
                    "Reserved bits must be zero, but got: " + reservedBits
            );
        }

        // 4. 按位域分别提取各字段
        int address = (int) ((encoded >>> 36) & 0xFFFFFL); // 地址码 [55-36]
        long daysOffset = (encoded >>> 14) & 0x3FFFFFL;    // 生日码 [35-14]
        int sequence = (int) ((encoded >>> 4) & 0x3FFL);   // 顺序码 [13- 4]

        // 5. 根据天数偏移计算出生日期
        LocalDate birth = BASE_DATE.plusDays(daysOffset);

        // 6. 校验年份是否为四位数（年份应在 0000-9999 范围内）
        int year = birth.getYear();
        try {
            return IdentityNumber.format(
                    address,
                    year,
                    birth.getMonthValue(),
                    birth.getDayOfMonth(),
                    sequence
            );
        } catch (IdentityNumberFormatException e) {
            throw new IdentityCodecException("Invalid identity number format.", e);
        }
    }

}
