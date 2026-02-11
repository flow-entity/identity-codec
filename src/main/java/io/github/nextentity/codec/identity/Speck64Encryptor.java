package io.github.nextentity.codec.identity;

import org.jspecify.annotations.NonNull;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * SPECK64分组密码加密器实现
 * <p>
 * SPECK是一种轻量级分组密码算法，专为受限环境设计。
 * SPECK64/128表示64位分组大小和128位密钥长度。
 * <p>
 * 算法特点：
 * <pre>
 * - 分组大小：64位
 * - 密钥长度：128位（4个32位整数）
 * - 轮数：27轮
 * - 旋转位数：右移8位，左移3位
 * - 结构：Feistel网络变体
 * </pre>
 * <p>
 * 工作原理：
 * <pre>
 * 1. 将64位明文分为两个32位部分
 * 2. 通过27轮Feistel变换进行加密
 * 3. 每轮使用不同的子密钥
 * 4. 子密钥通过密钥调度算法生成
 * </pre>
 *
 * @see <a href="https://en.wikipedia.org/wiki/Speck_(cipher)">SPECK Cipher Wikipedia</a>
 */
public class Speck64Encryptor implements Encryptor {

    /**
     * 每轮的加密密钥数组
     */
    private final int[] keys;

    /**
     * 右旋转位数
     * 标准SPECK64使用8位右旋转
     */
    private final int alpha;

    /**
     * 左旋转位数
     * 标准SPECK64使用3位左旋转
     */
    private final int beta;

    /**
     * 字节数组构造函数
     * 将128位字节数组密钥转换为4个32位整数数组
     *
     * @param key 128位加密密钥字节数组，长度必须为16字节
     * @throws IllegalArgumentException 当字节数组长度不为16时抛出
     */
    public Speck64Encryptor(byte @NonNull [] key) {
        int[] intKey = bytesToInts(key);
        this(27, 8, 3, intKey);
    }

    /**
     * 默认构造函数
     * 使用SPECK64/128标准参数：27轮，右旋8位，左旋3位
     *
     * @param key 128位加密密钥，必须包含4个32位整数
     * @throws IllegalArgumentException 当密钥数组长度不为4时抛出
     */
    public Speck64Encryptor(int @NonNull [] key) {
        if (key.length != 4) {
            throw new IllegalArgumentException("SPECK64/128 requires exactly 4 integers (128 bits) as key");
        }
        this(27, 8, 3, key);
    }

    /**
     * 自定义参数构造函数
     * 允许自定义SPECK算法的各种参数
     * <p>
     * <strong>参数要求和影响：</strong>
     * <pre>
     * rounds（轮数）：
     *   要求：建议≥16轮，标准为27轮
     *   影响：轮数越多安全性越高，但性能越低
     *   权衡：轮数过少可能导致安全性不足，过多影响性能
     *
     * alpha（右旋转位数）：
     *   要求：建议3-16位，标准为8位
     *   影响：控制Feistel网络的扩散速度
     *   权衡：位数过大可能降低扩散效果
     *
     * beta（左旋转位数）：
     *   要求：建议1-8位，标准为3位
     *   影响：与alpha配合控制算法的混淆特性
     *   权衡：通常比alpha小，避免过度旋转
     *
     * 参数组合建议：
     *   标准配置：rounds=27, alpha=8, beta=3
     *   高性能配置：rounds=16, alpha=4, beta=2
     *   高安全性配置：rounds=32, alpha=8, beta=3
     * </pre>
     *
     * @param rounds 加密轮数，建议≥16轮，标准为27轮
     * @param alpha  右旋转位数，建议3-16位，标准为8位
     * @param beta   左旋转位数，建议1-8位，标准为3位
     * @param key    128位加密密钥，必须包含4个32位整数
     * @throws IllegalArgumentException 当密钥数组长度不为4时抛出
     */
    public Speck64Encryptor(int rounds, int alpha, int beta, int @NonNull [] key) {
        checkKeyLength(key);
        this.alpha = alpha;
        this.beta = beta;
        this.keys = generateKeys(key, rounds);
    }

    /**
     * 检查密钥长度是否为4个32位整数
     *
     * @param key 密钥数组
     * @throws IllegalArgumentException 当密钥长度不为4时抛出
     */
    private static void checkKeyLength(int[] key) {
        if (key.length != 4) {
            throw new IllegalArgumentException("SPECK64/128 requires exactly 4 integers (128 bits) as key");
        }
    }

