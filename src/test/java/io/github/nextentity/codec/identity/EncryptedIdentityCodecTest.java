package io.github.nextentity.codec.identity;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;

/**
 * EncryptedIdentityCodec 测试类
 * 测试加密身份编码器的功能，包括编码解码、异常处理等
 */
public class EncryptedIdentityCodecTest {

    private static final Logger logger = LoggerFactory.getLogger(EncryptedIdentityCodecTest.class);

    private IdentityCodec simpleCodec;
    private Encryptor mockEncryptor;
    private EncryptedIdentityCodec encryptedCodec;
    private static final int[] TEST_KEY = {0x01234567, 0x89ABCDEF, 0xFEDCBA98, 0x76543210};

    @BeforeEach
    void setUp() {
        simpleCodec = IdentityCodecs.simple();
        mockEncryptor = new Speck64Encryptor(TEST_KEY);
        encryptedCodec = new EncryptedIdentityCodec(simpleCodec, mockEncryptor);
    }

    /**
     * 测试构造函数参数验证
     */
    @Test
    void testConstructorValidation() {
        // 测试null codec参数
        assertThrows(NullPointerException.class, () ->
                new EncryptedIdentityCodec(null, mockEncryptor),
                "null codec应该抛出NullPointerException");

        // 测试null encryptor参数
        assertThrows(NullPointerException.class, () ->
                new EncryptedIdentityCodec(simpleCodec, null),
                "null encryptor应该抛出NullPointerException");

        // 测试两个参数都为null
        assertThrows(NullPointerException.class, () ->
                new EncryptedIdentityCodec(null, null),
                "两个参数都为null应该抛出NullPointerException");

        logger.info("构造函数参数验证测试通过");
    }

    /**
     * 测试基本的加密编码和解密功能
     */
    @Test
    void testBasicEncryptDecrypt() {
        String idCard = TestConstants.VALID_ID_CARD_WITH_X;

        // 编码
        long encrypted = encryptedCodec.encode(idCard);
        logger.info("原始身份证: {}", idCard);
        logger.info("加密编码结果: {}", Long.toHexString(encrypted));

        // 解码
        String decoded = encryptedCodec.decode(encrypted);
        logger.info("解密结果: {}", decoded);

        // 验证
        assertEquals(idCard, decoded, "编码解码后应该与原始身份证号码一致");
    }

    /**
     * 测试加密结果与简单编码结果的差异
     */
    @Test
    void testEncryptedResultDifferentFromSimple() {
        String idCard = "11010519491231002X";

        // 简单编码结果
        long simpleEncoded = simpleCodec.encode(idCard);

        // 加密编码结果
        long encryptedEncoded = encryptedCodec.encode(idCard);

        assertNotEquals(simpleEncoded, encryptedEncoded, 
                "加密编码结果应该与简单编码结果不同");
    }

    /**
     * 测试不同身份证的加密结果唯一性
     */
    @Test
    void testEncryptionUniqueness() {
        String idCard1 = "11010519491231002X";
        String idCard2 = "110101199001011237";

        long encrypted1 = encryptedCodec.encode(idCard1);
        long encrypted2 = encryptedCodec.encode(idCard2);

        assertNotEquals(encrypted1, encrypted2, 
                "不同的身份证应该产生不同的加密结果");

        // 验证各自能正确解密
        assertEquals(idCard1, encryptedCodec.decode(encrypted1));
        assertEquals(idCard2, encryptedCodec.decode(encrypted2));
    }

    /**
     * 测试相同身份证多次编码的一致性
     */
    @Test
    void testEncodingConsistency() {
        String idCard = "11010519491231002X";

        long encrypted1 = encryptedCodec.encode(idCard);
        long encrypted2 = encryptedCodec.encode(idCard);
        long encrypted3 = encryptedCodec.encode(idCard);

        assertEquals(encrypted1, encrypted2, "同一身份证多次编码应该一致");
        assertEquals(encrypted2, encrypted3, "同一身份证多次编码应该一致");
    }

