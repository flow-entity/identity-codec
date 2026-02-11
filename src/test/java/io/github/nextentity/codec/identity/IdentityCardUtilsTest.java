package io.github.nextentity.codec.identity;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;

/**
 * IdentityCheckCodeCalculator 测试类
 * 测试身份证号码校验码计算功能
 */
public class IdentityCardUtilsTest {

    private static final Logger logger = LoggerFactory.getLogger(IdentityCardUtilsTest.class);

    /**
     * 测试校验码验证功能
     */
    @Test
    void testIsValidCheckCode() {
        // 正确的身份证号码
        String validId = "11010519491231002X";
        assertTrue(IdentityCardUtils.isValid(validId),
                "正确的身份证号码应该验证通过");

        // 错误的校验码（将 X 改为 1）
        String invalidId = "110105194912310021";
        assertFalse(IdentityCardUtils.isValid(invalidId),
                "错误的校验码应该验证失败");

        // 错误的校验码（将 X 改为 1）
        String invalidLenId = "1101051949123100211";
        assertFalse(IdentityCardUtils.isValid(invalidLenId),
                "长度错误的身份证号码应该验证失败");
    }

    /**
     * 测试日期校验功能
     */
    @Test
    void testDateValidation() {
        // 非闰年2月29日（应该失败）
        String nonLeapYearFirst17 = "11010519990229002";
        String nonLeapYearId = nonLeapYearFirst17 + IdentityCardUtils.calculateCheckCode(nonLeapYearFirst17);
        assertFalse(IdentityCardUtils.isValid(nonLeapYearId),
                "非闰年2月29日应该验证失败: " + nonLeapYearId);

        // 4月31日（应该失败）
        String invalidDayFirst17 = "11010519900431002";
        String invalidDayId = invalidDayFirst17 + IdentityCardUtils.calculateCheckCode(invalidDayFirst17);
        assertFalse(IdentityCardUtils.isValid(invalidDayId),
                "4月31日应该验证失败: " + invalidDayId);

        // 无效月份
        String invalidMonthFirst17 = "11010519901301002";
        String invalidMonthId = invalidMonthFirst17 + IdentityCardUtils.calculateCheckCode(invalidMonthFirst17);
        assertFalse(IdentityCardUtils.isValid(invalidMonthId),
                "13月应该验证失败: " + invalidMonthId);


        // 无效月份
        String invalidMonthFirst17_2 = "11010519900001002";
        String invalidMonthId_2 = invalidMonthFirst17_2 + IdentityCardUtils.calculateCheckCode(invalidMonthFirst17_2);
        assertFalse(IdentityCardUtils.isValid(invalidMonthId_2),
                "00月应该验证失败: " + invalidMonthId_2);

        // 无效的日
        String zeroDayFirst17 = "11010519901200002";
        String zeroDayId = zeroDayFirst17 + IdentityCardUtils.calculateCheckCode(zeroDayFirst17);
        assertFalse(IdentityCardUtils.isValid(zeroDayId),
                "0日应该验证失败: " + zeroDayId);
    }

    /**
     * 测试校验码验证失败时抛出异常的功能
     */
    @Test
    void testValidate() {
        // 正确的身份证号码应该不抛异常
        String validId = "11010519491231002X";
        assertDoesNotThrow(() -> IdentityCardUtils.validate(validId),
                "正确的身份证号码不应该抛出异常");

        // 错误的校验码应该抛出异常
        String invalidId = "110105194912310021";
        Exception exception = assertThrows(InvalidIdentityNumberException.class,
                () -> IdentityCardUtils.validate(invalidId),
                "错误的校验码应该抛出异常");

        assertTrue(exception.getMessage().contains("IIN-002") || exception.getMessage().contains("check code is invalid"),
                "异常消息应该包含校验码错误信息: " + exception.getMessage());

        logger.info("校验失败异常消息: {}", exception.getMessage());
    }

    /**
     * 测试小写 x 的处理
     */
    @Test
    void testLowerCaseX() {
        String idWithUpperX = "11010519491231002X";
        String idWithLowerX = "11010519491231002x";

        // 小写 x 也应该验证通过
        assertTrue(IdentityCardUtils.isValid(idWithLowerX),
                "小写 x 也应该验证通过");

        // 计算结果应该都是 X（大写）
        assertEquals('X', IdentityCardUtils.calculateCheckCode(idWithUpperX));
        assertEquals('X', IdentityCardUtils.calculateCheckCode(idWithLowerX));
    }

    /**
     * 测试非法输入 - 长度不正确
     */
    @Test
    void testInvalidLength() {
        // 长度小于17
        assertThrows(InvalidIdentityNumberException.class, () ->
                IdentityCardUtils.calculateCheckCode("1234567890123456"));

        assertThrows(InvalidIdentityNumberException.class, () ->
                IdentityCardUtils.calculateCheckCode("1234567890123456789"));
    }

    /**
     * 测试非法字符
     */
    @Test
    void testInvalidCharacters() {
        // 前17位包含非数字字符
        assertThrows(InvalidIdentityNumberException.class, () ->
                IdentityCardUtils.calculateCheckCode("110105194912310A2X"));
    }

    /**
     * 测试各种校验码值
     */
    @Test
    void testAllCheckCodeValues() {
        // 测试能生成所有可能的校验码（0-9 和 X）
        // 使用 generateCompleteId 生成正确的身份证号码，确保校验码正确
        String[] first17List = {
                "11010119000101003", // 应该生成校验码 0
                "11010119000101008", // 应该生成校验码 1
                "11010119000101002", // 应该生成校验码 2
                "11010119000101007", // 应该生成校验码 3
                "11010119000101001", // 应该生成校验码 4
                "11010119000101006", // 应该生成校验码 5
                "11010119000101000", // 应该生成校验码 6
                "11010119000101005", // 应该生成校验码 7
                "11010119000101018", // 应该生成校验码 8
                "11010119000101004", // 应该生成校验码 9
                "11010119000101009", // 应该生成校验码 X
        };

        // 测试所有校验码字符
        char[] expectedCodes = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'X'};

        for (int i = 0; i < first17List.length; i++) {
            String first17Chars = first17List[i];
            char calculated = IdentityCardUtils.calculateCheckCode(first17Chars);
            assertEquals(expectedCodes[i], calculated,
                    "生成的身份证号码校验码计算错误: " + first17Chars);
        }
    }
}