    /**
     * 预生成所有轮密钥
     * 在构造函数中调用一次，避免每次加密/解密时重复计算
     *
     * @return 轮密钥数组
     */
    public int[] generateKeys(int[] key, int rounds) {
        int[] schedule = new int[key.length - 1];
        int[] result = new int[rounds];
        System.arraycopy(key, 0, schedule, 0, key.length - 1);
        result[0] = key[key.length - 1];

        for (int i = 0; i < rounds - 1; i++) {
            schedule[i % schedule.length] = (ror(schedule[i % schedule.length], alpha) + result[i]) ^ i;
            result[i + 1] = rol(result[i], beta) ^ schedule[i % schedule.length];
        }
        return result;
    }

    /**
     * 加密64位数据
     * <p>
     * 使用SPECK64/128算法对64位长整型数据进行加密。
     * 算法采用27轮Feistel网络结构，每轮使用不同的子密钥。
     * <p>
     * 加密流程：
     * <pre>
     * 1. 将64位明文分为高位32位和低位32位
     * 2. 执行27轮Feistel变换
     * 3. 每轮应用轮函数和密钥异或
     * 4. 返回64位密文
     * </pre>
     *
     * @param plaintext 64位明文数据
     * @return 64位加密后的数据
     */
    @Override
    public long encrypt(long plaintext) {
        // 将 64 位 long 拆分为两个 32 位 int (high, low)
        int high = (int) (plaintext >> 32);
        int low = (int) (plaintext & 0xFFFFFFFFL);
        // 加密循环 - 执行指定轮数的Feistel变换
        for (int key : keys) {
            // 轮函数 - SPECK的核心变换
            // 1. 高位右旋转并与低位相加，再与子密钥异或
            high = (ror(high, alpha) + low) ^ key;
            // 2. 低位左旋转并与新的高位异或
            low = rol(low, beta) ^ high;
        }

        return ((long) high << 32) | (low & 0xFFFFFFFFL);
    }

    /**
     * 解密64位数据
     * <p>
     * 使用SPECK64/128算法对64位密文数据进行解密。
     * 解密过程是加密过程的逆向操作，按照相反顺序应用轮函数。
     * <p>
     * 解密流程：
     * <pre>
     * 1. 使用预计算的轮密钥（构造函数中生成）
     * 2. 按照相反顺序执行轮逆向变换
     * 3. 每轮应用逆向轮函数
     * 4. 返回64位明文
     * </pre>
     *
     * @param ciphertext 64位密文数据
     * @return 64位解密后的明文数据
     */
    @Override
    public long decrypt(long ciphertext) {
        int high = (int) (ciphertext >> 32);
        int low = (int) (ciphertext & 0xFFFFFFFFL);

        for (int i = keys.length - 1; i >= 0; i--) {
            low = ror(low ^ high, beta);
            high = rol((high ^ keys[i]) - low, alpha);
        }

        return ((long) high << 32) | (low & 0xFFFFFFFFL);
    }


    /**
     * 循环左移操作
     *
     * @param value 待移位的32位整数值
     * @param bits  左移位数
     * @return 循环左移后的结果
     */
    private static int rol(int value, int bits) {
        return (value << bits) | (value >>> (32 - bits));
    }

    /**
     * 循环右移操作
     *
     * @param value 待移位的32位整数值
     * @param bits  右移位数
     * @return 循环右移后的结果
     */
    private static int ror(int value, int bits) {
        return (value >>> bits) | (value << (32 - bits));
    }

    /**
     * 将字节数组转换为整数数组
     * <p>
     * SPECK算法使用小端字节序（Little Endian）进行字节到整数的转换
     *
     * @param key 16字节密钥字节数组
     * @return 包含4个32位整数的数组
     * @throws IllegalArgumentException 当密钥长度不为16字节时抛出
     */
    public static int[] bytesToInts(byte[] key) {
        if (key.length != 16) {
            throw new IllegalArgumentException("SPECK64/128 requires exactly 16 bytes (128 bits) as key");
        }

        int[] intKey = new int[4];
        var buffer = ByteBuffer.wrap(key).order(ByteOrder.LITTLE_ENDIAN);
        for (int i = 0; i < 4; i++) {
            intKey[i] = buffer.getInt();
        }
        return intKey;
    }

}
