package com.tech.ian.user.utils;

import com.tech.ian.user.model.UserEntity;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.log4j.Log4j2;
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
@Log4j2
public class JwtUtils {

    private final Path path = Paths.get("user/privatekey.pem");
    private final String key = Files.readString(path)
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace(System.lineSeparator(), "")
            .replace("-----END PRIVATE KEY-----", "");

    public JwtUtils() throws IOException {
    }

    public String generateToken(UserEntity user) {
        return Jwts
                .builder()
                .setSubject(user.getEmail())
                .setIssuedAt(Date.from(Instant.now()))
                .setExpiration(Date.from(Instant.now().plusSeconds(60*60)))
                .claim("uuid", user.getUuid())
                .signWith(getKey(), SignatureAlgorithm.RS256)
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
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getKey() {
        byte[] decode = Base64.getDecoder().decode(key);
        PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(decode);
        try {
            KeyFactory k = KeyFactory.getInstance("RSA");
            return k.generatePrivate(pkcs8EncodedKeySpec);
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}