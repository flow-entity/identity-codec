package io.github.nextentity.codec.identity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AesCtrEncryptor 测试类
 * 测试 AES/CTR 模式加密器的加密和解密功能
 */
public class AesCtrEncryptorTest {

    private AesCtrEncryptor encryptor;
    private static final byte[] TEST_KEY = new byte[]{
            0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07,
            0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F
    };

    @BeforeEach
    void setUp() {
        encryptor = new AesCtrEncryptor(TEST_KEY);
    }

    /**
     * 测试基本的加密解密功能
     */
    @Test
    void testEncryptDecryptBasic() {
        long plaintext = 0x0F0F0F0F0F0F0F0FL;

        // 加密
        long encrypted = encryptor.encrypt(plaintext);
        System.out.println("原始数据: " + Long.toHexString(plaintext));
        System.out.println("加密结果: " + Long.toHexString(encrypted));

        // 解密
        long decrypted = encryptor.decrypt(encrypted);
        System.out.println("解密结果: " + Long.toHexString(decrypted));

        // 验证解密后与原始数据一致
        assertEquals(plaintext, decrypted, "解密后应该与原始数据一致");
        assertNotEquals(plaintext, encrypted, "加密结果应该与原始数据不同");
    }

    /**
     * 测试加密和解密是互逆操作
     */
    @Test
    void testEncryptDecryptInverse() {
        long[] testData = {
                0L,
                -1L,  // 全1
                0x123456789ABCDEF0L,
                0xFEDCBA9876543210L,
                0x5555555555555555L,
                0xAAAAAAAAAAAAAAAAL,
                Long.MAX_VALUE,
                Long.MIN_VALUE,
                0x1101011990010112L,  // 类似身份证号的数据
                0x3101011985061523L
        };

        for (long data : testData) {
            long encrypted = encryptor.encrypt(data);
            long decrypted = encryptor.decrypt(encrypted);
            assertEquals(data, decrypted,
                    "数据 " + Long.toHexString(data) + " 加密解密后应该一致");
        }
    }

    /**
     * 测试不同的密钥产生不同的加密结果
     */
    @Test
    void testDifferentKeys() {
        long data = 0x123456789ABCDEF0L;

        byte[] key1 = new byte[16];
        Arrays.fill(key1, (byte) 0x11);
        AesCtrEncryptor encryptor1 = new AesCtrEncryptor(key1);

        byte[] key2 = new byte[16];
        Arrays.fill(key2, (byte) 0xFF);
        AesCtrEncryptor encryptor2 = new AesCtrEncryptor(key2);

        long result1 = encryptor1.encrypt(data);
        long result2 = encryptor2.encrypt(data);

        assertNotEquals(result1, result2, "不同密钥应该产生不同的加密结果");
    }

    /**
     * 测试相同密钥产生相同的加密结果
     */
    @Test
    void testSameKeyConsistency() {
        long data = 0x123456789ABCDEF0L;

        AesCtrEncryptor encryptor1 = new AesCtrEncryptor(TEST_KEY);
        AesCtrEncryptor encryptor2 = new AesCtrEncryptor(TEST_KEY);

        long result1 = encryptor1.encrypt(data);
        long result2 = encryptor2.encrypt(data);

        assertEquals(result1, result2, "相同密钥应该产生相同的加密结果");
    }

    /**
     * 测试边界值
     */
    @Test
    void testBoundaryValues() {
        long[] boundaryValues = {
                0L,                          // 最小值
                -1L,                         // 全1 (0xFFFFFFFFFFFFFFFF)
                Long.MAX_VALUE,              // 0x7FFFFFFFFFFFFFFF
                Long.MIN_VALUE,              // 0x8000000000000000
                0x0000000000000001L,         // 只有最低位为1
                0x8000000000000000L,         // 只有最高位为1
                0x5555555555555555L,         // 交替位
                0xAAAAAAAAAAAAAAAAL          // 交替位（反）
        };

        for (long value : boundaryValues) {
            long encrypted = encryptor.encrypt(value);
            long decrypted = encryptor.decrypt(encrypted);
            assertEquals(value, decrypted,
                    "边界值 " + Long.toHexString(value) + " 加密解密失败");
        }
    }

    /**
     * 测试字节数组加密解密功能
     */
    @Test
    void testByteArrayEncryptDecrypt() throws Exception {
        byte[] originalData = {0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08};

        // 加密
        byte[] encrypted = encryptor.encrypt(originalData);
        assertNotNull(encrypted, "加密结果不应为null");
        assertNotEquals(Arrays.toString(originalData), Arrays.toString(encrypted),
                "加密结果应该与原始数据不同");

        // 解密
        byte[] decrypted = encryptor.decrypt(encrypted);
        assertArrayEquals(originalData, decrypted, "解密后应该与原始数据一致");
    }

