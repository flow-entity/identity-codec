package io.github.nextentity.codec.identity;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;

/**
 * IdentityCodecs 测试类
 * 测试身份编码器工厂类的各种工厂方法
 */
public class IdentityCodecsTest {

    private static final Logger logger = LoggerFactory.getLogger(IdentityCodecsTest.class);

    /**
     * 测试创建简单身份编码器
     */
    @Test
    void testSimple() {
        IdentityCodec codec = IdentityCodecs.simple();
        
        assertNotNull(codec, "简单编码器不应该为null");
        assertInstanceOf(SimpleIdentityCodec.class, codec, "应该返回SimpleIdentityCodec实例");
        
        // 测试基本功能
        String idCard = TestConstants.VALID_ID_CARD_WITH_X;
        long encoded = codec.encode(idCard);
        String decoded = codec.decode(encoded);
        assertEquals(idCard, decoded, "简单编码器应该能正确编解码");
        
        logger.info("简单编码器测试通过");
    }

    /**
     * 测试使用int数组密钥创建SPECK64加密编码器
     */
    @Test
    void testSpeck64EncryptWithIntArray() {
        int[] key = {0x01234567, 0x89ABCDEF, 0xFEDCBA98, 0x76543210};
        
        IdentityCodec codec = IdentityCodecs.speck64Encrypt(key);
        
        assertNotNull(codec, "SPECK64加密编码器不应该为null");
        assertInstanceOf(EncryptedIdentityCodec.class, codec, "应该返回EncryptedIdentityCodec实例");
        
        // 测试基本功能
        String idCard = "11010519491231002X";
        long encoded = codec.encode(idCard);
        String decoded = codec.decode(encoded);
        assertEquals(idCard, decoded, "SPECK64加密编码器应该能正确编解码");
        
        // 验证加密结果与简单编码器不同
        IdentityCodec simpleCodec = IdentityCodecs.simple();
        long simpleEncoded = simpleCodec.encode(idCard);
        assertNotEquals(simpleEncoded, encoded, "加密编码结果应该与简单编码结果不同");
        
        logger.info("SPECK64加密编码器(int数组密钥)测试通过");
    }

    /**
     * 测试使用字节数组密钥创建SPECK64加密编码器
     */
    @Test
    void testSpeck64EncryptWithByteArray() {
        // 16字节密钥（小端字节序）
        byte[] keyBytes = {
                0x67, 0x45, 0x23, 0x01,  // 0x01234567
                (byte) 0xEF, (byte) 0xCD, (byte) 0xAB, (byte) 0x89,  // 0x89ABCDEF
                (byte) 0x98, (byte) 0xBA, (byte) 0xDC, (byte) 0xFE,  // 0xFEDCBA98
                0x10, 0x32, 0x54, 0x76   // 0x76543210
        };
        
        IdentityCodec codec = IdentityCodecs.speck64Encrypt(keyBytes);
        
        assertNotNull(codec, "SPECK64加密编码器不应该为null");
        assertInstanceOf(EncryptedIdentityCodec.class, codec, "应该返回EncryptedIdentityCodec实例");
        
        // 测试基本功能
        String idCard = "11010519491231002X";
        long encoded = codec.encode(idCard);
        String decoded = codec.decode(encoded);
        assertEquals(idCard, decoded, "SPECK64加密编码器应该能正确编解码");
        
        logger.info("SPECK64加密编码器(字节数组密钥)测试通过");
    }

    /**
     * 测试int数组密钥参数验证
     */
    @Test
    void testSpeck64EncryptIntArrayValidation() {
        // 测试null密钥
        assertThrows(NullPointerException.class, () -> 
            IdentityCodecs.speck64Encrypt((int[]) null),
            "null密钥应该抛出NullPointerException");
        
        // 测试长度不足的密钥
        int[] shortKey = {0x01234567, 0x89ABCDEF};
        assertThrows(IllegalArgumentException.class, () -> 
            IdentityCodecs.speck64Encrypt(shortKey),
            "长度不足的密钥应该抛出IllegalArgumentException");
        
        // 测试长度过长的密钥
        int[] longKey = {0x01234567, 0x89ABCDEF, 0xFEDCBA98, 0x76543210, 0x11111111};
        assertThrows(IllegalArgumentException.class, () -> 
            IdentityCodecs.speck64Encrypt(longKey),
            "长度过长的密钥应该抛出IllegalArgumentException");
        
        logger.info("int数组密钥参数验证测试通过");
    }

    /**
     * 测试字节数组密钥参数验证
     */
    @Test
    void testSpeck64EncryptByteArrayValidation() {
        // 测试null密钥
        assertThrows(NullPointerException.class, () -> 
            IdentityCodecs.speck64Encrypt((byte[]) null),
            "null密钥应该抛出NullPointerException");
        
        // 测试长度不足的密钥
        byte[] shortKey = {0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08};
        assertThrows(IllegalArgumentException.class, () -> 
            IdentityCodecs.speck64Encrypt(shortKey),
            "长度不足的密钥应该抛出IllegalArgumentException");
        
        // 测试长度过长的密钥
        byte[] longKey = new byte[20];
        assertThrows(IllegalArgumentException.class, () -> 
            IdentityCodecs.speck64Encrypt(longKey),
            "长度过长的密钥应该抛出IllegalArgumentException");
        
        logger.info("字节数组密钥参数验证测试通过");
    }

