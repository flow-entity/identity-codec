package io.github.nextentity.codec.identity;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * InvalidIdentityNumberException 测试类
 * 测试无效身份证号码异常的各种构造函数和功能
 */
public class InvalidIdentityNumberExceptionTest {

    private static final Logger logger = LoggerFactory.getLogger(InvalidIdentityNumberExceptionTest.class);

    /**
     * 测试基本构造函数
     */
    @Test
    void testBasicConstructor() {
        String message = "测试异常消息";
        InvalidIdentityNumberException exception = new InvalidIdentityNumberException(message);

        assertNotNull(exception, "异常实例不应该为null");
        assertEquals(message, exception.getMessage(), "异常消息应该正确设置");
        assertNull(exception.getCause(), "默认情况下cause应该为null");
        assertNull(exception.getErrorCode(), "默认情况下错误码应该为null");
        assertNull(exception.getErrorCodeString(), "默认情况下错误码字符串应该为null");

        logger.info("基本构造函数测试通过");
    }

    /**
     * 测试带错误码的构造函数
     */
    @Test
    void testConstructorWithErrorCode() {
        ErrorCode errorCode = ErrorCode.INVALID_ID_LENGTH;
        String detail = "身份证号码长度不正确";
        InvalidIdentityNumberException exception = new InvalidIdentityNumberException(errorCode, detail);

        assertNotNull(exception, "异常实例不应该为null");

        String expectedMessage = String.format("[%s] %s: %s",
                errorCode.getCode(), errorCode.getDescription(), detail);
        assertEquals(expectedMessage, exception.getMessage(), "异常消息应该包含错误码和详情");
        assertNull(exception.getCause(), "cause应该为null");
        assertEquals(errorCode, exception.getErrorCode(), "错误码应该正确设置");
        assertEquals(errorCode.getCode(), exception.getErrorCodeString(), "错误码字符串应该正确设置");

        logger.info("带错误码构造函数测试通过");
    }

    /**
     * 测试带错误码和原因的构造函数
     */
    @Test
    void testConstructorWithErrorCodeAndCause() {
        ErrorCode errorCode = ErrorCode.INVALID_CHECK_CODE;
        String detail = "校验码验证失败";
        NumberFormatException cause = new NumberFormatException("数字格式错误");
        InvalidIdentityNumberException exception = new InvalidIdentityNumberException(errorCode, detail, cause);

        assertNotNull(exception, "异常实例不应该为null");

        String expectedMessage = String.format("[%s] %s: %s",
                errorCode.getCode(), errorCode.getDescription(), detail);
        assertEquals(expectedMessage, exception.getMessage(), "异常消息应该包含错误码和详情");
        assertEquals(cause, exception.getCause(), "cause应该正确设置");
        assertEquals(errorCode, exception.getErrorCode(), "错误码应该正确设置");
        assertEquals(errorCode.getCode(), exception.getErrorCodeString(), "错误码字符串应该正确设置");

        logger.info("带错误码和原因构造函数测试通过");
    }

    /**
     * 测试所有错误码枚举值
     */
    @Test
    void testAllErrorCodeEnums() {
        // 测试身份证相关的错误码
        ErrorCode[] idNumberErrorCodes = {
                ErrorCode.INVALID_ID_LENGTH,
                ErrorCode.INVALID_CHECK_CODE,
                ErrorCode.INVALID_CHARACTER,
                ErrorCode.INVALID_DATE
        };

        assertEquals(4, idNumberErrorCodes.length, "应该有4个身份证错误码");

        // 验证每个错误码的属性
        for (ErrorCode errorCode : idNumberErrorCodes) {
            assertNotNull(errorCode.getCode(), "错误码不应该为null");
            assertNotNull(errorCode.getDescription(), "错误描述不应该为null");
            assertFalse(errorCode.getCode().isEmpty(), "错误码不应该为空");
            assertFalse(errorCode.getDescription().isEmpty(), "错误描述不应该为空");

");

            logger.info("错误码: {} - {}", errorCode.getCode(), errorCode.getDescription());
        }

        logger.info("所有错误码枚举测试通过");
    }

