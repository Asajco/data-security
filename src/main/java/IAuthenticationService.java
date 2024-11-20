import model.OperationResult;

public interface IAuthenticationService {

    OperationResult login(String username, String password);
    OperationResult register(String username, String password, String role);
    void logout();
    void validateToken(String token, String invokedBy);
}
