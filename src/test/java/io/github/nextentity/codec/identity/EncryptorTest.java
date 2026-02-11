package io.github.nextentity.codec.identity;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Encryptor 接口测试类
 * 测试加密器接口的契约和实现规范
 */
public class EncryptorTest {

    private static final Logger logger = LoggerFactory.getLogger(EncryptorTest.class);

    /**
     * 测试Speck64Encryptor实现了Encryptor接口
     */
    @Test
    void testSpeck64EncryptorImplementsInterface() {
        Encryptor encryptor = new Speck64Encryptor(new int[]{0x01234567, 0x89ABCDEF, 0xFEDCBA98, 0x76543210});
        
        assertNotNull(encryptor, "Speck64Encryptor实例不应该为null");
        assertTrue(encryptor instanceof Encryptor, "Speck64Encryptor应该实现Encryptor接口");
        
        logger.info("Speck64Encryptor接口实现验证通过");
    }

    /**
     * 测试加密器的基本契约：加密解密是可逆操作
     */
    @Test
    void testEncryptionDecryptionContract() {
        Encryptor encryptor = new Speck64Encryptor(new int[]{0x01234567, 0x89ABCDEF, 0xFEDCBA98, 0x76543210});
        
        long[] testValues = {
                0L,
                -1L,
                0x123456789ABCDEFL,
                Long.MAX_VALUE,
                Long.MIN_VALUE,
                0x5555555555555555L,
                0xAAAAAAAAAAAAAAAAL
        };
        
        for (long value : testValues) {
            long encrypted = encryptor.encrypt(value);
            long decrypted = encryptor.decrypt(encrypted);
            assertEquals(value, decrypted, 
                    "值 " + Long.toHexString(value) + " 加密解密后应该一致");
        }
        
        logger.info("加密解密契约测试通过");
    }

    /**
     * 测试加密器的确定性：相同输入和密钥产生相同输出
     */
    @Test
    void testDeterministicEncryption() {
        Encryptor encryptor = new Speck64Encryptor(new int[]{0x01234567, 0x89ABCDEF, 0xFEDCBA98, 0x76543210});
        
        long plaintext = 0x123456789ABCDEFL;
        
        long encrypted1 = encryptor.encrypt(plaintext);
        long encrypted2 = encryptor.encrypt(plaintext);
        long encrypted3 = encryptor.encrypt(plaintext);
        
        assertEquals(encrypted1, encrypted2, "相同输入应该产生相同的加密结果");
        assertEquals(encrypted2, encrypted3, "相同输入应该产生相同的加密结果");
        
        logger.info("确定性加密测试通过");
    }

    /**
     * 测试解密器的确定性：相同输入和密钥产生相同输出
     */
    @Test
    void testDeterministicDecryption() {
        Encryptor encryptor = new Speck64Encryptor(new int[]{0x01234567, 0x89ABCDEF, 0xFEDCBA98, 0x76543210});
        
        long ciphertext = 0xFEDCBA9876543210L;
        
        long decrypted1 = encryptor.decrypt(ciphertext);
        long decrypted2 = encryptor.decrypt(ciphertext);
        long decrypted3 = encryptor.decrypt(ciphertext);
        
        assertEquals(decrypted1, decrypted2, "相同输入应该产生相同的解密结果");
        assertEquals(decrypted2, decrypted3, "相同输入应该产生相同的解密结果");
        
        logger.info("确定性解密测试通过");
    }

    /**
     * 测试加密改变数据：加密结果应该与原始数据不同
     */
    @Test
    void testEncryptionChangesData() {
        Encryptor encryptor = new Speck64Encryptor(new int[]{0x01234567, 0x89ABCDEF, 0xFEDCBA98, 0x76543210});
        
        long[] testValues = {0L, 1L, -1L, 0x123456789ABCDEFL};
        
        for (long value : testValues) {
            long encrypted = encryptor.encrypt(value);
            assertNotEquals(value, encrypted, 
                    "加密结果应该与原始数据不同: " + Long.toHexString(value));
        }
        
        logger.info("加密改变数据测试通过");
    }

