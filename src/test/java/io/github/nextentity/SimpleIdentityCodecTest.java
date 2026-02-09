package io.github.nextentity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SimpleIdentityCodec 测试类
 * 测试身份证编码器的编码和解码功能
 */
public class SimpleIdentityCodecTest {

    private SimpleIdentityCodec codec;

    @BeforeEach
    void setUp() {
        codec = new SimpleIdentityCodec();
    }

    /**
     * 测试基本的编码解码功能
     */
    @Test
    void testEncodeDecodeBasic() {
        String idCard = "110105194912310021"; // 示例身份证号码

        // 编码
        long encoded = codec.encode(idCard);
        System.out.println("原始身份证: " + idCard);
        System.out.println("编码结果: " + encoded);
        System.out.println("二进制表示: " + Long.toBinaryString(encoded));

        // 解码
        String decoded = codec.decode(encoded);
        System.out.println("解码结果: " + decoded);

        // 验证
        assertEquals(idCard, decoded, "编码解码后应该与原始身份证号码一致");
    }

    /**
     * 测试不同地区的身份证号码
     */
    @Test
    void testDifferentRegions() {
        String[] testIds = {
                "110101199001011234", // 北京市东城区
                "310101198506152345", // 上海市黄浦区
                "440101198012123456", // 广州市越秀区
                "510101197503214567"  // 成都市锦江区
        };

        for (String id : testIds) {
            long encoded = codec.encode(id);
            String decoded = codec.decode(encoded);
            assertEquals(id, decoded, "地区测试失败: " + id);
        }
    }

    /**
     * 测试不同的出生年份
     */
    @Test
    void testDifferentBirthYears() {
        String[] testIds = {
                "110101190001011234", // 1900年
                "110101195006152345", // 1950年
                "110101200012123456", // 2000年
                "110101202303214567"  // 2023年
        };

        for (String id : testIds) {
            long encoded = codec.encode(id);
            String decoded = codec.decode(encoded);
            assertEquals(id, decoded, "年份测试失败: " + id);
        }
    }

    /**
     * 测试边界日期
     */
    @Test
    void testBoundaryDates() {
        // 测试基准日期附近
        String[] boundaryIds = {
                "110101190001011234", // 基准日期 1900-01-01
                "110101190001021234", // 基准日期后一天
                "110101241301011234"  // 接近最大值 (1900 + 2^18天 ≈ 2413年)
        };

        for (String id : boundaryIds) {
            long encoded = codec.encode(id);
            String decoded = codec.decode(encoded);
            assertEquals(id, decoded, "边界日期测试失败: " + id);
        }
    }

    /**
     * 测试校验码包含X的情况
     */
    @Test
    void testCheckCodeWithX() {
        String idWithX = "11010119900307123X";
        long encoded = codec.encode(idWithX);
        String decoded = codec.decode(encoded);
        assertEquals(idWithX, decoded, "校验码X测试失败");
    }

    /**
     * 测试顺序码的不同值
     */
    @Test
    void testDifferentSequenceCodes() {
        String[] sequenceIds = {
                "110101199001010011", // 顺序码 001
                "110101199001011234", // 顺序码 123
                "110101199001019999"  // 顺序码 999
        };

        for (String id : sequenceIds) {
            long encoded = codec.encode(id);
            String decoded = codec.decode(encoded);
            assertEquals(id, decoded, "顺序码测试失败: " + id);
        }
    }

    /**
     * 测试异常情况 - 无效的身份证格式
     */
    @Test
    void testInvalidIdFormat() {
        // 测试无效月份会抛出异常
        assertThrows(Exception.class, () -> {
            codec.encode("110101199013011234");  // 无效月份13
        }, "无效月份应该抛出异常");

        // 非法字符测试比较复杂，因为charAt(17)是'A'会被parseInt处理
        // 长度检查需要在实际应用中添加，当前实现没有做长度验证
    }

    /**
     * 测试异常情况 - 出生日期超出范围
     */
    @Test
    void testBirthDateOutOfRange() {
        // 18位天数的最大值约为 262144 天 ≈ 718年
        // 从1900年开始约到2618年
        String futureId = "110101261901011234"; // 超出范围的未来日期

        assertThrows(IllegalArgumentException.class, () ->
                codec.encode(futureId), "超出日期范围应该抛出异常");
    }

    /**
     * 测试位域分配正确性
     */
    @Test
    void testBitFieldAllocation() {
        String idCard = "110105194912310021";
        long encoded = codec.encode(idCard);

        // 验证版本号在最低4位
        int version = (int) (encoded & 0xFL);
        assertEquals(1, version, "版本号应该是1");

        // 验证校验码在[7-4]位
        int check = (int) ((encoded >>> 4) & 0xFL);
        assertEquals(1, check, "校验码提取错误");

        // 验证顺序码在[17-8]位
        int sequence = (int) ((encoded >>> 8) & 0x3FFL);
        assertEquals(2, sequence, "顺序码提取错误");

        System.out.println("位域分配验证通过");
    }

    /**
     * 性能测试 - 批量编码解码
     */
    @Test
    void testBatchPerformance() {
        String[] testIds = {
                "110101199001011234",
                "110101198506152345",
                "110101200012123456",
                "110101197503214567",
                "110101196008155678"
        };

        long startTime = System.nanoTime();

        for (int i = 0; i < 1000; i++) {
            for (String id : testIds) {
                long encoded = codec.encode(id);
                String decoded = codec.decode(encoded);
                assertEquals(id, decoded);
            }
        }

        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1_000_000; // 转换为毫秒

        System.out.println("批量性能测试完成，耗时: " + duration + " ms");
        assertTrue(duration < 5000, "性能测试应该在5秒内完成");
    }

    /**
     * 测试反向工程 - 验证long值的唯一性
     */
    @Test
    void testUniqueness() {
        String id1 = "110101199001011234";
        String id2 = "110101199001011235"; // 只差一位

        long encoded1 = codec.encode(id1);
        long encoded2 = codec.encode(id2);

        assertNotEquals(encoded1, encoded2, "不同的身份证应该产生不同的编码");

        // 验证同一个身份证多次编码结果一致
        long encoded1Again = codec.encode(id1);
        assertEquals(encoded1, encoded1Again, "同一身份证多次编码应该一致");
    }
}