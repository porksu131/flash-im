package com.szr.flashim.general.utils;

import java.io.FileWriter;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;

public class RsaKeyGenerator {

    public static void generateKeys(String publicKeyPath, String privateKeyPath) throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        // 保存公钥
        try (FileWriter pubWriter = new FileWriter(publicKeyPath)) {
            pubWriter.write(Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded()));
        }

        // 保存私钥
        try (FileWriter privWriter = new FileWriter(privateKeyPath)) {
            privWriter.write(Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded()));
        }
    }

    public static void main(String[] args) throws Exception {
        generateKeys("rsa-public.key", "rsa-private.key");
    }
}