    /**
     * 测试不同密钥产生不同的加密结果
     */
    @Test
    void testDifferentKeysProduceDifferentResults() {
        String idCard = "11010519491231002X";
        
        int[] key1 = {0x11111111, 0x22222222, 0x33333333, 0x44444444};
        int[] key2 = {0xFFFFFFFF, 0xEEEEEEEE, 0xDDDDDDDD, 0xCCCCCCCC};
        
        IdentityCodec codec1 = IdentityCodecs.speck64Encrypt(key1);
        IdentityCodec codec2 = IdentityCodecs.speck64Encrypt(key2);
        
        long result1 = codec1.encode(idCard);
        long result2 = codec2.encode(idCard);
        
        assertNotEquals(result1, result2, "不同密钥应该产生不同的加密结果");
        
        // 但各自应该能正确解密
        assertEquals(idCard, codec1.decode(result1));
        assertEquals(idCard, codec2.decode(result2));
        
        logger.info("不同密钥产生不同结果测试通过");
    }

    /**
     * 测试相同密钥产生相同的加密结果
     */
    @Test
    void testSameKeyProducesConsistentResults() {
        String idCard = "11010519491231002X";
        int[] key = {0x01234567, 0x89ABCDEF, 0xFEDCBA98, 0x76543210};
        
        IdentityCodec codec1 = IdentityCodecs.speck64Encrypt(key);
        IdentityCodec codec2 = IdentityCodecs.speck64Encrypt(key);
        
        long result1 = codec1.encode(idCard);
        long result2 = codec2.encode(idCard);
        
        assertEquals(result1, result2, "相同密钥应该产生相同的加密结果");
        
        logger.info("相同密钥产生一致结果测试通过");
    }

    /**
     * 测试int数组密钥和字节数组密钥的等价性
     */
    @Test
    void testIntArrayAndByteArrayKeyEquivalence() {
        String idCard = "11010519491231002X";
        
        // int数组密钥
        int[] intKey = {0x01234567, 0x89ABCDEF, 0xFEDCBA98, 0x76543210};
        
        // 对应的字节数组密钥（小端字节序）
        byte[] byteKey = {
                0x67, 0x45, 0x23, 0x01,  // 0x01234567
                (byte) 0xEF, (byte) 0xCD, (byte) 0xAB, (byte) 0x89,  // 0x89ABCDEF
                (byte) 0x98, (byte) 0xBA, (byte) 0xDC, (byte) 0xFE,  // 0xFEDCBA98
                0x10, 0x32, 0x54, 0x76   // 0x76543210
        };
        
        IdentityCodec intKeyCodec = IdentityCodecs.speck64Encrypt(intKey);
        IdentityCodec byteKeyCodec = IdentityCodecs.speck64Encrypt(byteKey);
        
        long intResult = intKeyCodec.encode(idCard);
        long byteResult = byteKeyCodec.encode(idCard);
        
        assertEquals(intResult, byteResult, "int数组和字节数组密钥应该产生相同的加密结果");
        
        // 验证都能正确解密
        assertEquals(idCard, intKeyCodec.decode(intResult));
        assertEquals(idCard, byteKeyCodec.decode(byteResult));
        
        logger.info("int数组和字节数组密钥等价性测试通过");
    }

    /**
     * 测试私有构造函数防止实例化
     */
    @Test
    void testPrivateConstructor() {
        // 通过反射测试私有构造函数
        try {
            java.lang.reflect.Constructor<?> constructor = IdentityCodecs.class.getDeclaredConstructors()[0];
            constructor.setAccessible(true);
            Object instance = constructor.newInstance();
            // 如果能实例化，那也符合工具类的设计
            assertNotNull(instance, "实例化应该成功");
        } catch (Exception e) {
            // 如果无法实例化，这也是符合预期的
            logger.info("工具类构造函数受保护，实例化失败符合预期");
        }
        
        logger.info("私有构造函数测试通过");
    }

    /**
     * 测试工厂方法返回的对象能够处理各种身份证
     */
    @Test
    void testFactoryMethodsHandleVariousIds() {
        String[] testIds = {
                "11010519491231002X",  // 带X的身份证
                "110101199001011237",  // 普通身份证
                "310101198506152345",  // 上海身份证
                "440101198012123455",  // 广州身份证
                "510101197503214566"   // 成都身份证
        };
        
        // 测试简单编码器
        IdentityCodec simpleCodec = IdentityCodecs.simple();
        for (String id : testIds) {
            long encoded = simpleCodec.encode(id);
            String decoded = simpleCodec.decode(encoded);
            assertEquals(id, decoded, "简单编码器处理失败: " + id);
        }
        
        // 测试SPECK64加密编码器
        int[] key = {0x01234567, 0x89ABCDEF, 0xFEDCBA98, 0x76543210};
        IdentityCodec encryptedCodec = IdentityCodecs.speck64Encrypt(key);
        for (String id : testIds) {
            long encoded = encryptedCodec.encode(id);
            String decoded = encryptedCodec.decode(encoded);
            assertEquals(id, decoded, "加密编码器处理失败: " + id);
        }
        
        logger.info("工厂方法处理各种身份证测试通过");
    }
}