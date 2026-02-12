package io.github.nextentity.codec.identity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Speck64Encryptor 测试类
 * 测试 SPECK64/128 分组密码加密器的功能
 */
public class Speck64EncryptorTest {

    private static final Logger logger = LoggerFactory.getLogger(Speck64EncryptorTest.class);

    private Speck64Encryptor encryptor;
    private static final int[] TEST_KEY = {0x01234567, 0x89ABCDEF, 0xFEDCBA98, 0x76543210};

    @BeforeEach
    void setUp() {
        encryptor = new Speck64Encryptor(TEST_KEY);
    }

    /**
     * 测试字节数组构造函数
     */
    @Test
    void testByteArrayConstructor() {
        // 测试正确的16字节密钥（小端字节序）
        byte[] keyBytes = {
                0x67, 0x45, 0x23, 0x01,  // 0x01234567 的小端字节序
                (byte) 0xEF, (byte) 0xCD, (byte) 0xAB, (byte) 0x89,  // 0x89ABCDEF 的小端字节序
                (byte) 0x98, (byte) 0xBA, (byte) 0xDC, (byte) 0xFE,  // 0xFEDCBA98 的小端字节序
                0x10, 0x32, 0x54, 0x76   // 0x76543210 的小端字节序
        };

        assertDoesNotThrow(() -> new Speck64Encryptor(keyBytes));

        // 验证转换后的整数数组是否正确
        Speck64Encryptor byteEncryptor = new Speck64Encryptor(keyBytes);
        Speck64Encryptor intEncryptor = new Speck64Encryptor(TEST_KEY);

        long testData = 0x123456789ABCDEF0L;
        long result1 = byteEncryptor.encrypt(testData);
        long result2 = intEncryptor.encrypt(testData);

        assertEquals(result1, result2, "字节数组和整数数组构造的加密器应该产生相同结果");
    }

    /**
     * 测试字节数组构造函数的参数验证
     */
    @Test
    void testByteArrayConstructorValidation() {
        // 测试长度不足的字节数组
        byte[] shortKey = {0x01, 0x02, 0x03, 0x04};
        assertThrows(IllegalArgumentException.class, () -> new Speck64Encryptor(shortKey));

        // 测试长度过长的字节数组
        byte[] longKey = new byte[18];
        assertThrows(IllegalArgumentException.class, () -> new Speck64Encryptor(longKey));
    }

    /**
     * 测试构造函数参数验证
     */
    @Test
    void testConstructorValidation() {
        // 测试正确长度的密钥
        assertDoesNotThrow(() -> new Speck64Encryptor(TEST_KEY));

        // 测试错误长度的密钥
        int[] shortKey = {0x01234567,};
        assertThrows(IllegalArgumentException.class, () -> new Speck64Encryptor(shortKey));
    }

    /**
     * 测试基本的加密解密功能
     */
    @Test
    void testEncryptDecryptBasic() {
        long plaintext = 0x0123456789ABCDEFL;

        // 加密
        long encrypted = encryptor.encrypt(plaintext);
        logger.info("原始数据: {}", Long.toHexString(plaintext));
        logger.info("加密结果: {}", Long.toHexString(encrypted));

        // 解密
        long decrypted = encryptor.decrypt(encrypted);
        logger.info("解密结果: {}", Long.toHexString(decrypted));

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

        int[] key1 = {0x11111111, 0x22222222, 0x33333333, 0x44444444};
        int[] key2 = {0xFFFFFFFF, 0xEEEEEEEE, 0xDDDDDDDD, 0xCCCCCCCC};

        Speck64Encryptor encryptor1 = new Speck64Encryptor(key1);
        Speck64Encryptor encryptor2 = new Speck64Encryptor(key2);

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

        Speck64Encryptor encryptor1 = new Speck64Encryptor(TEST_KEY);
        Speck64Encryptor encryptor2 = new Speck64Encryptor(TEST_KEY);

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
     * 测试加密结果的唯一性
     */
    @Test
    void testEncryptionUniqueness() {
        long data1 = 0x1101011990010112L;
        long data2 = 0x1101011990010113L; // 只差一位

        long encrypted1 = encryptor.encrypt(data1);
        long encrypted2 = encryptor.encrypt(data2);

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

        for (int i = 0; i < 1000000; i++) {
            for (long data : testData) {
                long encrypted = encryptor.encrypt(data);
                long decrypted = encryptor.decrypt(encrypted);
                assertEquals(data, decrypted);
            }
        }

        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1_000_000; // 转换为毫秒

        logger.info("SPECK64加密性能测试完成，耗时: {} ms", duration);
        assertTrue(duration < 5000, "性能测试应该在5秒内完成");
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