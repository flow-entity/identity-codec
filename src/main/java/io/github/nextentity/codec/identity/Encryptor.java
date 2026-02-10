package io.github.nextentity.codec.identity;

public interface Encryptor {

    long encrypt(long plaintextData);

    long decrypt(long encryptedData);

}