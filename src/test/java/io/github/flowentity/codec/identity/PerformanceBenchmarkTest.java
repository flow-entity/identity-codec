package io.github.flowentity.codec.identity;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 性能基准测试类
 * <p>
 * 专门用于测试Identity Codec库的各项性能指标，包括：
 * <pre>
 * - 身份证号码解析性能
 * - 编码解码性能
 * - 加密解密性能
 * - 内存使用情况
 * - 并发性能
 * </pre>
 *
 * @version 1.0
 */
class PerformanceBenchmarkTest {

    private static final Logger logger = LoggerFactory.getLogger(PerformanceBenchmarkTest.class);

    // 测试配置
    private static final int WARMUP_ITERATIONS = 10_000;
    private static final int BENCHMARK_ITERATIONS = 1_000_000;
    private static final int CONCURRENT_THREADS = 10;
    private static final int CONCURRENT_ITERATIONS = 100_000;

    // 测试数据
    private static String[] testIdCards;
    private static IdentityNumber[] testIdentityNumbers;
    private static long[] encodedValues;
    private static long[] encryptedValues;

    private final IdentityCodec simpleCodec = IdentityCodecs.simple();
    private final IdentityCodec encryptedCodec = IdentityCodecs.speck64Encrypt(TestConstants.DEFAULT_ENCRYPTION_KEY);

    @BeforeAll
    static void setUp() {
        logger.info("=== 性能测试初始化 ===");

        // 初始化测试数据
        initializeTestData();

        // 预热JVM
        warmUpJVM();

        logger.info("性能测试环境准备完成");
    }

    /**
     * 初始化测试数据
     */
    private static void initializeTestData() {
        testIdCards = new String[]{
                TestConstants.VALID_ID_CARD_WITH_X,
                TestConstants.VALID_ID_CARD_NUMERIC,
                TestConstants.SHANGHAI_ID_CARD,
                TestConstants.GUANGZHOU_ID_CARD,
                TestConstants.CHENGDU_ID_CARD
        };

        testIdentityNumbers = new IdentityNumber[testIdCards.length];
        encodedValues = new long[testIdCards.length];
        encryptedValues = new long[testIdCards.length];

        for (int i = 0; i < testIdCards.length; i++) {
            testIdentityNumbers[i] = IdentityNumber.parse(testIdCards[i]);
            encodedValues[i] = IdentityCodecs.simple().encode(testIdentityNumbers[i]);
            encryptedValues[i] = IdentityCodecs.speck64Encrypt(TestConstants.DEFAULT_ENCRYPTION_KEY)
                    .encode(testIdentityNumbers[i]);
        }
    }

    /**
     * JVM预热
     */
    private static void warmUpJVM() {
        logger.info("开始JVM预热...");

        // 预热身份证解析
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            IdentityNumber.parse(testIdCards[i % testIdCards.length]);
        }

