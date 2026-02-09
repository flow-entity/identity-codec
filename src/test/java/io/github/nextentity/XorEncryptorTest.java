package io.github.nextentity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * XorEncryptor 测试类
 * 测试 XOR 加密器的加密和解密功能
 */
public class XorEncryptorTest {

    private XorEncryptor encryptor;
    private static final long TEST_KEY = 0x123456789ABCDEF0L;

    @BeforeEach
    void setUp() {
        encryptor = new XorEncryptor(TEST_KEY);
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
    }

    /**
     * 测试 XOR 加密的对称性：加密后再加密等于解密
     */
    @Test
    void testXorSymmetry() {
        long data = 0xAAAAAAAA55555555L;

        // 第一次加密
        long encrypted = encryptor.encrypt(data);
        // 第二次加密（相当于解密）
        long doubleEncrypted = encryptor.encrypt(encrypted);

        // 两次加密后应该回到原始数据
        assertEquals(data, doubleEncrypted, "XOR加密两次应该回到原始数据");

        // 同样，两次解密也应该回到原始数据
        long decrypted = encryptor.decrypt(data);
        long doubleDecrypted = encryptor.decrypt(decrypted);
        assertEquals(data, doubleDecrypted, "XOR解密两次应该回到原始数据");
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
                Long.MIN_VALUE
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

        XorEncryptor encryptor1 = new XorEncryptor(0x1111111111111111L);
        XorEncryptor encryptor2 = new XorEncryptor(0xFFFFFFFFFFFFFFFFL);

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

        XorEncryptor encryptor1 = new XorEncryptor(TEST_KEY);
        XorEncryptor encryptor2 = new XorEncryptor(TEST_KEY);

        long result1 = encryptor1.encrypt(data);
        long result2 = encryptor2.encrypt(data);

        assertEquals(result1, result2, "相同密钥应该产生相同的加密结果");
    }

    /**
     * 测试密钥为0时的行为（无加密效果）
     */
    @Test
    void testZeroKey() {
        XorEncryptor zeroKeyEncryptor = new XorEncryptor(0L);
        long data = 0x123456789ABCDEF0L;

        long encrypted = zeroKeyEncryptor.encrypt(data);
        long decrypted = zeroKeyEncryptor.decrypt(data);

        // 密钥为0时，加密解密都不改变数据
        assertEquals(data, encrypted, "密钥为0时加密不应该改变数据");
        assertEquals(data, decrypted, "密钥为0时解密不应该改变数据");
    }

    /**
     * 测试密钥为全1时的行为
     */
    @Test
    void testAllOnesKey() {
        XorEncryptor allOnesEncryptor = new XorEncryptor(-1L); // 0xFFFFFFFFFFFFFFFF
        long data = 0x0F0F0F0F0F0F0F0FL;

        long encrypted = allOnesEncryptor.encrypt(data);
        long expected = ~data; // XOR with all 1s is equivalent to bitwise NOT

        assertEquals(expected, encrypted, "密钥为全1时应该相当于按位取反");

        // 解密后应该回到原始数据
        long decrypted = allOnesEncryptor.decrypt(encrypted);
        assertEquals(data, decrypted, "解密后应该回到原始数据");
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
     * 测试 XOR 的性质：A ^ B ^ B = A
     */
    @Test
    void testXorProperty() {
        long a = 0x123456789ABCDEF0L;
        long b = TEST_KEY;

        // a ^ b
        long aXorB = a ^ b;
        // (a ^ b) ^ b = a
        long result = aXorB ^ b;

        assertEquals(a, result, "XOR性质验证：A ^ B ^ B 应该等于 A");
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

        for (int i = 0; i < 10000; i++) {
            for (long data : testData) {
                long encrypted = encryptor.encrypt(data);
                long decrypted = encryptor.decrypt(encrypted);
                assertEquals(data, decrypted);
            }
        }

        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1_000_000; // 转换为毫秒

        System.out.println("XOR加密性能测试完成，耗时: " + duration + " ms");
        assertTrue(duration < 5000, "性能测试应该在5秒内完成");
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
}
