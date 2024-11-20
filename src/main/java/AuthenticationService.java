import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import model.OperationResult;
import model.User;
import util.DbUtils;
import util.PropertiesUtils;

import javax.crypto.SecretKey;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Date;
import java.util.Properties;

public class AuthenticationService implements IAuthenticationService {
    private final SecretKey _jwtSecretKey;
    private final Properties _props = PropertiesUtils.GetProperties();
    private final DbUtils _db;

    public AuthenticationService() {
       _db = new DbUtils();
        assert _props != null;
        _jwtSecretKey = getSigningKey(_props.getProperty("JWT_SECRET"));
    }

    @Override
    public OperationResult login(String username, String password) {
        // Retrieve user from DB
        User user = _db.getUser(username);

        // Check for user existence and password match
        if (user != null) {
            String hashedPassword = hashPassword(password);

            // If the password matches with the one stored in the DB the user gets a token,
            if (user.getHashedPassword().equals(hashedPassword)) {
                return new OperationResult(jwtFromUsername(username), true);
            } else return new OperationResult("Invalid password", false);
        } else return new OperationResult("There is no account registered with this username", false);
    }

    @Override
    public OperationResult register(String username, String password, String role) {
        if (_db.userExists(username)) return new OperationResult("User already exists", false);

        // Storing the password in a hashed format instead of plain text
        String hashedPassword = hashPassword(password);

        // Execute SQL Insert of the new user
        if(_db.addUser(username, hashedPassword, role)) {

            // If SQL update is successful, generate JWT token for the user, to validate their requests
            return new OperationResult(jwtFromUsername(username), true);
        } else return new OperationResult("Failed to register user", false);
    }

    @Override
    public void logout() {

    }

    @Override
    public void validateToken(String token, String invokedBy) {
        // Extract username from JWT
        Claims claims = Jwts.parser()
                .verifyWith(_jwtSecretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        String username = claims.getSubject();

        // If a user tries to use another user's token the authentication fails
        if (!username.equals(invokedBy)) throw new SecurityException("Token cannot be used by other users");

        Date exp = claims.getExpiration();

        // If the token is expired or the user does not exist the authentication fails
        // TODO: also check for permissions for access control part
        if(!_db.userExists(username) || System.currentTimeMillis() >= exp.getTime())
            throw new SecurityException("Invalid token");
    }

    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to hash password", e);
        }
    }

    private String jwtFromUsername(String username) {
        return Jwts.builder()
                .subject(username)
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24)) // Token expires after one day
                .signWith(_jwtSecretKey)
                .compact();
    }

    private SecretKey getSigningKey(String keyAsString) {
        byte[] keyBytes = Decoders.BASE64.decode(keyAsString);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