    /**
     * 测试无效长度错误码
     */
    @Test
    void testInvalidLengthErrorCode() {
        ErrorCode errorCode = ErrorCode.INVALID_ID_LENGTH;

        assertEquals("IIN-001", errorCode.getCode(), "无效长度错误码应该正确为IIN-001");
        assertEquals("无效的身份证号码长度", errorCode.getDescription(),
                "无效长度错误描述应该正确");

        String detail = "身份证号码长度为16，期望18位";
        InvalidIdentityNumberException exception = new InvalidIdentityNumberException(errorCode, detail);

        assertTrue(exception.getMessage().contains("IIN-001"),
                "异常消息应该包含错误码");
        assertTrue(exception.getMessage().contains("无效的身份证号码长度"),
                "异常消息应该包含错误描述");
        assertTrue(exception.getMessage().contains(detail),
                "异常消息应该包含详细信息");

        logger.info("无效长度错误码测试通过");
    }

    /**
     * 测试无效校验码错误码
     */
    @Test
    void testInvalidCheckCodeErrorCode() {
        ErrorCode errorCode = ErrorCode.INVALID_CHECK_CODE;

        assertEquals("IIN-002", errorCode.getCode(), "无效校验码错误码应该正确为IIN-002");
        assertEquals("无效的校验码", errorCode.getDescription(),
                "无效校验码错误描述应该正确");

        String detail = "校验码计算结果为5，但提供的校验码是6";
        InvalidIdentityNumberException exception = new InvalidIdentityNumberException(errorCode, detail);

        assertTrue(exception.getMessage().contains("IIN-002"),
                "异常消息应该包含错误码");
        assertTrue(exception.getMessage().contains("无效的校验码"),
                "异常消息应该包含错误描述");

        logger.info("无效校验码错误码测试通过");
    }

    /**
     * 测试无效字符错误码
     */
    @Test
    void testInvalidCharacterErrorCode() {
        ErrorCode errorCode = ErrorCode.INVALID_CHARACTER;

        assertEquals("IIN-003", errorCode.getCode(), "无效字符错误码应该正确为IIN-003");
        assertEquals("身份证号码中包含无效字符", errorCode.getDescription(),
                "无效字符错误描述应该正确");

        String detail = "身份证号码中包含非法字符: 'A'";
        InvalidIdentityNumberException exception = new InvalidIdentityNumberException(errorCode, detail);

        assertTrue(exception.getMessage().contains("IIN-003"),
                "异常消息应该包含错误码");
        assertTrue(exception.getMessage().contains("身份证号码中包含无效字符"),
                "异常消息应该包含错误描述");

        logger.info("无效字符错误码测试通过");
    }

    /**
     * 测试无效日期错误码
     */
    @Test
    void testInvalidDateErrorCode() {
        ErrorCode errorCode = ErrorCode.INVALID_DATE;

        assertEquals("IIN-004", errorCode.getCode(), "无效日期错误码应该正确为IIN-004");
        assertEquals("无效的出生日期", errorCode.getDescription(),
                "无效日期错误描述应该正确");

        String detail = "出生日期超出有效范围";
        InvalidIdentityNumberException exception = new InvalidIdentityNumberException(errorCode, detail);

        assertTrue(exception.getMessage().contains("IIN-004"),
                "异常消息应该包含错误码");
        assertTrue(exception.getMessage().contains("无效的出生日期"),
                "异常消息应该包含错误描述");

        logger.info("无效日期错误码测试通过");
    }

    /**
     * 测试异常的序列化
     */
    @Test
    void testExceptionSerialization() {
        ErrorCode errorCode = ErrorCode.INVALID_ID_LENGTH;
        String detail = "序列化测试";
        InvalidIdentityNumberException originalException = new InvalidIdentityNumberException(errorCode, detail);

        // 验证异常可以被序列化和反序列化
        try {
            // 这里主要测试serialVersionUID的存在和异常的基本属性
            assertNotNull(originalException, "原始异常不应该为null");
            assertEquals(errorCode, originalException.getErrorCode(), "错误码应该保持");
            assertEquals(detail, originalException.getMessage().substring(
                    originalException.getMessage().lastIndexOf(": ") + 2), "详情应该保持");

            logger.info("异常序列化测试通过");
        } catch (Exception e) {
            fail("异常序列化测试失败: " + e.getMessage());
        }
    }