    /**
     * 测试各种有效的身份证号码
     */
    @Test
    void testVariousValidIdCards() {
        String[] validIds = {
                "11010519491231002X",  // 带X的身份证
                "110101199001011237",  // 普通身份证
                "310101198506152345",  // 上海身份证
                "440101198012123455",  // 广州身份证
                "510101197503214566"   // 成都身份证
        };

        for (String id : validIds) {
            long encrypted = encryptedCodec.encode(id);
            String decrypted = encryptedCodec.decode(encrypted);
            assertEquals(id, decrypted, "身份证 " + id + " 应该能正确编解码");
        }

        logger.info("各种有效身份证测试通过");
    }

    /**
     * 测试边界情况的身份证号码
     */
    @Test
    void testBoundaryIdCards() {
        String[] boundaryIds = {
                "110101000001011236", // 基准日期0000-01-01
                "110101000101021239", // 基准日期后一天
                "110101999912311236", // 9999年12月31日
                "110101199001010015", // 最小顺序码
                "110101199001019992"  // 大顺序码
        };

        for (String id : boundaryIds) {
            long encrypted = encryptedCodec.encode(id);
            String decrypted = encryptedCodec.decode(encrypted);
            assertEquals(id, decrypted, "边界身份证 " + id + " 应该能正确编解码");
        }

        logger.info("边界身份证测试通过");
    }

    /**
     * 测试加密器异常传播
     */
    @Test
    void testEncryptorExceptionPropagation() {
        // 创建一个会抛出异常的模拟加密器
        Encryptor faultyEncryptor = new Encryptor() {
            @Override
            public long encrypt(long plaintext) {
                throw new RuntimeException("模拟加密失败");
            }

            @Override
            public long decrypt(long ciphertext) {
                throw new RuntimeException("模拟解密失败");
            }
        };

        EncryptedIdentityCodec faultyCodec = new EncryptedIdentityCodec(simpleCodec, faultyEncryptor);
        String idCard = "11010519491231002X";

        // 测试编码时的异常传播
        assertThrows(RuntimeException.class, () -> faultyCodec.encode(idCard),
                "加密器异常应该被传播到编码方法");

        // 测试解码时的异常传播
        assertThrows(RuntimeException.class, () -> faultyCodec.decode(12345L),
                "加密器异常应该被传播到解码方法");

        logger.info("加密器异常传播测试通过");
    }

    /**
     * 测试底层编解码器异常传播
     */
    @Test
    void testCodecExceptionPropagation() {
        // 创建一个会抛出异常的模拟编解码器
        IdentityCodec faultyCodec = new IdentityCodec() {
            @Override
            public long encode(@NonNull String identityNumber) {
                throw new RuntimeException("模拟编码失败");
            }

            @Override
            public @NonNull String decode(long encoded) {
                throw new RuntimeException("模拟解码失败");
            }
        };

        EncryptedIdentityCodec faultyEncryptedCodec = new EncryptedIdentityCodec(faultyCodec, mockEncryptor);
        String idCard = "11010519491231002X";

        // 测试编码时的异常传播
        assertThrows(RuntimeException.class, () -> faultyEncryptedCodec.encode(idCard),
                "编解码器异常应该被传播到编码方法");

        // 测试解码时的异常传播
        assertThrows(RuntimeException.class, () -> faultyEncryptedCodec.decode(12345L),
                "编解码器异常应该被传播到解码方法");

        logger.info("编解码器异常传播测试通过");
    }

