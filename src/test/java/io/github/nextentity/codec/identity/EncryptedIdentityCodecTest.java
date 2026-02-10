package io.github.nextentity.codec.identity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * EncryptedIdentityCodec 测试类
 * 测试加密身份编码器的功能
 */
public class EncryptedIdentityCodecTest {

    private EncryptedIdentityCodec encryptedCodec;

    @BeforeEach
    void setUp() {
        // 使用固定的测试密钥流
        long testKeystream = new Random().nextLong();
        encryptedCodec = new EncryptedIdentityCodec(testKeystream);
    }

    /**
     * 测试基本的加密编码解码功能
     */
    @Test
    void testEncryptDecryptBasic() {
        String idCard = "11010519491231002X";

        // 加密编码
        long encrypted = encryptedCodec.encode(idCard);
        System.out.println("原始身份证: " + idCard);
        System.out.println("加密编码结果: " + encrypted);
        System.out.println("加密二进制: " + Long.toBinaryString(encrypted));

        // 解密解码
        String decrypted = encryptedCodec.decode(encrypted);
        System.out.println("解密解码结果: " + decrypted);

        // 验证
        assertEquals(idCard, decrypted, "加密解密后应该与原始身份证号码一致");
    }

    /**
     * 测试与未加密版本的结果不同
     */
    @Test
    void testEncryptionEffect() {
        String idCard = "11010519491231002X";
        SimpleIdentityCodec simpleCodec = new SimpleIdentityCodec();

        // 普通编码
        long plainResult = simpleCodec.encode(idCard);

        // 加密编码
        long encryptedResult = encryptedCodec.encode(idCard);

        // 验证结果不同（加密起了作用）
        assertNotEquals(plainResult, encryptedResult, "加密结果应该与普通编码结果不同");

        System.out.println("普通编码: " + plainResult);
        System.out.println("加密编码: " + encryptedResult);
    }

    /**
     * 测试不同的密钥产生不同的加密结果
     */
    @Test
    void testDifferentKeys() {
        String idCard = "11010519491231002X";

        // 使用不同密钥的编码器
        long key1 = 0x0102030405060708L;
        long key2 = 0xFFFFFFFFFFFFFFFEL;

        EncryptedIdentityCodec codec1 = new EncryptedIdentityCodec(key1);
        EncryptedIdentityCodec codec2 = new EncryptedIdentityCodec(key2);

        long result1 = codec1.encode(idCard);
        long result2 = codec2.encode(idCard);

        assertNotEquals(result1, result2, "不同密钥应该产生不同的加密结果");
    }

    /**
     * 测试获取内部组件
     */
    @Test
    void testGetComponentAccess() {
        assertNotNull(encryptedCodec.getIdentityCodec(), "应该能够获取 IdentityCodec");
        assertNotNull(encryptedCodec.getXorEncryptor(), "应该能够获取 XorEncryptor");
    }

    /**
     * 测试多个身份证号码的加解密
     */
    @Test
    void testMultipleIdCards() {
        String[] testIds = {
                "110101199001011237",
                "310101198506152345",
                "440101198012123455",
                "510101197503214566"
        };

        for (String id : testIds) {
            long encrypted = encryptedCodec.encode(id);
            String decrypted = encryptedCodec.decode(encrypted);
            assertEquals(id, decrypted, "多身份证测试失败: " + id);
        }
    }

    /**
     * 测试加密的一致性
     */
    @Test
    void testEncryptionConsistency() {
        String idCard = "11010519491231002X";

        // 多次加密应该得到相同结果
        long encrypted1 = encryptedCodec.encode(idCard);
        long encrypted2 = encryptedCodec.encode(idCard);
        long encrypted3 = encryptedCodec.encode(idCard);

        assertEquals(encrypted1, encrypted2, "同一身份证多次加密应该一致");
        assertEquals(encrypted2, encrypted3, "同一身份证多次加密应该一致");
    }

    /**
     * 性能测试 - 加密编码解码
     */
    @Test
    void testEncryptedPerformance() {
        String[] testIds = {
                "110101199001011237",
                "110101198506152348",
                "110101200012123459",
                "110101197503214561",
                "110101196008155678"
        };

        long startTime = System.nanoTime();

        for (int i = 0; i < 1000; i++) {
            for (String id : testIds) {
                long encrypted = encryptedCodec.encode(id);
                String decrypted = encryptedCodec.decode(encrypted);
                assertEquals(id, decrypted);
            }
        }

        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1_000_000; // 转换为毫秒

        System.out.println("加密性能测试完成，耗时: " + duration + " ms");
        assertTrue(duration < 10000, "加密性能测试应该在10秒内完成");
    }

    /**
     * 测试与 SimpleIdentityCodec 的兼容性
     */
    @Test
    void testCompatibilityWithSimpleCodec() {
        String idCard = "11010519491231002X";
        SimpleIdentityCodec simpleCodec = new SimpleIdentityCodec();

        // 先用SimpleIdentityCodec编码，再手动加密解密验证流程
        long plainEncoded = simpleCodec.encode(idCard);

        // 手动加密
        long manualEncrypted = encryptedCodec.getXorEncryptor().encrypt(plainEncoded);

        // 使用封装的加密编码器
        long autoEncrypted = encryptedCodec.encode(idCard);

        // 验证结果一致
        assertEquals(manualEncrypted, autoEncrypted, "手动加密和自动加密结果应该一致");

        // 验证解密也一致
        String autoDecrypted = encryptedCodec.decode(autoEncrypted);
        assertEquals(idCard, autoDecrypted, "自动解密应该正确");
    }
}