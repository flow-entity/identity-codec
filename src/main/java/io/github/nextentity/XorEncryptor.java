package io.github.nextentity;

/**
 * XOR加密器
 * 使用固定密钥流对8字节数据进行 XOR 加密和解密
 * <pre>
 * 该类实现了简单的 XOR 流加密算法，适用于固定长度的数据加密
 * </pre>
 * 
 * @version 1.0
 */
public class XorEncryptor {
    private final byte[] keystream;

    /**
     * 构造函数
     *
     * @param keystream 密钥流不能为空且长度必须为8
     * @throws IllegalArgumentException 当密钥流为空或长度不为8时抛出
     */
    public XorEncryptor(byte[] keystream) {
        if (keystream == null || keystream.length != 8) {
            throw new IllegalArgumentException("Keystream must be exactly 8 bytes");
        }
        this.keystream = keystream.clone();
    }

    /**
     * 加密数据
     * 对输入的8字节数据进行 XOR 加密
     *
     * @param data 待加密的8字节数据
     * @return 加密后的8字节数据
     * @throws IllegalArgumentException 当输入数据长度不为8字节时抛出
     */
    public byte[] encrypt(byte[] data) {
        if (data.length != 8) throw new IllegalArgumentException("Data must be exactly 8 bytes");
        byte[] result = new byte[8];
        for (int i = 0; i < 8; i++) {
            result[i] = (byte) (data[i] ^ keystream[i]);
        }
        return result;
    }

    /**
     * 解密数据
     * 对输入的8字节密文进行 XOR 解密
     *
     * @param ciphertext 待解密的8字节密文
     * @return 解密后的8字节明文数据
     * @throws IllegalArgumentException 当输入密文长度不为8字节时抛出
     */
    public byte[] decrypt(byte[] ciphertext) {
        if (ciphertext.length != 8) throw new IllegalArgumentException("Ciphertext must be exactly 8 bytes");
        byte[] result = new byte[8];
        for (int i = 0; i < 8; i++) {
            result[i] = (byte) (ciphertext[i] ^ keystream[i]);
        }
        return result;
    }

}