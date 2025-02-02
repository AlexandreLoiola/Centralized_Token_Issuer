package com.AlexandreLoiola.Issuer.service;

import com.AlexandreLoiola.Issuer.rest.dto.UserInfoDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Key;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Log4j2
public class TokenService {
    private PrivateKey privateKey;
    private PublicKey publicKey;
    private SecretKey aesKey;
    private static final long ACCESS_EXPIRATION = 1000 * 60 * 15; // 15min
    private static final long REFRESH_EXPIRATION = 1000 * 60 * 60 * 24 * 7; // 7D

    private final Map<String, UserInfoDto> refreshTokens = new ConcurrentHashMap<>();

    @Value("${jwt.aes.secret}")
    private String aesSecretKey;

    @PostConstruct
    public void init() {
        try {
            this.privateKey = loadPrivateKey("keys/private.pem");
            this.publicKey = loadPublicKey("keys/public.pem");
            this.aesKey = new SecretKeySpec(aesSecretKey.getBytes(StandardCharsets.UTF_8), "AES");
        } catch (Exception e) {
            throw new RuntimeException("Erro ao carregar chaves RSA/AES", e);
        }
    }

    public String generateAccessToken(UserInfoDto userInfoDto) {
        log.info("Gerando token para usuário: {}", userInfoDto.getUsername());
        try {
            String encryptedPayload = encryptAES(userInfoDto.getUsername() + ":" + userInfoDto.getRole().name());
            return Jwts.builder()
                    .setSubject(userInfoDto.getUsername())
                    .claim("data", encryptedPayload)
                    .setExpiration(new Date(System.currentTimeMillis() + ACCESS_EXPIRATION))
                    .signWith(privateKey)
                    .compact();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar token", e);
        }
    }

    public String generateRefreshToken(UserInfoDto userInfoDto) {
        String refreshToken = UUID.randomUUID().toString();
        refreshTokens.put(refreshToken, userInfoDto);
        return refreshToken;
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .setSigningKey(publicKey)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            log.error("Token inválido: {}", e.getMessage());
            return false;
        }
    }

    public String getUsernameFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(publicKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            String decryptedData = decryptAES(claims.get("data", String.class));
            String username = decryptedData.split(":")[0];
            return username;
        } catch (Exception e) {
            throw new RuntimeException("Erro ao obter username do token", e);
        }
    }

    public String refreshAccessToken(String refreshToken) {
        UserInfoDto userInfoDto = refreshTokens.get(refreshToken);
        if (userInfoDto == null) {
            throw new RuntimeException("Refresh Token inválido");
        }
        return generateAccessToken(userInfoDto);
    }

    public void revokeRefreshToken(String refreshToken) {
        refreshTokens.remove(refreshToken);
    }

    public static Map<String, Object> decodeJwtPayload(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) {
                throw new IllegalArgumentException("Token inválido");
            }

            byte[] decodedBytes = Base64.getUrlDecoder().decode(parts[1]);
            String payloadJson = new String(decodedBytes, StandardCharsets.UTF_8);

            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(payloadJson, Map.class);

        } catch (IOException e) {
            throw new RuntimeException("Erro ao decodificar o JWT", e);
        }
    }

    private String encryptAES(String data) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, aesKey, new IvParameterSpec(new byte[16]));
        return Base64.getEncoder().encodeToString(cipher.doFinal(data.getBytes(StandardCharsets.UTF_8)));
    }

    private String decryptAES(String encryptedData) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, aesKey, new IvParameterSpec(new byte[16]));
        return new String(cipher.doFinal(Base64.getDecoder().decode(encryptedData)), StandardCharsets.UTF_8);
    }

    private static PrivateKey loadPrivateKey(String path) throws Exception {
        InputStream is = new ClassPathResource(path).getInputStream();
        byte[] keyBytes = is.readAllBytes();
        String keyPEM = new String(keyBytes)
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");

        byte[] decodedKey = Base64.getDecoder().decode(keyPEM);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decodedKey);
        return keyFactory.generatePrivate(keySpec);
    }

    private static PublicKey loadPublicKey(String path) throws Exception {
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