import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

public class PrintClient {
    private IPrintServer server;
    private String sessionId;

    public PrintClient(String host) {
        try {
            Registry registry = LocateRegistry.getRegistry(host);
            server = (IPrintServer) registry.lookup("PrintServer");
        } catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }
    }

    public boolean login(String username, String password) {
        try {
            sessionId = server.login(username, password);
            return true;
        } catch (Exception e) {
            System.err.println("Login failed: " + e.getMessage());
            return false;
        }
    }

    public void logout() {
        try {
            if (sessionId != null) {
                server.logout(sessionId);
                sessionId = null;
            }
        } catch (Exception e) {
            System.err.println("Logout failed: " + e.getMessage());
        }
    }

    // Example usage
    public static void main(String[] args) {
        PrintClient client = new PrintClient("localhost");
        Scanner scanner = new Scanner(System.in);

        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();

        if (client.login(username, password)) {
            System.out.println("Login successful!");
            client.logout();
        }
    }
}
