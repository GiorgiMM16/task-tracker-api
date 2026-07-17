package com.example.tasktracker.security;

import com.example.tasktracker.entity.User;
import com.example.tasktracker.exception.InvalidJwtException;
import com.example.tasktracker.model.Role;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

@Service
public class JwtService {

	private static final String HMAC_ALGORITHM = "HmacSHA256";

	private final JsonMapper objectMapper = new JsonMapper();

	@Value("${app.jwt.secret}")
	private String secret;

	@Value("${app.jwt.expiration-ms}")
	private long expirationMs;

	public String generateToken(User user) {
		Instant now = Instant.now();
		Map<String, Object> header = new LinkedHashMap<>();
		header.put("alg", "HS256");
		header.put("typ", "JWT");

		Map<String, Object> payload = new LinkedHashMap<>();
		payload.put("sub", user.getEmail());
		payload.put("userId", user.getId());
		payload.put("role", user.getRole().name());
		payload.put("iat", now.getEpochSecond());
		payload.put("exp", now.plusMillis(expirationMs).getEpochSecond());

		String unsignedToken = encodeJson(header) + "." + encodeJson(payload);
		return unsignedToken + "." + sign(unsignedToken);
	}

	public JwtClaims parseToken(String token) {
		String[] parts = token.split("\\.");
		if (parts.length != 3) {
			throw new InvalidJwtException("Invalid JWT format");
		}

		String unsignedToken = parts[0] + "." + parts[1];
		if (!MessageDigest.isEqual(sign(unsignedToken).getBytes(StandardCharsets.UTF_8), parts[2].getBytes(StandardCharsets.UTF_8))) {
			throw new InvalidJwtException("Invalid JWT signature");
		}

		Map<String, Object> payload = decodeJson(parts[1]);
		long expiresAt = readLong(payload.get("exp"), "exp");
		if (Instant.now().getEpochSecond() >= expiresAt) {
			throw new InvalidJwtException("JWT has expired");
		}

		String email = readString(payload.get("sub"), "sub");
		Long userId = readLong(payload.get("userId"), "userId");
		Role role = Role.valueOf(readString(payload.get("role"), "role"));
		return new JwtClaims(userId, email, role);
	}

	private String encodeJson(Map<String, Object> values) {
		try {
			return base64Url(objectMapper.writeValueAsBytes(values));
		} catch (JacksonException exception) {
			throw new IllegalStateException("Unable to encode JWT", exception);
		}
	}

	private Map<String, Object> decodeJson(String value) {
		try {
			byte[] json = Base64.getUrlDecoder().decode(value);
			return objectMapper.readValue(json, new TypeReference<>() {
			});
		} catch (IllegalArgumentException | JacksonException exception) {
			throw new InvalidJwtException("Unable to decode JWT", exception);
		}
	}

	private String sign(String unsignedToken) {
		try {
			Mac mac = Mac.getInstance(HMAC_ALGORITHM);
			SecretKeySpec key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM);
			mac.init(key);
			return base64Url(mac.doFinal(unsignedToken.getBytes(StandardCharsets.UTF_8)));
		} catch (Exception exception) {
			throw new IllegalStateException("Unable to sign JWT", exception);
		}
	}

	private String base64Url(byte[] bytes) {
		return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
	}

	private String readString(Object value, String claim) {
		if (value instanceof String stringValue && !stringValue.isBlank()) {
			return stringValue;
		}
		throw new InvalidJwtException("Missing JWT claim: " + claim);
	}

	private Long readLong(Object value, String claim) {
		if (value instanceof Number numberValue) {
			return numberValue.longValue();
		}
		throw new InvalidJwtException("Missing JWT claim: " + claim);
	}
}
