package io.github.nextentity.codec.identity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Speck64EncryptedIdentityCodec 测试类
 * 测试使用SPECK64加密的身份编码器功能
 */
public class Speck64EncryptedIdentityCodecTest {
    
    private static final Logger logger = LoggerFactory.getLogger(Speck64EncryptedIdentityCodecTest.class);

    private IdentityCodec encryptedCodec;
    private static final int[] TEST_KEY = {0x01234567, 0x89ABCDEF, 0xFEDCBA98, 0x76543210};

    @BeforeEach
    void setUp() {
        // 使用固定的测试密钥
        encryptedCodec = IdentityCodecs.speck64Encrypt(TEST_KEY);
    }

    /**
     * 测试基本的加密编码和解密功能
     */
    @Test
    void testEncodeDecodeBasic() {
        String idCard = "11010519491231002X";

        // 编码
        long encoded = encryptedCodec.encode(idCard);
        logger.info("身份证: {}", idCard);
        logger.info("编码结果: {}", Long.toHexString(encoded));

        // 解码
        String decoded = encryptedCodec.decode(encoded);
        logger.info("解码结果: {}", decoded);

        assertEquals(idCard, decoded, "解码后的身份证应该与原始一致");
        assertNotEquals(idCard.hashCode(), encoded, "编码结果应该与原始哈希值不同");
    }

    /**
     * 测试不同身份证的编码唯一性
     */
    @Test
    void testEncodingUniqueness() {
        String idCard1 = "11010519491231002X";
        String idCard2 = "11010119900307109X"; // 使用已知有效的不同身份证

        long result1 = encryptedCodec.encode(idCard1);
        long result2 = encryptedCodec.encode(idCard2);

        assertNotEquals(result1, result2, "不同身份证应该产生不同的编码结果");
    }

    /**
     * 测试使用字节数组密钥的构造函数
     */
    @Test
    void testByteArrayKeyConstruction() {
        // 16字节密钥
        byte[] keyBytes = {
                0x01, 0x23, 0x45, 0x67,
                (byte) 0x89, (byte) 0xAB, (byte) 0xCD, (byte) 0xEF,
                (byte) 0xFE, (byte) 0xDC, (byte) 0xBA, (byte) 0x98,
                0x76, 0x54, 0x32, 0x10
        };

        IdentityCodec byteKeyCodec = IdentityCodecs.speck64Encrypt(keyBytes);

        String idCard = "11010519491231002X";
        long encoded = byteKeyCodec.encode(idCard);
        String decoded = byteKeyCodec.decode(encoded);

        assertEquals(idCard, decoded, "使用字节数组密钥应该能正确编解码");
    }

    /**
     * 测试已知有效的身份证号码
     */
    @Test
    void testValidIdCards() {
        String[] validIds = {
                "11010519491231002X",  // 带X的标准身份证
                "11010119900307109X",  // 另一个带X的身份证
                "110101199001011237"   // 普通数字身份证
        };

        for (String id : validIds) {
            long encoded = encryptedCodec.encode(id);
            String decoded = encryptedCodec.decode(encoded);
            assertEquals(id, decoded, "有效身份证 " + id + " 应该能正确编解码");
        }
    }

    /**
     * 测试非法输入
     */
    @Test
    void testInvalidInput() {
        // 测试null输入
        assertThrows(NullPointerException.class, () -> encryptedCodec.encode(null));

        // 测试无效身份证格式
        assertThrows(IllegalArgumentException.class, () -> encryptedCodec.encode("123"));
        assertThrows(IllegalArgumentException.class, () -> encryptedCodec.encode("110105194912310021")); // 校验位错误
    }

    /**
     * 测试不同密钥产生不同结果
     */
    @Test
    void testDifferentKeys() {
        String idCard = "11010519491231002X";

        // 使用不同密钥
        int[] key1 = {0x11111111, 0x22222222, 0x33333333, 0x44444444};
        int[] key2 = {0xFFFFFFFF, 0xEEEEEEEE, 0xDDDDDDDD, 0xCCCCCCCC};

        IdentityCodec codec1 = IdentityCodecs.speck64Encrypt(key1);
        IdentityCodec codec2 = IdentityCodecs.speck64Encrypt(key2);

        long result1 = codec1.encode(idCard);
        long result2 = codec2.encode(idCard);

        assertNotEquals(result1, result2, "不同密钥应该产生不同的加密结果");
    }

    /**
     * 性能测试
     */
    @Test
    void testPerformance() {
        String[] testIds = {
                "11010519491231002X",
                "11010119900307109X",
                "110101199001011237"
        };

        long startTime = System.nanoTime();

        // 批量处理
        for (int i = 0; i < 1000000; i++) {
            for (String id : testIds) {
                long encoded = encryptedCodec.encode(id);
                String decoded = encryptedCodec.decode(encoded);
                assertEquals(id, decoded);
            }
        }

        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1_000_000; // 转换为毫秒

        logger.info("SPECK64加密身份编码性能测试完成，耗时: {} ms", duration);
        assertTrue(duration < 5000, "性能测试应该在5秒内完成");
    }

    /**
     * 测试加密强度
     */
    @Test
    void testEncryptionStrength() {
        String idCard = "11010519491231002X";
        long encoded = encryptedCodec.encode(idCard);

        // 验证加密结果看起来是随机的
        assertNotEquals(idCard.hashCode(), encoded, "加密结果不应该等于原始哈希值");
        assertTrue(encoded != 0, "加密结果不应该全为0");
        assertTrue(encoded != -1, "加密结果不应该全为1");

        // 验证雪崩效应
        String idCard2 = "11010119900307109X"; // 使用不同的有效身份证
        long encoded2 = encryptedCodec.encode(idCard2);
        long diff = encoded ^ encoded2;
        assertNotEquals(0L, diff, "不同输入应该导致不同的输出");
    }
}