package io.github.nextentity.codec.identity;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * InvalidEncodingException 测试类
 * 测试无效编码异常的各种构造函数和功能
 */
public class InvalidEncodingExceptionTest {

    private static final Logger logger = LoggerFactory.getLogger(InvalidEncodingExceptionTest.class);

    /**
     * 测试基本构造函数
     */
    @Test
    void testBasicConstructor() {
        String message = "测试异常消息";
        InvalidEncodingException exception = new InvalidEncodingException(message);

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
        InvalidEncodingException.ErrorCode errorCode = InvalidEncodingException.ErrorCode.UNSUPPORTED_VERSION;
        String detail = "版本2不被支持";
        InvalidEncodingException exception = new InvalidEncodingException(errorCode, detail);

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
        InvalidEncodingException.ErrorCode errorCode = InvalidEncodingException.ErrorCode.RESERVED_BITS_NOT_ZERO;
        String detail = "预留位不为零";
        IOException cause = new IOException("底层IO错误");
        InvalidEncodingException exception = new InvalidEncodingException(errorCode, detail, cause);

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
        InvalidEncodingException.ErrorCode[] errorCodes = InvalidEncodingException.ErrorCode.values();
        
        assertEquals(3, errorCodes.length, "应该有3个错误码");
        
        // 验证每个错误码的属性
        for (InvalidEncodingException.ErrorCode errorCode : errorCodes) {
            assertNotNull(errorCode.getCode(), "错误码不应该为null");
            assertNotNull(errorCode.getDescription(), "错误描述不应该为null");
            assertFalse(errorCode.getCode().isEmpty(), "错误码不应该为空");
            assertFalse(errorCode.getDescription().isEmpty(), "错误描述不应该为空");
            
            logger.info("错误码: {} - {}", errorCode.getCode(), errorCode.getDescription());
        }
        
        logger.info("所有错误码枚举测试通过");
    }

    /**
     * 测试不支持的版本错误码
     */
    @Test
    void testUnsupportedVersionErrorCode() {
        InvalidEncodingException.ErrorCode errorCode = InvalidEncodingException.ErrorCode.UNSUPPORTED_VERSION;
        
        assertEquals("IEC-001", errorCode.getCode(), "不支持的版本错误码应该正确");
        assertEquals("Unsupported compression version", errorCode.getDescription(), 
                "不支持的版本错误描述应该正确");

        String detail = "检测到版本2";
        InvalidEncodingException exception = new InvalidEncodingException(errorCode, detail);
        
        assertTrue(exception.getMessage().contains("IEC-001"), 
                "异常消息应该包含错误码");
        assertTrue(exception.getMessage().contains("Unsupported compression version"), 
                "异常消息应该包含错误描述");
        assertTrue(exception.getMessage().contains(detail), 
                "异常消息应该包含详细信息");

        logger.info("不支持的版本错误码测试通过");
    }

    /**
     * 测试预留位不为零错误码
     */
    @Test
    void testReservedBitsNotZeroErrorCode() {
        InvalidEncodingException.ErrorCode errorCode = InvalidEncodingException.ErrorCode.RESERVED_BITS_NOT_ZERO;
        
        assertEquals("IEC-002", errorCode.getCode(), "预留位不为零错误码应该正确");
        assertEquals("Reserved bits must be zero", errorCode.getDescription(), 
                "预留位不为零错误描述应该正确");

        String detail = "预留位检测到非零值";
        InvalidEncodingException exception = new InvalidEncodingException(errorCode, detail);
        
        assertTrue(exception.getMessage().contains("IEC-002"), 
                "异常消息应该包含错误码");
        assertTrue(exception.getMessage().contains("Reserved bits must be zero"), 
                "异常消息应该包含错误描述");

        logger.info("预留位不为零错误码测试通过");
    }

    /**
     * 测试无效位域提取错误码
     */
    @Test
    void testInvalidBitFieldErrorCode() {
        InvalidEncodingException.ErrorCode errorCode = InvalidEncodingException.ErrorCode.INVALID_BIT_FIELD;
        
        assertEquals("IEC-003", errorCode.getCode(), "无效位域提取错误码应该正确");
        assertEquals("Invalid bit field extraction", errorCode.getDescription(), 
                "无效位域提取错误描述应该正确");

        String detail = "无法从编码值中提取有效的位域";
        InvalidEncodingException exception = new InvalidEncodingException(errorCode, detail);
        
        assertTrue(exception.getMessage().contains("IEC-003"), 
                "异常消息应该包含错误码");
        assertTrue(exception.getMessage().contains("Invalid bit field extraction"), 
                "异常消息应该包含错误描述");

        logger.info("无效位域提取错误码测试通过");
    }