    /**
     * 测试不同加密器的兼容性
     */
    @Test
    void testDifferentEncryptorsCompatibility() {
        String idCard = "11010519491231002X";
        
        // 使用不同的密钥创建不同的加密器
        Encryptor encryptor1 = new Speck64Encryptor(new int[]{0x11111111, 0x22222222, 0x33333333, 0x44444444});
        Encryptor encryptor2 = new Speck64Encryptor(new int[]{0xFFFFFFFF, 0xEEEEEEEE, 0xDDDDDDDD, 0xCCCCCCCC});
        
        EncryptedIdentityCodec codec1 = new EncryptedIdentityCodec(simpleCodec, encryptor1);
        EncryptedIdentityCodec codec2 = new EncryptedIdentityCodec(simpleCodec, encryptor2);
        
        long encrypted1 = codec1.encode(idCard);
        long encrypted2 = codec2.encode(idCard);
        
        // 不同加密器应该产生不同的加密结果
        assertNotEquals(encrypted1, encrypted2, "不同加密器应该产生不同的加密结果");
        
        // 但各自应该能正确解密
        assertEquals(idCard, codec1.decode(encrypted1));
        assertEquals(idCard, codec2.decode(encrypted2));
        
        // 验证不能跨密钥解密
        assertThrows(Exception.class, () -> codec2.decode(encrypted1),
                "不能用不同的密钥解密");
        
        logger.info("不同加密器兼容性测试通过");
    }

    /**
     * 测试XOR加密器（简单测试用加密器）
     */
    @Test
    void testXorEncryptorIntegration() {
        // 创建一个简单的XOR加密器用于测试
        Encryptor xorEncryptor = new Encryptor() {
            private static final long XOR_KEY = 0x123456789ABCDEFL;

            @Override
            public long encrypt(long plaintext) {
                return plaintext ^ XOR_KEY;
            }

            @Override
            public long decrypt(long ciphertext) {
                return ciphertext ^ XOR_KEY;
            }
        };

        EncryptedIdentityCodec xorCodec = new EncryptedIdentityCodec(simpleCodec, xorEncryptor);
        String idCard = "11010519491231002X";

        long encrypted = xorCodec.encode(idCard);
        String decrypted = xorCodec.decode(encrypted);

        assertEquals(idCard, decrypted, "XOR加密器应该能正确编解码");

        // 验证加密结果确实与简单编码不同
        long simpleEncoded = simpleCodec.encode(idCard);
        assertNotEquals(simpleEncoded, encrypted, "XOR加密结果应该与简单编码结果不同");

        logger.info("XOR加密器集成测试通过");
    }

    /**
     * 测试性能
     */
    @Test
    void testPerformance() {
        String[] testIds = {
                "11010519491231002X",
                "110101199001011237",
                "310101198506152345",
                "440101198012123455",
                "510101197503214566"
        };

        long startTime = System.nanoTime();

        // 批量处理
        for (int i = 0; i < 100000; i++) {
            for (String id : testIds) {
                long encrypted = encryptedCodec.encode(id);
                String decrypted = encryptedCodec.decode(encrypted);
                assertEquals(id, decrypted);
            }
        }

        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1_000_000; // 转换为毫秒

        logger.info("加密身份编码器性能测试完成，耗时: {} ms", duration);
        assertTrue(duration < 10000, "性能测试应该在10秒内完成");
    }

    /**
     * 测试线程安全性
     */
    @Test
    void testThreadSafety() throws InterruptedException {
        final int threadCount = 10;
        final int iterationsPerThread = 1000;
        Thread[] threads = new Thread[threadCount];
        final boolean[] success = new boolean[threadCount];
        final String testId = "11010519491231002X";

        // 创建多个线程同时使用加密编码器
        for (int i = 0; i < threadCount; i++) {
            final int threadIndex = i;
            threads[i] = new Thread(() -> {
                try {
                    for (int j = 0; j < iterationsPerThread; j++) {
                        long encrypted = encryptedCodec.encode(testId);
                        String decrypted = encryptedCodec.decode(encrypted);
                        assertEquals(testId, decrypted, "线程" + threadIndex + "迭代" + j + "失败");
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
     * 测试空输入处理
     */
    @Test
    void testNullInputHandling() {
        assertThrows(NullPointerException.class, () -> encryptedCodec.encode(null),
                "null身份证应该抛出NullPointerException");

        // 解码null long是可以的，但会因解密失败而抛出异常
        assertThrows(Exception.class, () -> encryptedCodec.decode(0L),
                "解码0应该因解密失败而抛出异常");

        logger.info("空输入处理测试通过");
    }
}