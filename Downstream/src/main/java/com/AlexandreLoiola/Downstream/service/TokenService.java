package com.AlexandreLoiola.Downstream.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.impl.DefaultClaims;
import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;


@Service
@Log4j2
public class TokenService {

    private SecretKey aesKey;

    private PublicKey publicKey;

    @Value("${jwt.aes.secret}")
    private String aesSecretKey;


    @PostConstruct
    public void init() {
        try {
            this.publicKey = loadPublicKey("keys/public.pem");
            this.aesKey = new SecretKeySpec(aesSecretKey.getBytes(StandardCharsets.UTF_8), "AES");
        } catch (Exception e) {
            throw new RuntimeException("Erro ao carregar chaves RSA/AES", e);
        }
    }
    public Claims validateToken(final String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(publicKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String encryptedData = claims.get("data", String.class);
            String[] userData = extractUserData(encryptedData);

            Map<String, Object> claimsMap = new HashMap<>();
            claimsMap.put("username", userData[0]);
            claimsMap.put("role", userData[1]);

            return new DefaultClaims(claimsMap);
        } catch (JwtException e) {
            throw new JwtException("Token inválido ou expirado");
        }
    }

    private String decryptAES(final String encryptedData) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, aesKey, new IvParameterSpec(new byte[16]));
        return new String(cipher.doFinal(Base64.getDecoder().decode(encryptedData)), StandardCharsets.UTF_8);
    }

    private String[] extractUserData(final String encryptedData) {
        try {
            String decryptedData = decryptAES(encryptedData);
            String[] parts = decryptedData.split(":");
            if (parts.length != 2) {
                throw new JwtException("Formato de dados inválido no token");
            }
            return parts;
        } catch (Exception e) {
            throw new JwtException("Erro ao descriptografar token", e);
        }
    }


    private static PublicKey loadPublicKey(final String path) throws Exception {
        InputStream is = new ClassPathResource(path).getInputStream();
        byte[] keyBytes = is.readAllBytes();
        String keyPEM = new String(keyBytes)
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");

        byte[] decodedKey = Base64.getDecoder().decode(keyPEM);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decodedKey);
        return keyFactory.generatePublic(keySpec);
    }
}