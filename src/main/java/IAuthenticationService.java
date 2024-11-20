public interface IAuthenticationService {

    String login(String username, String password);
    String register(String username, String password, String role);
    void logout();
    void validateToken(String token, String invokedBy);
}