    /**
     * 测试不同密钥产生不同的加密结果
     */
    @Test
    void testDifferentKeysProduceDifferentResults() {
        long plaintext = 0x123456789ABCDEFL;
        
        Encryptor encryptor1 = new Speck64Encryptor(new int[]{0x11111111, 0x22222222, 0x33333333, 0x44444444});
        Encryptor encryptor2 = new Speck64Encryptor(new int[]{0xFFFFFFFF, 0xEEEEEEEE, 0xDDDDDDDD, 0xCCCCCCCC});
        
        long encrypted1 = encryptor1.encrypt(plaintext);
        long encrypted2 = encryptor2.encrypt(plaintext);
        
        assertNotEquals(encrypted1, encrypted2, "不同密钥应该产生不同的加密结果");
        
        // 但各自应该能正确解密
        assertEquals(plaintext, encryptor1.decrypt(encrypted1));
        assertEquals(plaintext, encryptor2.decrypt(encrypted2));
        
        logger.info("不同密钥产生不同结果测试通过");
    }

    /**
     * 测试雪崩效应：输入的微小变化应该导致输出的巨大变化
     */
    @Test
    void testAvalancheEffect() {
        Encryptor encryptor = new Speck64Encryptor(new int[]{0x01234567, 0x89ABCDEF, 0xFEDCBA98, 0x76543210});
        
        long plaintext1 = 0x123456789ABCDEFL;
        long plaintext2 = 0x123456789ABCDE0L; // 只改变最低4位
        
        long encrypted1 = encryptor.encrypt(plaintext1);
        long encrypted2 = encryptor.encrypt(plaintext2);
        
        long diff = encrypted1 ^ encrypted2;
        
        // 检查差异的位数（至少应该有16位不同）
        int bitCount = Long.bitCount(diff);
        assertTrue(bitCount >= 16, 
                "雪崩效应：输入的微小变化应该导致输出的巨大变化，实际不同位数: " + bitCount);
        
        logger.info("雪崩效应测试通过，不同位数: {}", bitCount);
    }

    /**
     * 创建一个简单的XOR加密器实现用于测试接口契约
     */
    private static class XorEncryptor implements Encryptor {
        private final long key;

        public XorEncryptor(long key) {
            this.key = key;
        }

        @Override
        public long encrypt(long plaintext) {
            return plaintext ^ key;
        }

        @Override
        public long decrypt(long ciphertext) {
            return ciphertext ^ key;
        }
    }

    /**
     * 测试自定义加密器实现
     */
    @Test
    void testCustomEncryptorImplementation() {
        Encryptor xorEncryptor = new XorEncryptor(0x123456789ABCDEFL);
        
        long plaintext = 0x1111111111111111L;
        
        // 测试加密
        long encrypted = xorEncryptor.encrypt(plaintext);
        long expectedEncrypted = plaintext ^ 0x123456789ABCDEFL;
        assertEquals(expectedEncrypted, encrypted, "XOR加密应该按预期工作");
        
        // 测试解密
        long decrypted = xorEncryptor.decrypt(encrypted);
        assertEquals(plaintext, decrypted, "XOR解密应该恢复原始数据");
        
        // 测试契约
        assertNotEquals(plaintext, encrypted, "加密结果应该与原始数据不同");
        
        logger.info("自定义加密器实现测试通过");
    }

    /**
     * 测试加密器的性能要求
     */
    @Test
    void testPerformanceRequirements() {
        Encryptor encryptor = new Speck64Encryptor(new int[]{0x01234567, 0x89ABCDEF, 0xFEDCBA98, 0x76543210});
        
        long[] testData = new long[10000];
        for (int i = 0; i < testData.length; i++) {
            testData[i] = i * 0x12345678L + 0x9ABCDEFL;
        }
        
        // 测试加密性能
        long startTime = System.nanoTime();
        for (long value : testData) {
            encryptor.encrypt(value);
        }
        long encryptTime = System.nanoTime() - startTime;
        
        // 测试解密性能
        startTime = System.nanoTime();
        for (long value : testData) {
            encryptor.decrypt(value);
        }
        long decryptTime = System.nanoTime() - startTime;
        
        logger.info("加密耗时: {} ms", encryptTime / 1_000_000);
        logger.info("解密耗时: {} ms", decryptTime / 1_000_000);
        
        // 性能要求：10000次操作应该在合理时间内完成（这里设置为5秒）
        assertTrue(encryptTime < 5_000_000_000L, "加密性能应该满足要求");
        assertTrue(decryptTime < 5_000_000_000L, "解密性能应该满足要求");
        
        logger.info("性能要求测试通过");
    }