        // 预热编码
        IdentityCodec codec = IdentityCodecs.simple();
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            codec.encode(testIdentityNumbers[i % testIdentityNumbers.length]);
        }

        // 预热解码
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            codec.decode(encodedValues[i % encodedValues.length]);
        }

        logger.info("JVM预热完成");
    }

    // ==================== 身份证解析性能测试 ====================

    @Test
    void testIdentityNumberParsePerformance() {
        logger.info("=== 身份证号码解析性能测试 ===");

        long startTime = System.nanoTime();

        for (int i = 0; i < BENCHMARK_ITERATIONS; i++) {
            IdentityNumber id = IdentityNumber.parse(testIdCards[i % testIdCards.length]);
            assertNotNull(id);
        }

        long endTime = System.nanoTime();
        long duration = endTime - startTime;
        double avgTime = (double) duration / BENCHMARK_ITERATIONS;
        double throughput = 1_000_000_000.0 / avgTime;

        logger.info("解析性能测试结果:");
        logger.info("  总迭代次数: {}", BENCHMARK_ITERATIONS);
        logger.info("  总耗时: {} ms", duration / 1_000_000);
        logger.info("  平均耗时: {} ns/op", String.format("%.2f", avgTime));
        logger.info("  吞吐量: {} ops/sec", String.format("%.0f", throughput));

        // 性能断言
        assertTrue(avgTime < 1000, "平均解析时间应小于1000纳秒");
        assertTrue(throughput > 500_000, "吞吐量应大于50万ops/sec");

        logger.info("身份证号码解析性能测试完成");
    }

    // ==================== 编码性能测试 ====================

    @Test
    void testEncodingPerformance() {
        logger.info("=== 编码性能测试 ===");

        long startTime = System.nanoTime();

        for (int i = 0; i < BENCHMARK_ITERATIONS; i++) {
            long encoded = simpleCodec.encode(testIdentityNumbers[i % testIdentityNumbers.length]);
            assertTrue(encoded != 0);
        }

        long endTime = System.nanoTime();
        long duration = endTime - startTime;
        double avgTime = (double) duration / BENCHMARK_ITERATIONS;
        double throughput = 1_000_000_000.0 / avgTime;

        logger.info("编码性能测试结果:");
        logger.info("  总迭代次数: {}", BENCHMARK_ITERATIONS);
        logger.info("  总耗时: {} ms", duration / 1_000_000);
        logger.info("  平均耗时: {} ns/op", String.format("%.2f", avgTime));
        logger.info("  吞吐量: {} ops/sec", String.format("%.0f", throughput));

        // 性能断言
        assertTrue(avgTime < 2000, "平均编码时间应小于2000纳秒");
        assertTrue(throughput > 250_000, "吞吐量应大于25万ops/sec");

        logger.info("编码性能测试完成");
    }

    // ==================== 解码性能测试 ====================

    @Test
    void testDecodingPerformance() {
        logger.info("=== 解码性能测试 ===");

        long startTime = System.nanoTime();

        for (int i = 0; i < BENCHMARK_ITERATIONS; i++) {
            IdentityNumber decoded = simpleCodec.decode(encodedValues[i % encodedValues.length]);
            assertNotNull(decoded);
        }

        long endTime = System.nanoTime();
        long duration = endTime - startTime;
        double avgTime = (double) duration / BENCHMARK_ITERATIONS;
        double throughput = 1_000_000_000.0 / avgTime;

        logger.info("解码性能测试结果:");
        logger.info("  总迭代次数: {}", BENCHMARK_ITERATIONS);
        logger.info("  总耗时: {} ms", duration / 1_000_000);
        logger.info("  平均耗时: {} ns/op", String.format("%.2f", avgTime));
        logger.info("  吞吐量: {} ops/sec", String.format("%.0f", throughput));

        // 性能断言
        assertTrue(avgTime < 2000, "平均解码时间应小于2000纳秒");
        assertTrue(throughput > 250_000, "吞吐量应大于25万ops/sec");

        logger.info("解码性能测试完成");
    }

    // ==================== 加密编码性能测试 ====================

    @Test
    void testEncryptedEncodingPerformance() {
        logger.info("=== 加密编码性能测试 ===");

        long startTime = System.nanoTime();

        for (int i = 0; i < BENCHMARK_ITERATIONS / 2; i++) { // 减少迭代次数因为加密较慢
            long encrypted = encryptedCodec.encode(testIdentityNumbers[i % testIdentityNumbers.length]);
            assertTrue(encrypted != 0);
        }

        long endTime = System.nanoTime();
        long iterations = BENCHMARK_ITERATIONS / 2;
        long duration = endTime - startTime;
        double avgTime = (double) duration / iterations;
        double throughput = 1_000_000_000.0 / avgTime;

        logger.info("加密编码性能测试结果:");
        logger.info("  总迭代次数: {}", iterations);
        logger.info("  总耗时: {} ms", duration / 1_000_000);
        logger.info("  平均耗时: {} ns/op", String.format("%.2f", avgTime));
        logger.info("  吞吐量: {} ops/sec", String.format("%.0f", throughput));

        // 性能断言
        assertTrue(avgTime < 5000, "平均加密编码时间应小于5000纳秒");
        assertTrue(throughput > 100_000, "吞吐量应大于10万ops/sec");

        logger.info("加密编码性能测试完成");
    }

    // ==================== 解密性能测试 ====================

    @Test
    void testEncryptedDecodingPerformance() {
        logger.info("=== 解密性能测试 ===");

        long startTime = System.nanoTime();

        for (int i = 0; i < BENCHMARK_ITERATIONS / 2; i++) { // 减少迭代次数因为解密较慢
            IdentityNumber decrypted = encryptedCodec.decode(encryptedValues[i % encryptedValues.length]);
            assertNotNull(decrypted);
        }

        long endTime = System.nanoTime();
        long iterations = BENCHMARK_ITERATIONS / 2;
        long duration = endTime - startTime;
        double avgTime = (double) duration / iterations;
        double throughput = 1_000_000_000.0 / avgTime;

        logger.info("解密性能测试结果:");
        logger.info("  总迭代次数: {}", iterations);
        logger.info("  总耗时: {} ms", duration / 1_000_000);
        logger.info("  平均耗时: {} ns/op", String.format("%.2f", avgTime));
        logger.info("  吞吐量: {} ops/sec", String.format("%.0f", throughput));

        // 性能断言
        assertTrue(avgTime < 5000, "平均解密时间应小于5000纳秒");
        assertTrue(throughput > 100_000, "吞吐量应大于10万ops/sec");

        logger.info("解密性能测试完成");
    }

    // ==================== 批量处理性能测试 ====================

    @Test
    void testBatchProcessingPerformance() {
        logger.info("=== 批量处理性能测试 ===");

        final int batchSize = 1000;
        long[] batchEncoded = new long[batchSize];
        IdentityNumber[] batchDecoded = new IdentityNumber[batchSize];

        long startTime = System.nanoTime();

        // 批量编码
        for (int batch = 0; batch < BENCHMARK_ITERATIONS / batchSize; batch++) {
            for (int i = 0; i < batchSize; i++) {
                batchEncoded[i] = simpleCodec.encode(testIdentityNumbers[(batch * batchSize + i) % testIdentityNumbers.length]);
            }

            // 批量解码
            for (int i = 0; i < batchSize; i++) {
                batchDecoded[i] = simpleCodec.decode(batchEncoded[i]);
            }

            // 验证结果
            for (int i = 0; i < batchSize; i++) {
                assertEquals(testIdentityNumbers[(batch * batchSize + i) % testIdentityNumbers.length].number(),
                        batchDecoded[i].number());
            }
        }

        long endTime = System.nanoTime();
        long totalOperations = (BENCHMARK_ITERATIONS / batchSize) * batchSize * 2; // 编码+解码
        long duration = endTime - startTime;
        double avgTime = (double) duration / totalOperations;
        double throughput = 1_000_000_000.0 / avgTime;

        logger.info("批量处理性能测试结果:");
        logger.info("  批量大小: {}", batchSize);
        logger.info("  总操作数: {}", totalOperations);
        logger.info("  总耗时: {} ms", duration / 1_000_000);
        logger.info("  平均耗时: {} ns/op", String.format("%.2f", avgTime));
        logger.info("  吞吐量: {} ops/sec", String.format("%.0f", throughput));

        logger.info("批量处理性能测试完成");
    }

    // ==================== 内存使用测试 ====================

    @Test
    void testMemoryUsage() {
        logger.info("=== 内存使用测试 ===");

        Runtime runtime = Runtime.getRuntime();

        // 强制垃圾回收
        System.gc();
        long initialMemory = runtime.totalMemory() - runtime.freeMemory();

        // 创建大量对象
        IdentityNumber[] numbers = new IdentityNumber[1_000_000];
        for (int i = 0; i < numbers.length; i++) {
            numbers[i] = IdentityNumber.parse(testIdCards[i % testIdCards.length]);
        }

        long afterCreation = runtime.totalMemory() - runtime.freeMemory();
        long memoryUsed = afterCreation - initialMemory;
        double avgMemoryPerObject = (double) memoryUsed / numbers.length;

        logger.info("内存使用测试结果:");
        logger.info("  创建对象数量: {}", numbers.length);
        logger.info("  初始内存使用: {} KB", initialMemory / 1024);
        logger.info("  创建后内存使用: {} KB", afterCreation / 1024);
        logger.info("  内存增长: {} KB", memoryUsed / 1024);
        logger.info("  平均每个对象内存: {} bytes", String.format("%.2f", avgMemoryPerObject));

        // 内存使用断言 - 放宽限制
        assertTrue(avgMemoryPerObject < 200, "平均每个IdentityNumber对象内存应小于200字节, 实际：" + avgMemoryPerObject + "字节");

        // 清理引用
        for (int i = 0; i < numbers.length; i++) {
            numbers[i] = null;
        }
        System.gc();

        logger.info("内存使用测试完成");
    }

    // ==================== 并发性能测试 ====================

    @Test
    void testConcurrentPerformance() throws InterruptedException {
        logger.info("=== 并发性能测试 ===");

        Thread[] threads = new Thread[CONCURRENT_THREADS];
        long[] threadTimes = new long[CONCURRENT_THREADS];

        long startTime = System.nanoTime();

        // 创建并发线程
        for (int t = 0; t < CONCURRENT_THREADS; t++) {
            final int threadId = t;
            threads[t] = new Thread(() -> {
                long threadStart = System.nanoTime();

                for (int i = 0; i < CONCURRENT_ITERATIONS; i++) {
                    // 随机选择操作类型
                    int operation = ThreadLocalRandom.current().nextInt(4);
                    switch (operation) {
                        case 0: // 解析
                            IdentityNumber id = IdentityNumber.parse(testIdCards[i % testIdCards.length]);
                            assertNotNull(id);
                            break;
                        case 1: // 编码
                            long encoded = simpleCodec.encode(testIdentityNumbers[i % testIdentityNumbers.length]);
                            assertTrue(encoded != 0);
                            break;
                        case 2: // 解码
                            IdentityNumber decoded = simpleCodec.decode(encodedValues[i % encodedValues.length]);
                            assertNotNull(decoded);
                            break;
                        case 3: // 加密编码
                            long encrypted = encryptedCodec.encode(testIdentityNumbers[i % testIdentityNumbers.length]);
                            assertTrue(encrypted != 0);
                            break;
                    }
                }

                threadTimes[threadId] = System.nanoTime() - threadStart;
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

        long endTime = System.nanoTime();
        long totalDuration = endTime - startTime;
        long totalOperations = (long) CONCURRENT_THREADS * CONCURRENT_ITERATIONS;

        double avgTime = (double) totalDuration / totalOperations;
        double throughput = 1_000_000_000.0 / avgTime;

        logger.info("并发性能测试结果:");
        logger.info("  并发线程数: {}", CONCURRENT_THREADS);
        logger.info("  每线程迭代次数: {}", CONCURRENT_ITERATIONS);
        logger.info("  总操作数: {}", totalOperations);
        logger.info("  总耗时: {} ms", totalDuration / 1_000_000);
        logger.info("  平均耗时: {} ns/op", String.format("%.2f", avgTime));
        logger.info("  吞吐量: {} ops/sec", String.format("%.0f", throughput));

        // 显示各线程性能
        for (int i = 0; i < CONCURRENT_THREADS; i++) {
            logger.info("  线程{}耗时: {} ms", i, threadTimes[i] / 1_000_000);
        }

        // 并发性能断言
        assertTrue(avgTime < 5000, "并发平均操作时间应小于5000纳秒");
        assertTrue(throughput > 100_000, "并发吞吐量应大于10万ops/sec");

        logger.info("并发性能测试完成");
    }

    // ==================== 压力测试 ====================

    @Test
    void testStressPerformance() {
        logger.info("=== 压力测试 ===");

        final int stressIterations = 10_000_000;
        long startTime = System.nanoTime();

        for (int i = 0; i < stressIterations; i++) {
            // 混合操作：解析->编码->解码
            IdentityNumber id = IdentityNumber.parse(testIdCards[i % testIdCards.length]);
            long encoded = simpleCodec.encode(id);
            IdentityNumber decoded = simpleCodec.decode(encoded);

            // 验证数据一致性
            assertEquals(id.number(), decoded.number());
        }

        long endTime = System.nanoTime();
        long duration = endTime - startTime;
        double avgTime = (double) duration / stressIterations;
        double throughput = 1_000_000_000.0 / avgTime;

        logger.info("压力测试结果:");
        logger.info("  总迭代次数: {}", stressIterations);
        logger.info("  总耗时: {} ms", duration / 1_000_000);
        logger.info("  平均耗时: {} ns/op", String.format("%.2f", avgTime));
        logger.info("  吞吐量: {} ops/sec", String.format("%.0f", throughput));

        // 压力测试断言
        assertTrue(avgTime < 3000, "压力测试平均时间应小于3000纳秒");

        logger.info("压力测试完成");
    }
}