    /**
     * 测试不同长度的字节数组
     */
    @Test
    void testDifferentLengthByteArrays() throws Exception {
        byte[][] testData = {
                {},  // 空数组
                {0x01},
                {0x01, 0x02},
                {0x01, 0x02, 0x03, 0x04},
                {0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08},
                {0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A}
        };

        for (byte[] data : testData) {
            byte[] encrypted = encryptor.encrypt(data);
            byte[] decrypted = encryptor.decrypt(encrypted);
            assertArrayEquals(data, decrypted,
                    "长度为 " + data.length + " 的字节数组加密解密失败");
        }
    }

    /**
     * 测试异常情况 - 无效密钥长度
     */
    @Test
    void testInvalidKeyLength() {
        // 测试密钥长度不足16字节
        byte[] shortKey = new byte[8];
        assertThrows(IllegalArgumentException.class, () -> new AesCtrEncryptor(shortKey),
                "应该抛出异常当密钥长度不正确时");

        // 测试密钥长度超过16字节
        byte[] longKey = new byte[32];
        assertThrows(IllegalArgumentException.class, () -> new AesCtrEncryptor(longKey),
                "应该抛出异常当密钥长度超过16字节时");

        // 测试null密钥
        assertThrows(IllegalArgumentException.class, () -> new AesCtrEncryptor(null),
                "应该抛出异常当密钥为null时");
    }

    /**
     * 测试加密结果的唯一性
     */
    @Test
    void testEncryptionUniqueness() {
        long data1 = 0x1101011990010112L;
        long data2 = 0x1101011990010113L; // 只差一位

        long encrypted1 = encryptor.encrypt(data1);
        long encrypted2 = encryptor.decrypt(data2);

        assertNotEquals(encrypted1, encrypted2, "不同数据应该产生不同的加密结果");

        // 验证雪崩效应：输入改变一位，输出应该大幅改变
        long diff = encrypted1 ^ encrypted2;
        assertNotEquals(0L, diff, "加密结果差异不应该为0");
    }

    /**
     * 测试多次加密解密的一致性
     */
    @Test
    void testConsistency() {
        long data = 0x123456789ABCDEF0L;

        // 多次加密应该得到相同结果
        long encrypted1 = encryptor.encrypt(data);
        long encrypted2 = encryptor.encrypt(data);
        long encrypted3 = encryptor.encrypt(data);

        assertEquals(encrypted1, encrypted2, "同一数据多次加密应该一致");
        assertEquals(encrypted2, encrypted3, "同一数据多次加密应该一致");

        // 多次解密也应该得到相同结果
        long decrypted1 = encryptor.decrypt(encrypted1);
        long decrypted2 = encryptor.decrypt(encrypted1);

        assertEquals(decrypted1, decrypted2, "同一数据多次解密应该一致");
    }

    /**
     * 性能测试 - 批量加密解密
     */
    @Test
    void testBatchPerformance() {
        long[] testData = {
                0x1101011990010112L,
                0x3101011985061523L,
                0x4401011980121234L,
                0x5101011975032145L,
                0x1101011960081556L
        };

        long startTime = System.nanoTime();

        for (int i = 0; i < 1000; i++) {
            for (long data : testData) {
                long encrypted = encryptor.encrypt(data);
                long decrypted = encryptor.decrypt(encrypted);
                assertEquals(data, decrypted);
            }
        }

        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1_000_000; // 转换为毫秒

        System.out.println("AES/CTR加密性能测试完成，耗时: " + duration + " ms");
        assertTrue(duration < 5000, "性能测试应该在5秒内完成");
    }

    /**
     * 测试CTR模式的特性 - 相同明文产生不同密文（由于计数器递增）
     * 注意：由于使用固定IV，实际测试中会得到相同结果
     */
    @Test
    void testCtrModeCharacteristics() {
        long data = 0x123456789ABCDEF0L;

        // 在CTR模式下，使用相同密钥和IV，相同明文会产生相同密文
        long encrypted1 = encryptor.encrypt(data);
        long encrypted2 = encryptor.encrypt(data);

        assertEquals(encrypted1, encrypted2, "CTR模式下相同明文应该产生相同密文");
    }

    /**
     * 测试加密强度 - 验证加密结果看起来是随机的
     */
    @Test
    void testEncryptionRandomness() {
        long data = 0x123456789ABCDEF0L;
        long encrypted = encryptor.encrypt(data);

        // 加密结果不应该显示出明显的模式
        assertNotEquals(data, encrypted, "加密结果不应该等于原始数据");

        // 检查加密结果是否包含各种bit模式
        assertTrue(encrypted != 0, "加密结果不应该全为0");
        assertTrue(encrypted != -1, "加密结果不应该全为1");
    }
}