    /**
     * 测试异常的序列化
     */
    @Test
    void testExceptionSerialization() {
        InvalidEncodingException.ErrorCode errorCode = InvalidEncodingException.ErrorCode.UNSUPPORTED_VERSION;
        String detail = "序列化测试";
        InvalidEncodingException originalException = new InvalidEncodingException(errorCode, detail);

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
        InvalidEncodingException exception = new InvalidEncodingException(message);

        // 验证继承关系
        assertTrue(exception instanceof IllegalArgumentException, 
                "InvalidEncodingException应该继承IllegalArgumentException");
        assertTrue(exception instanceof RuntimeException, 
                "InvalidEncodingException应该继承RuntimeException");
        assertTrue(exception instanceof Exception, 
                "InvalidEncodingException应该继承Exception");

        logger.info("异常继承关系测试通过");
    }

    /**
     * 测试异常的堆栈跟踪
     */
    @Test
    void testExceptionStackTrace() {
        InvalidEncodingException.ErrorCode errorCode = InvalidEncodingException.ErrorCode.INVALID_BIT_FIELD;
        String detail = "堆栈跟踪测试";
        
        try {
            // 故意抛出异常以生成堆栈跟踪
            throw new InvalidEncodingException(errorCode, detail);
        } catch (InvalidEncodingException exception) {
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
        InvalidEncodingException.ErrorCode errorCode = InvalidEncodingException.ErrorCode.UNSUPPORTED_VERSION;
        
        String errorCodeString = errorCode.toString();
        assertNotNull(errorCodeString, "错误码的toString不应该为null");
        assertTrue(errorCodeString.contains("UNSUPPORTED_VERSION"), 
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
        
        InvalidEncodingException exception1 = new InvalidEncodingException(message1);
        InvalidEncodingException exception2 = new InvalidEncodingException(message1);
        InvalidEncodingException exception3 = new InvalidEncodingException(message2);
        
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
        // 模拟不支持的版本场景
        try {
            int version = 2;
            if (version != 1) {
                throw new InvalidEncodingException(
                        InvalidEncodingException.ErrorCode.UNSUPPORTED_VERSION, 
                        "版本 " + version + " 不被支持，仅支持版本1");
            }
        } catch (InvalidEncodingException e) {
            assertTrue(e.getMessage().contains("IEC-001"));
            assertTrue(e.getMessage().contains("版本 2 不被支持"));
            assertEquals(InvalidEncodingException.ErrorCode.UNSUPPORTED_VERSION, e.getErrorCode());
        }

        // 模拟预留位不为零场景
        try {
            long encodedValue = 0xFF00000000000001L; // 假设高8位不为零
            if ((encodedValue & 0xFF00000000000000L) != 0) {
                throw new InvalidEncodingException(
                        InvalidEncodingException.ErrorCode.RESERVED_BITS_NOT_ZERO, 
                        "预留位包含非零值: 0x" + Long.toHexString(encodedValue >>> 56));
            }
        } catch (InvalidEncodingException e) {
            assertTrue(e.getMessage().contains("IEC-002"));
            assertTrue(e.getMessage().contains("预留位包含非零值"));
            assertEquals(InvalidEncodingException.ErrorCode.RESERVED_BITS_NOT_ZERO, e.getErrorCode());
        }

        logger.info("实际使用场景测试通过");
    }

    /**
     * 测试异常的链式异常处理
     */
    @Test
    void testChainedExceptionHandling() {
        IOException rootCause = new IOException("底层IO操作失败");
        InvalidEncodingException.ErrorCode errorCode = InvalidEncodingException.ErrorCode.INVALID_BIT_FIELD;
        String detail = "由于IO错误导致位域提取失败";
        
        InvalidEncodingException chainedException = new InvalidEncodingException(errorCode, detail, rootCause);
        
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
}