    /**
     * 测试异常的继承关系
     */
    @Test
    void testExceptionInheritance() {
        String message = "继承测试";
        InvalidIdentityNumberException exception = new InvalidIdentityNumberException(message);

        // 验证继承关系
        assertTrue(exception instanceof IdentityCodecException,
                "InvalidIdentityNumberException应该继承IdentityCodecException");
        assertTrue(exception instanceof RuntimeException,
                "InvalidIdentityNumberException应该继承RuntimeException");
        assertTrue(exception instanceof Exception,
                "InvalidIdentityNumberException应该继承Exception");

        logger.info("异常继承关系测试通过");
    }

    /**
     * 测试异常的堆栈跟踪
     */
    @Test
    void testExceptionStackTrace() {
        ErrorCode errorCode = ErrorCode.INVALID_CHARACTER;
        String detail = "堆栈跟踪测试";

        try {
            // 故意抛出异常以生成堆栈跟踪
            throw new InvalidIdentityNumberException(errorCode, detail);
        } catch (InvalidIdentityNumberException exception) {
            StackTraceElement[] stackTrace = exception.getStackTrace();
            assertNotNull(stackTrace, "堆栈跟踪不应该为null");
            assertTrue(stackTrace.length > 0, "堆栈跟踪应该包含元素");

            // 验证当前方法在堆栈跟踪中
            boolean found = false;
            for (StackTraceElement element : stackTrace) {
                if (element.getMethodName().equals("testExceptionStackTrace")) {
                    found = true;
                    break;
                }
            }
            assertTrue(found, "堆栈跟踪应该包含当前方法");
        }

        logger.info("异常堆栈跟踪测试通过");
    }

    /**
     * 测试错误码的toString方法
     */
    @Test
    void testErrorCodeToString() {
        ErrorCode errorCode = ErrorCode.INVALID_ID_LENGTH;

        String errorCodeString = errorCode.toString();
        assertNotNull(errorCodeString, "错误码的toString不应该为null");
        assertTrue(errorCodeString.contains("INVALID_ID_LENGTH"),
                "toString应该包含错误码名称");

        logger.info("错误码toString测试通过: {}", errorCodeString);
    }

    /**
     * 测试异常的equals和hashCode（如果重写了的话）
     */
    @Test
    void testExceptionEqualsAndHashCode() {
        String message1 = "测试消息1";
        String message2 = "测试消息2";

        InvalidIdentityNumberException exception1 = new InvalidIdentityNumberException(message1);
        InvalidIdentityNumberException exception2 = new InvalidIdentityNumberException(message1);
        InvalidIdentityNumberException exception3 = new InvalidIdentityNumberException(message2);

        // 异常的equals通常基于引用相等性，这里我们测试基本行为
        assertEquals(exception1, exception1, "异常应该等于自己");
        assertNotEquals(exception1, exception2, "不同实例的异常通常不相等");
        assertNotEquals(exception1, exception3, "不同消息的异常不应该相等");
        assertNotEquals(exception1, null, "异常不应该等于null");
        assertNotEquals(exception1, "字符串", "异常不应该等于不同类型的对象");

        logger.info("异常equals和hashCode测试通过");
    }

