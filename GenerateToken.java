import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

public class GenerateToken {
    public static void main(String[] args) {
        String secret = "monolith-dev-secret-key-please-change-in-production-2026";
        SecretKey secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        Instant now = Instant.now();
        String token = Jwts.builder()
                .subject("lisi")
                .claim("tenantId", "100000002")
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(86400 * 7)))
                .signWith(secretKey)
                .compact();
        System.out.println("Bearer " + token);
    }
}
