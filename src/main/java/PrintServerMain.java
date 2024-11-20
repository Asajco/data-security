import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class PrintServerMain {
    public static void main(String[] args) {
        try {
            IAuthenticationService authService = new AuthenticationService();
            PrintServer server = new PrintServer(authService);
            Registry registry = LocateRegistry.createRegistry(1099);
            registry.bind("PrintServer", server);
            System.out.println("Print Server is running...");
        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }
}