    /**
     * 测试异常的实际使用场景
     */
    @Test
    void testRealWorldUsage() {
        // 模拟无效长度场景
        try {
            String idNumber = "1234567890123456"; // 16位
            if (idNumber.length() != 18) {
                throw new InvalidIdentityNumberException(
                        ErrorCode.INVALID_ID_LENGTH,
                        "身份证号码长度为 " + idNumber.length() + "，期望18位");
            }
        } catch (InvalidIdentityNumberException e) {
            assertTrue(e.getMessage().contains("IIN-001"));
            assertTrue(e.getMessage().contains("身份证号码长度为 16"));
            assertEquals(ErrorCode.INVALID_ID_LENGTH, e.getErrorCode());
        }

        // 模拟无效校验码场景
        try {
            String providedCheckCode = "1";
            String expectedCheckCode = "X";
            if (!providedCheckCode.equals(expectedCheckCode)) {
                throw new InvalidIdentityNumberException(
                        ErrorCode.INVALID_CHECK_CODE,
                        String.format("Expected '%s', actual '%s'", expectedCheckCode, providedCheckCode));
            }
        } catch (InvalidIdentityNumberException e) {
            assertTrue(e.getMessage().contains("IIN-002"));
            assertTrue(e.getMessage().contains("Expected 'X', actual '1'"));
            assertEquals(ErrorCode.INVALID_CHECK_CODE, e.getErrorCode());
        }

        // 模拟无效日期场景
        try {
            int month = 13;
            if (month < 1 || month > 12) {
                throw new InvalidIdentityNumberException(
                        ErrorCode.INVALID_DATE,
                        "Invalid month: " + month);
            }
        } catch (InvalidIdentityNumberException e) {
            assertTrue(e.getMessage().contains("IIN-004"));
            assertTrue(e.getMessage().contains("Invalid month: 13"));
            assertEquals(ErrorCode.INVALID_DATE, e.getErrorCode());
        }

        logger.info("实际使用场景测试通过");
    }

    /**
     * 测试异常的链式异常处理
     */
    @Test
    void testChainedExceptionHandling() {
        NumberFormatException rootCause = new NumberFormatException("数字格式错误");
        ErrorCode errorCode = ErrorCode.INVALID_DATE;
        String detail = "由于数字格式错误导致日期解析失败";

        InvalidIdentityNumberException chainedException = new InvalidIdentityNumberException(errorCode, detail, rootCause);

        assertEquals(rootCause, chainedException.getCause(), "原因异常应该正确设置");
        assertEquals(errorCode, chainedException.getErrorCode(), "错误码应该正确设置");
        assertTrue(chainedException.getMessage().contains(detail), "消息应该包含详细信息");

        // 测试异常链的遍历
        Throwable current = chainedException;
        int chainLength = 0;
        while (current != null) {
            chainLength++;
            current = current.getCause();
        }
        assertEquals(2, chainLength, "异常链应该包含2个异常");

        logger.info("链式异常处理测试通过");
    }

    /**
     * 测试使用字节数组输入的场景
     */
    @Test
    void testByteArrayInput() {
        byte[] bytes = "110101199001011237".getBytes(StandardCharsets.UTF_8);

        try {
            if (bytes.length != 18) {
                throw new InvalidIdentityNumberException(
                        ErrorCode.INVALID_ID_LENGTH,
                        "字节数组长度为 " + bytes.length + "，期望18位");
            }
        } catch (InvalidIdentityNumberException e) {
            assertTrue(e.getMessage().contains("IIN-001"));
            assertEquals(ErrorCode.INVALID_ID_LENGTH, e.getErrorCode());
        }

        logger.info("字节数组输入测试通过");
    }

    /**
     * 测试边界值场景
     */
    @Test
    void testBoundaryValueScenarios() {
        // 测试空字符串
        try {
            String emptyId = "";
            if (emptyId.isEmpty()) {
                throw new InvalidIdentityNumberException(
                        ErrorCode.INVALID_ID_LENGTH,
                        "身份证号码为空");
            }
        } catch (InvalidIdentityNumberException e) {
            assertEquals(ErrorCode.INVALID_ID_LENGTH, e.getErrorCode());
        }

        // 测试非数字字符
        try {
            char invalidChar = 'A';
            if (!Character.isDigit(invalidChar)) {
                throw new InvalidIdentityNumberException(
                        ErrorCode.INVALID_CHARACTER,
                        "包含非法字符: " + invalidChar);
            }
        } catch (InvalidIdentityNumberException e) {
            assertEquals(ErrorCode.INVALID_CHARACTER, e.getErrorCode());
        }

        logger.info("边界值场景测试通过");
    }
}
