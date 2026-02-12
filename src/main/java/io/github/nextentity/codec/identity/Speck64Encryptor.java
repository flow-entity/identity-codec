package io.github.nextentity.codec.identity;

import org.jspecify.annotations.NonNull;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * SPECK64分组密码加密器实现
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
 * @see <a href="https://en.wikipedia.org/wiki/Speck_(cipher)">SPECK Cipher Wikipedia</a>
 */
public class Speck64Encryptor implements Encryptor {

    /**
     * 每轮的加密密钥数组
     */
    private final int[] keys;

    /**
     * 右旋转位数，标准SPECK64使用8位右旋转
     */
    private final int alpha;

    /**
     * 左旋转位数，标准SPECK64使用3位左旋转
     */
    private final int beta;

    /**
     * 字节数组构造函数
     *
     * @param key 密钥，最小长度8，建议长度16
     * @throws IllegalArgumentException 当密钥长度小于8或者不是4的倍数时抛出
     */
    public Speck64Encryptor(byte @NonNull [] key) {
        this(27, 8, 3, bytesToInts(key));
    }

    /**
     * int 数组构造函数
     *
     * @param key 密钥，小长度2，建议长度4
     * @throws IllegalArgumentException 当密钥数组长度小于2时抛出
     */
    public Speck64Encryptor(int @NonNull [] key) {
        this(27, 8, 3, key);
    }

    /**
     * 自定义参数构造函数
     * 允许自定义SPECK算法的各种参数
     *
     * @param rounds 加密轮数，建议≥16轮，标准为27轮
     * @param alpha  右旋转位数，建议3-16位，标准为8位
     * @param beta   左旋转位数，建议1-8位，标准为3位
     * @param key    密钥，小长度2，建议长度4
     * @throws IllegalArgumentException 当密钥数组长度小于2时抛出
     */
    public Speck64Encryptor(int rounds, int alpha, int beta, int @NonNull [] key) {
        if (key.length < 2) {
            throw new IllegalArgumentException("Key must contain at least 2 integers");
        }
        this.alpha = alpha;
        this.beta = beta;
        this.keys = generateKeys(key, rounds);
    }

    /**
     * 预生成所有轮密钥
     *
     * @return 轮密钥数组
     */
    private int[] generateKeys(int @NonNull [] key, int rounds) {
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
     * 使用SPECK64/128算法加密。
     * <p>
     * 加密流程：
     * <pre>
     * 1. 将64位明文分为高位32位和低位32位
     * 2. 执行27轮Feistel变换
     * 3. 每轮应用轮函数和密钥异或
     * </pre>
     *
     * @param plaintext 明文
     * @return 密文
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
     * 使用SPECK64/128算法进行解密。
     * <p>
     * 解密流程：
     * <pre>
     * 1. 使用预计算的轮密钥（构造函数中生成）
     * 2. 按照相反顺序执行轮逆向变换
     * 3. 每轮应用逆向轮函数
     * </pre>
     *
     * @param ciphertext 密文
     * @return 明文
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

    private static int rol(int value, int bits) {
        return (value << bits) | (value >>> (32 - bits));
    }

    private static int ror(int value, int bits) {
        return (value >>> bits) | (value << (32 - bits));
    }

    private static int[] bytesToInts(byte @NonNull [] key) {
        if (key.length < 8) {
            throw new IllegalArgumentException("Key length must be at least 8 bytes");
        }
        if (key.length % 4 != 0) {
            throw new IllegalArgumentException("Key length must be a multiple of 4");
        }
        int[] intKey = new int[key.length / 4];
        var buffer = ByteBuffer.wrap(key).order(ByteOrder.LITTLE_ENDIAN);
        for (int i = 0; i < intKey.length; i++) {
            intKey[i] = buffer.getInt();
        }
        return intKey;
    }

    static void main() {
        Speck64Encryptor encryptor = new Speck64Encryptor(new byte[8]);
        System.out.println(encryptor.encrypt(0L));
    }

}
