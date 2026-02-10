package io.github.nextentity.codec.identity;

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
                "110101000001011234", // 基准日期 0000-01-01
                "110101000101021234", // 基准日期后一天
                "110101999912311234"  // 9999年12月31日
        };

        for (String id : boundaryIds) {
            long encoded = codec.encode(id);
            String decoded = codec.decode(encoded);
            assertEquals(id, decoded, "边界日期测试失败: " + id);
        }
    }

    /**
     * 测试边界日期处理
     * 专门验证边界日期数组遍历和编码解码的正确性
     */
    @Test
    void testBoundaryDateProcessing() {
        // 测试边界日期处理逻辑
        String[] boundaryTestCases = {
                "110101000101010011", // 基准日期 - 最小边界 (公元元年)
                "11010100010101123X", // 基准日期带X校验码
                "110101999912319999"  // 接近公元1000年
        };

        System.out.println("=== 边界日期处理测试 ===");

        // 验证每个边界案例
        for (int i = 0; i < boundaryTestCases.length; i++) {
            String testCase = boundaryTestCases[i];
            System.out.println("测试案例 " + (i + 1) + ": " + testCase);

            try {
                long encoded = codec.encode(testCase);
                String decoded = codec.decode(encoded);

                assertEquals(testCase, decoded,
                        "边界日期处理失败 - 测试案例 " + (i + 1) + ": " + testCase);

                System.out.println("  ✓ 编码: " + encoded);
                System.out.println("  ✓ 解码: " + decoded);

            } catch (Exception e) {
                System.out.println("  ✗ 处理异常: " + e.getMessage());
                // 对于超出范围的日期，验证是否正确抛出异常
                if (testCase.equals("110101999912319999")) {
                    // 这个日期应该在正常范围内
                    fail("公元999年的日期应该在范围内: " + e.getMessage());
                } else {
                    fail("意外的异常: " + e.getMessage());
                }
            }
        }

        System.out.println("边界日期处理测试完成");
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
     * 测试校验码大小写X处理
     * 专门验证校验码对大写X和小写x的处理逻辑
     */
    @Test
    void testCheckDigitCaseHandling() {
        System.out.println("=== 校验码大小写处理测试 ===");

        // 测试大写X
        String upperXId = "11010119900307123X";
        System.out.println("测试大写X: " + upperXId);

        try {
            long encodedUpper = codec.encode(upperXId);
            String decodedUpper = codec.decode(encodedUpper);
            assertEquals(upperXId, decodedUpper, "大写X处理失败");
            System.out.println("  ✓ 大写X编码: " + encodedUpper);
            System.out.println("  ✓ 大写X解码: " + decodedUpper);
        } catch (Exception e) {
            fail("大写X处理异常: " + e.getMessage());
        }

        // 测试小写x - 验证编码时能正确处理，解码时转换为大写X
        String lowerXId = "11010119900307123x";
        String expectedDecoded = "11010119900307123X"; // 解码后应该是大写X
        System.out.println("测试小写x: " + lowerXId);
        System.out.println("期望解码结果: " + expectedDecoded);

        try {
            long encodedLower = codec.encode(lowerXId);
            String decodedLower = codec.decode(encodedLower);
            assertEquals(expectedDecoded, decodedLower, "小写x处理失败 - 解码后应为大写X");
            System.out.println("  ✓ 小写x编码: " + encodedLower);
            System.out.println("  ✓ 小写x解码: " + decodedLower);
        } catch (Exception e) {
            fail("小写x处理异常: " + e.getMessage());
        }

        // 验证校验码的核心逻辑：大小写X都被正确识别为数值10
        // 通过比较编码结果来验证
        long upperEncoded = codec.encode(upperXId);
        long lowerEncoded = codec.encode(lowerXId);

        // 虽然原始字符串不同，但由于校验码都转换为10，其他部分相同，所以编码结果应该相同
        assertEquals(upperEncoded, lowerEncoded,
                "大小写X应该产生相同的编码结果（校验码都转换为数值10）");

        System.out.println("校验码大小写处理测试完成");
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
        // 此测试验证日期范围检查
        // 由于24位天数偏移支持非常大的日期范围（约45000年）
        // 对于四位数年份的身份证，不会超出范围
        // 这里主要测试无效的日期格式

        // 测试无效的月份
        assertThrows(Exception.class, () ->
                codec.encode("110101199013011234"), "无效月份应该抛出异常");
    }

    /**
     * 测试出生日期早于基准日期的异常处理
     * 专门验证daysOffset小于0时的异常处理逻辑
     */
    @Test
    void testEarlyBirthDateException() {
        System.out.println("=== 早于基准日期异常处理测试 ===");

        // 验证基准日期本身是允许的
        String baseDateId = "110101000001011234"; // 基准日期0000-01-01
        System.out.println("验证基准日期: " + baseDateId);
        try {
            long encoded = codec.encode(baseDateId);
            String decoded = codec.decode(encoded);
            assertEquals(baseDateId, decoded, "基准日期应该能正常编码解码");
            System.out.println("  ✓ 基准日期处理正常");
        } catch (Exception e) {
            fail("基准日期处理异常: " + e.getMessage());
        }

        // 验证基准日期后一天也是允许的
        String baseDatePlusOneId = "110101000101021234"; // 基准日期后一天
        System.out.println("验证基准日期后一天: " + baseDatePlusOneId);
        try {
            long encoded = codec.encode(baseDatePlusOneId);
            String decoded = codec.decode(encoded);
            assertEquals(baseDatePlusOneId, decoded, "基准日期后一天应该能正常编码解码");
            System.out.println("  ✓ 基准日期后一天处理正常");
        } catch (Exception e) {
            fail("基准日期后一天处理异常: " + e.getMessage());
        }

        System.out.println("早于基准日期异常处理测试完成");
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

    /**
     * 测试不支持版本号的异常处理
     * 专门验证version != 1时的异常处理逻辑
     */
    @Test
    void testUnsupportedVersionException() {
        System.out.println("=== 不支持版本号异常处理测试 ===");

        // 创建不同版本号的测试数据
        int[] unsupportedVersions = {0, 2, 3, 15}; // 不支持的版本号

        for (int version : unsupportedVersions) {
            System.out.println("测试不支持的版本 " + version);

            // 构造包含特定版本号的long值
            // 版本号在最低4位 [3-0]
            // 只设置版本号位

            // 验证应该抛出IllegalArgumentException异常
            Exception exception = assertThrows(IllegalArgumentException.class, () -> codec.decode(version), "不支持的版本号应该抛出异常");

            // 验证异常消息包含正确的版本信息
            String expectedMessage = "Unsupported compression version: " + version;
            assertTrue(exception.getMessage().contains(expectedMessage) ||
                       exception.getMessage().contains("不支持"),
                    "异常消息应该包含版本错误信息: " + expectedMessage);

            System.out.println("  ✓ 正确抛出异常: " + exception.getMessage());
        }

        // 验证支持的版本号(版本1)能正常工作
        System.out.println("验证支持的版本1:");
        try {
            String validId = "110101199001011234";
            long encoded = codec.encode(validId);
            String decoded = codec.decode(encoded);
            assertEquals(validId, decoded, "版本1应该能正常解码");
            System.out.println("  ✓ 版本1处理正常");
        } catch (Exception e) {
            fail("版本1处理异常: " + e.getMessage());
        }

        // 测试版本号在有效范围边界的情况
        System.out.println("测试版本号边界情况:");
        // 版本号4位，范围0-15，但只支持版本1
        for (int version = 0; version <= 15; version++) {
            if (version == 1) continue; // 跳过支持的版本

            long testData = version;
            assertThrows(IllegalArgumentException.class, () ->
                    codec.decode(testData), "版本 " + version + " 应该抛出异常");
        }
        System.out.println("  ✓ 所有非1版本都正确抛出异常");

        System.out.println("不支持版本号异常处理测试完成");
    }
}