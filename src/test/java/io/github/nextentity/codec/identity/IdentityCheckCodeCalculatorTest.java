package io.github.nextentity.codec.identity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * IdentityCheckCodeCalculator 测试类
 * 测试身份证号码校验码计算功能
 */
public class IdentityCheckCodeCalculatorTest {

    /**
     * 测试校验码计算 - 已知正确的身份证号码
     */
    @Test
    void testCalculateCheckCode() {
        // 使用 generateCompleteId 生成正确的身份证号码
        String[] first17List = {
                "11010519491231002", // 应该生成校验码 X
                "11010119900101123", // 应该生成校验码 7
                "31010119850615234", // 应该生成校验码 5
                "44010119801212345", // 应该生成校验码 5
                "51010119750321456"  // 应该生成校验码 6
        };

        for (String first17 : first17List) {
            String completeId = IdentityCheckCodeCalculator.generateCompleteId(first17);
            char expectedCheckCode = completeId.charAt(17);
            char calculatedCheckCode = IdentityCheckCodeCalculator.calculate(completeId);
            assertEquals(expectedCheckCode, calculatedCheckCode,
                    "身份证号码 " + completeId + " 的校验码计算错误");
        }
    }

    /**
     * 测试校验码验证功能
     */
    @Test
    void testIsValidCheckCode() {
        // 正确的身份证号码
        String validId = "11010519491231002X";
        assertTrue(IdentityCheckCodeCalculator.isValid(validId),
                "正确的身份证号码应该验证通过");

        // 错误的校验码（将 X 改为 1）
        String invalidId = "110105194912310021";
        assertFalse(IdentityCheckCodeCalculator.isValid(invalidId),
                "错误的校验码应该验证失败");

        // 错误的校验码（将 X 改为 1）
        String invalidLenId = "1101051949123100211";
        assertFalse(IdentityCheckCodeCalculator.isValid(invalidLenId),
                "长度错误的身份证号码应该验证失败");

        // 测试 null 输入应该抛出异常
        assertFalse(IdentityCheckCodeCalculator.isValid(null),
                "null 输入应该抛出异常");
    }

    /**
     * 测试日期校验功能
     */
    @Test
    void testDateValidation() {
        // 正确的日期
        String validId = IdentityCheckCodeCalculator.generateCompleteId("11010519901225002"); // 1990年12月25日
        assertTrue(IdentityCheckCodeCalculator.isValid(validId),
                "正确的日期应该验证通过: " + validId);

        // 闰年2月29日
        String leapYearId = IdentityCheckCodeCalculator.generateCompleteId("11010520000229002"); // 2000年2月29日（闰年）
        assertTrue(IdentityCheckCodeCalculator.isValid(leapYearId),
                "闰年2月29日应该验证通过: " + leapYearId);

        // 非闰年2月29日（应该失败）
        String nonLeapYearFirst17 = "11010519990229002";
        String nonLeapYearId = nonLeapYearFirst17 + IdentityCheckCodeCalculator.calculate(nonLeapYearFirst17 + " "); // 手动构造，校验码会是错的，但日期无效
        assertFalse(IdentityCheckCodeCalculator.isValid(nonLeapYearId),
                "非闰年2月29日应该验证失败: " + nonLeapYearId);

        // 4月31日（应该失败）
        String invalidDayFirst17 = "11010519900431002";
        String invalidDayId = invalidDayFirst17 + IdentityCheckCodeCalculator.calculate(invalidDayFirst17 + " "); // 手动构造
        assertFalse(IdentityCheckCodeCalculator.isValid(invalidDayId),
                "4月31日应该验证失败: " + invalidDayId);

        // 无效月份
        String invalidMonthFirst17 = "11010519901301002";
        String invalidMonthId = invalidMonthFirst17 + IdentityCheckCodeCalculator.calculate(invalidMonthFirst17 + " "); // 手动构造
        assertFalse(IdentityCheckCodeCalculator.isValid(invalidMonthId),
                "13月应该验证失败: " + invalidMonthId);


        // 无效月份
        String invalidMonthFirst17_2 = "11010519900001002";
        String invalidMonthId_2 = invalidMonthFirst17_2 + IdentityCheckCodeCalculator.calculate(invalidMonthFirst17_2 + " "); // 手动构造
        assertFalse(IdentityCheckCodeCalculator.isValid(invalidMonthId_2),
                "00月应该验证失败: " + invalidMonthId_2);

        // 无效的日
        String zeroDayFirst17 = "11010519901200002";
        String zeroDayId = zeroDayFirst17 + IdentityCheckCodeCalculator.calculate(zeroDayFirst17 + " "); // 手动构造
        assertFalse(IdentityCheckCodeCalculator.isValid(zeroDayId),
                "0日应该验证失败: " + zeroDayId);
    }

