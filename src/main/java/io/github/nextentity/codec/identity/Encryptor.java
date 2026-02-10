package io.github.nextentity.codec.identity;

public interface Encryptor {

    long encrypt(long plaintext);

    long decrypt(long encrypted);

}