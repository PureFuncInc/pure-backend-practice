package net.purefunc.practice.config.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Optional;

@Component
public class JwtTokenService {

    private final String key = "C874B58E53F97A2B15D0B5848RSJ5432GBSIJL09W5643BHG1E40924FCAF1C19BFA4484A1523704C0AB1F42";

    public String generate(String subject, Long ttlMillis, String uuid) {
        return Optional
                .of(new Date())
                .map(v -> Jwts
                        .builder()
                        .setIssuer("pure-backend-practice")
                        .setSubject(subject)
                        .setAudience(null)
                        .setExpiration(new Date(System.currentTimeMillis() + ttlMillis))
                        .setNotBefore(v)
                        .setIssuedAt(v)
                        .setId(uuid)
                        .signWith(Keys.hmacShaKeyFor(key.getBytes()), SignatureAlgorithm.HS512)
                        .compact()
                )
                .get();
    }

    public String retrieveSubject(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(key.getBytes()))
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
}