    /**
     * 测试加密器的线程安全性
     */
    @Test
    void testThreadSafety() throws InterruptedException {
        final int threadCount = 10;
        final int iterationsPerThread = 1000;
        Thread[] threads = new Thread[threadCount];
        final boolean[] success = new boolean[threadCount];
        final Encryptor encryptor = new Speck64Encryptor(new int[]{0x01234567, 0x89ABCDEF, 0xFEDCBA98, 0x76543210});
        
        // 创建多个线程同时使用加密器
        for (int i = 0; i < threadCount; i++) {
            final int threadIndex = i;
            threads[i] = new Thread(() -> {
                try {
                    for (int j = 0; j < iterationsPerThread; j++) {
                        long value = threadIndex * 1000L + j;
                        long encrypted = encryptor.encrypt(value);
                        long decrypted = encryptor.decrypt(encrypted);
                        assertEquals(value, decrypted, 
                                "线程" + threadIndex + "迭代" + j + "失败");
                    }
                    success[threadIndex] = true;
                } catch (Exception e) {
                    success[threadIndex] = false;
                    logger.error("线程 {} 发生异常", threadIndex, e);
                }
            });
        }
        
        // 启动所有线程
        for (Thread thread : threads) {
            thread.start();
        }
        
        // 等待所有线程完成
        for (Thread thread : threads) {
            thread.join();
        }
        
        // 验证所有线程都成功
        for (int i = 0; i < threadCount; i++) {
            assertTrue(success[i], "线程 " + i + " 应该成功执行");
        }
        
        logger.info("线程安全性测试通过");
    }

    /**
     * 测试边界值处理
     */
    @Test
    void testBoundaryValues() {
        Encryptor encryptor = new Speck64Encryptor(new int[]{0x01234567, 0x89ABCDEF, 0xFEDCBA98, 0x76543210});
        
        long[] boundaryValues = {
                0L,                          // 最小值
                1L,                          // 最小正数
                -1L,                         // 最大负数
                Long.MAX_VALUE,              // 最大正数
                Long.MIN_VALUE,              // 最小负数
                0x8000000000000000L,         // 只有符号位
                0x7FFFFFFFFFFFFFFFL,         // 除符号位外全为1
                0x0000000000000001L,         // 只有最低位
                0x8000000000000001L          // 符号位和最低位
        };
        
        for (long value : boundaryValues) {
            long encrypted = encryptor.encrypt(value);
            long decrypted = encryptor.decrypt(encrypted);
            assertEquals(value, decrypted, 
                    "边界值 " + Long.toHexString(value) + " 加密解密后应该一致");
            
            // 加密结果应该与原始数据不同（除了可能的特殊情况）
            assertNotEquals(value, encrypted, 
                    "边界值 " + Long.toHexString(value) + " 加密结果应该与原始数据不同");
        }
        
        logger.info("边界值处理测试通过");
    }

    /**
     * 测试加密器的对称性：使用相同密钥的加密器应该产生一致的结果
     */
    @Test
    void testSymmetricEncryption() {
        int[] key = {0x01234567, 0x89ABCDEF, 0xFEDCBA98, 0x76543210};
        
        Encryptor encryptor1 = new Speck64Encryptor(key);
        Encryptor encryptor2 = new Speck64Encryptor(key);
        
        long plaintext = 0x123456789ABCDEFL;
        
        long encrypted1 = encryptor1.encrypt(plaintext);
        long encrypted2 = encryptor2.encrypt(plaintext);
        
        assertEquals(encrypted1, encrypted2, "使用相同密钥的加密器应该产生相同的加密结果");
        
        // 解密也应该一致
        assertEquals(plaintext, encryptor1.decrypt(encrypted1));
        assertEquals(plaintext, encryptor2.decrypt(encrypted2));
        
        // 交叉解密也应该成功
        assertEquals(plaintext, encryptor1.decrypt(encrypted2));
        assertEquals(plaintext, encryptor2.decrypt(encrypted1));
        
        logger.info("对称加密测试通过");
    }
}