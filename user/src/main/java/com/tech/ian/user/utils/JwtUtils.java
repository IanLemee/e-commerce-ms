package com.tech.ian.user.utils;

import com.tech.ian.user.model.user.UserEntity;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;

@Service
public class JwtUtils {
    private final Key privateKey;

    public JwtUtils() {
        try {
            ClassPathResource resource = new ClassPathResource("keys/privatekey.pem");

            String keyString = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

            String privateKeyPEM = keyString
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s+", "");

            byte[] encoded = Base64.getDecoder().decode(privateKeyPEM);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
            this.privateKey = keyFactory.generatePrivate(keySpec);

        } catch (Exception e) {
            throw new RuntimeException("Erro fatal: Não foi possível carregar a chave privada (privatekey.pem)", e);
        }
    }

    public String generateToken(UserEntity user) {
        return Jwts
                .builder()
                .setSubject(user.getEmail())
                .setIssuedAt(Date.from(Instant.now()))
                .setExpiration(Date.from(Instant.now().plusSeconds(60*60)))
                .claim("uuid", user.getUuid())
                .signWith(privateKey, SignatureAlgorithm.RS256)
                .compact();
    }

    public boolean verifyToken(String token, String email){
        String username = getUsername(token);
        return (email.equals(username) && !isExpired(token));
    }

    private boolean isExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    public String getUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(privateKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}