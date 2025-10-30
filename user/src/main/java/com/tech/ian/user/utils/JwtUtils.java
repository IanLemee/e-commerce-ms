package com.tech.ian.user.utils;

import com.tech.ian.user.model.UserEntity;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Key;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtUtils {

    private final Path path = Paths.get("user/privatekey.pem");
    private final String key = Files.readString(path)
            .replace("-----BEGIN RSA PRIVATE KEY-----", "")
            .replace(System.lineSeparator(), "")
            .replace("-----END RSA PRIVATE KEY-----", "");

    public JwtUtils() throws IOException {
    }

    public String generateToken(UserEntity user) {
        return Jwts
                .builder()
                .setSubject(user.getEmail())
                .setIssuedAt(Date.from(Instant.now()))
                .setExpiration(Date.from(Instant.now().plusSeconds(60*60)))
                .claim("uuid", user.getUuid())
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean verifyToken(String token, String email){
        String username = getUsername(token);
        return username.equals(email) || !isExpired(token);
    }

    private boolean isExpired(String token) {
        return extractAllClaims(token).getExpiration().before(Date.from(Instant.now()));
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
        byte[] bytes = key.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(bytes);
    }
}