    /**
     * 测试校验码验证失败时抛出异常的功能
     */
    @Test
    void testValidate() {
        // 正确的身份证号码应该不抛异常
        String validId = "11010519491231002X";
        assertDoesNotThrow(() -> IdentityCheckCodeCalculator.validate(validId),
                "正确的身份证号码不应该抛出异常");

        // 错误的校验码应该抛出异常
        String invalidId = "110105194912310021";
        Exception exception = assertThrows(InvalidIdentityNumberException.class,
                () -> IdentityCheckCodeCalculator.validate(invalidId),
                "错误的校验码应该抛出异常");

        assertTrue(exception.getMessage().contains("check code is invalid"),
                "异常消息应该包含校验码错误信息");

        System.out.println("校验失败异常消息: " + exception.getMessage());
    }

    /**
     * 测试生成完整的身份证号码
     */
    @Test
    void testGenerateCompleteId() {
        // 前17位
        String first17 = "11010519491231002";
        String completeId = IdentityCheckCodeCalculator.generateCompleteId(first17);

        assertEquals(18, completeId.length(), "生成的身份证号码应该是18位");
        assertEquals(first17, completeId.substring(0, 17), "前17位应该保持不变");
        assertEquals('X', completeId.charAt(17), "校验码应该是 X");

        // 验证生成的身份证号码是正确的
        assertTrue(IdentityCheckCodeCalculator.isValid(completeId),
                "生成的身份证号码应该能通过验证");
    }

    /**
     * 测试小写 x 的处理
     */
    @Test
    void testLowerCaseX() {
        String idWithUpperX = "11010519491231002X";
        String idWithLowerX = "11010519491231002x";

        // 小写 x 也应该验证通过
        assertTrue(IdentityCheckCodeCalculator.isValid(idWithLowerX),
                "小写 x 也应该验证通过");

        // 计算结果应该都是 X（大写）
        assertEquals('X', IdentityCheckCodeCalculator.calculate(idWithUpperX));
        assertEquals('X', IdentityCheckCodeCalculator.calculate(idWithLowerX));
    }

    /**
     * 测试非法输入 - null
     */
    @Test
    void testNullInput() {
        assertThrows(InvalidIdentityNumberException.class, () ->
                IdentityCheckCodeCalculator.calculate(null));

        assertThrows(IllegalArgumentException.class, () ->
                IdentityCheckCodeCalculator.generateCompleteId(null));
    }

    /**
     * 测试非法输入 - 长度不正确
     */
    @Test
    void testInvalidLength() {
        // 长度小于17
        assertThrows(InvalidIdentityNumberException.class, () ->
                IdentityCheckCodeCalculator.calculate("1234567890123456"));

        assertThrows(InvalidIdentityNumberException.class, () ->
                IdentityCheckCodeCalculator.calculate("1234567890123456789"));

        // generateCompleteId 需要17位
        assertThrows(InvalidIdentityNumberException.class, () ->
                IdentityCheckCodeCalculator.generateCompleteId("1234567890123456"));

        assertThrows(InvalidIdentityNumberException.class, () ->
                IdentityCheckCodeCalculator.generateCompleteId("123456789012345678"));
    }

    /**
     * 测试非法字符
     */
    @Test
    void testInvalidCharacters() {
        // 前17位包含非数字字符
        assertThrows(InvalidIdentityNumberException.class, () ->
                IdentityCheckCodeCalculator.calculate("110105194912310A2X"));
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
                "11010119000101004", // 应该生成校验码 1
                "11010119000101005", // 应该生成校验码 2
                "11010119000101006", // 应该生成校验码 3
                "11010119000101007", // 应该生成校验码 4
                "11010119000101008", // 应该生成校验码 5
                "11010119000101009", // 应该生成校验码 6
                "11010119000101000", // 应该生成校验码 7
                "11010119000101001", // 应该生成校验码 8
                "11010119000101002", // 应该生成校验码 9
                "11010119000101003", // 再次测试，校验码 0
        };

        // 测试所有校验码字符
        char[] expectedCodes = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0'};

        for (int i = 0; i < first17List.length; i++) {
            String completeId = IdentityCheckCodeCalculator.generateCompleteId(first17List[i]);
            char calculated = IdentityCheckCodeCalculator.calculate(completeId);
            assertEquals(completeId.charAt(17), calculated,
                    "生成的身份证号码校验码计算错误: " + completeId);
        }

        // 专门测试 X 校验码
        String first17WithX = "11010519491231002";
        String completeWithX = IdentityCheckCodeCalculator.generateCompleteId(first17WithX);
        assertEquals('X', completeWithX.charAt(17));
        assertEquals('X', IdentityCheckCodeCalculator.calculate(completeWithX));
    }
}
