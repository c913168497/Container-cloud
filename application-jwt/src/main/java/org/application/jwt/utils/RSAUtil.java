package org.application.jwt.utils;

import org.apache.tomcat.util.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * RSA 工具
 * 用openssl生成512位RSA：
 * 生成私钥：
 * openssl genrsa -out key.pem 512
 * 从私钥中导出公钥：
 * openssl rsa -in key.pem -pubout -out public-key.pem
 * 公钥加密：
 * openssl rsautl -encrypt -in xx.file -inkey public-key.pem -pubin -out xx.en
 * 私钥解密：
 * openssl rsautl -decrypt -in xx.en -inkey key.pem -out xx.de
 * 要在Java内调用还要进行pkcs8编码：
 * openssl pkcs8 -topk8 -inform PEM -in key.pem -outform PEM -out private-key.pem -nocrypt
 * 最后将公私玥放在/resources/rsa/：private-key.pem public-key.pem
 *
 * @author Zoctan
 * @date 2018/05/27
 */
@Component
public class RSAUtil {
    private final static Logger log = LoggerFactory.getLogger(RSAUtil.class);

    private final String algorithm = "RSA";

    private static final String publicKeyHead = "-----BEGIN PUBLIC KEY-----";
    private static final String publicKeyTail = "-----END PUBLIC KEY-----";

    private static final String privateKeyHead = "-----BEGIN PRIVATE KEY-----";
    private static final String privateKeyTail = "-----END PRIVATE KEY-----";

    /**
     * 生成密钥对
     *
     * @param keyLength 密钥长度(最少512位)
     * @return 密钥对
     * 公钥 keyPair.getPublic()
     * 私钥 keyPair.getPrivate()
     * @throws Exception e
     */
    public KeyPair genKeyPair(final int keyLength) throws Exception {
        final KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(this.algorithm);
        keyPairGenerator.initialize(keyLength);
        return keyPairGenerator.generateKeyPair();
    }

    /**
     * 公钥加密
     *
     * @param content   待加密数据
     * @param publicKey 公钥
     * @return 加密内容
     * @throws Exception e
     */
    public byte[] encrypt(final byte[] content, final PublicKey publicKey) throws Exception {
        final Cipher cipher = Cipher.getInstance(this.algorithm);
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return cipher.doFinal(content);
    }

    /**
     * 私钥解密
     *
     * @param content    加密数据
     * @param privateKey 私钥
     * @return 解密内容
     * @throws Exception e
     */
    public byte[] decrypt(final byte[] content, final PrivateKey privateKey) throws Exception {
        final Cipher cipher = Cipher.getInstance(this.algorithm);
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        return cipher.doFinal(content);
    }

    private byte[] replaceAndBase64Decode(final String file, final String headReplace, final String tailReplace) throws Exception {
        final ResourceLoader loader = new DefaultResourceLoader();
        final File f = loader.getResource(file).getFile();
        final FileInputStream fis = new FileInputStream(f);
        final DataInputStream dis = new DataInputStream(fis);
        final byte[] keyBytes = new byte[(int) f.length()];
        dis.readFully(keyBytes);
        dis.close();

        final String keyPEM =
            new String(keyBytes)
                .replace(headReplace, "").trim()
                .replace(tailReplace, "").trim();

        return Base64.decodeBase64(keyPEM);
    }

    /**
     * 加载pem格式的公钥
     *
     * @param pem 公钥文件名
     * @return 公钥
     */
    public PublicKey loadPemPublicKey(final String pem) {
        try {
            final byte[] decoded = this.replaceAndBase64Decode(pem, publicKeyHead, publicKeyTail);
            final X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
            final KeyFactory keyFactory = KeyFactory.getInstance(this.algorithm);
            return keyFactory.generatePublic(spec);
        } catch (final Exception e) {
            log.error(e.getMessage());
            return null;
        }
    }

    /**
     * 加载pem格式PKCS8编码的私钥
     *
     * @param pem 私钥文件名
     * @return 私钥
     */
    public PrivateKey loadPemPrivateKey(final String pem) {
        try {
            final byte[] decoded = this.replaceAndBase64Decode(pem, privateKeyHead, privateKeyTail);
            final PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);
            final KeyFactory keyFactory = KeyFactory.getInstance(this.algorithm);
            return keyFactory.generatePrivate(spec);
        } catch (final Exception e) {
            log.error(e.getMessage());
            return null;
        }
    }
}
