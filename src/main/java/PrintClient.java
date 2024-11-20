import model.OperationResult;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

public class PrintClient {
    private static IPrintServer server;

    private static String currentUser;
    private static String token;

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
            OperationResult result = server.login(username,password);
            if (result.isSuccess()) {
                token = result.getPayload();
                currentUser = username;
                System.out.println("Login successful");
                return true;
            } else {
                System.out.println(result.getPayload());
                return false;
            }
        } catch (RemoteException e) {
            System.err.println("Error in remote connection: " + e.toString());
        }
        System.out.println("Error occurred during login");
        return false;
    }


    public static void main(String[] args) {
        PrintClient client = new PrintClient("localhost");
        Scanner scanner = new Scanner(System.in);

        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();

        if (client.login(username, password)) {
            try {
                CLI(scanner);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    private static void CLI(Scanner scanner) throws RemoteException {
        System.out.println("Welcome to the Printer System CLI");
        printCmd();
        String cmdId = scanner.nextLine();


        while (!cmdId.equals("0")) {
            switch (cmdId) {
                case "1":{
                    System.out.println("Printer: ");
                    String printer = scanner.nextLine();
                    System.out.println("Filename: ");
                    String filename = scanner.nextLine();

                    System.out.println(printer + " " + filename);

                    System.out.println(server.print(token, currentUser, filename, printer) + "\n");
                    break;
                }
                case "2":{
                    System.out.println("Printer: ");
                    String printer = scanner.nextLine();
                    System.out.println(server.queue(token, currentUser, printer) + "\n");
                    break;
                }
                case "3":{
                    System.out.println("Printer: ");
                    String printer = scanner.nextLine();
                    System.out.println("JobId: ");
                    String jobId = scanner.nextLine();

                    System.out.println(server.topQueue(token, currentUser, printer, jobId) + "\n");
                    break;
                }
                case "4":{
                    System.out.println(server.start(token, currentUser) + "\n");
                    break;
                }
                case "5":{
                    System.out.println(server.stop(token, currentUser) + "\n");
                    break;
                }
                case "6":{
                    System.out.println(server.restart(token, currentUser) + "\n");
                    break;
                }
                case "7":{
                    System.out.println("Printer: ");
                    String printer = scanner.nextLine();

                    System.out.println(server.status(token, currentUser, printer) + "\n");
                    break;
                }
                case "8":{
                    System.out.println("Config parameter: ");
                    String param = scanner.nextLine();

                    System.out.println(server.readConfig(token, currentUser, param) + "\n");
                    break;
                }
                case "9":{
                    System.out.println("Config parameter: ");
                    String param = scanner.nextLine();
                    System.out.println("New value: ");
                    String val = scanner.nextLine();

                    System.out.println(server.setConfig(token, currentUser, param, val) + "\n");
                    break;
                }
            }

            printCmd();

            cmdId = scanner.nextLine();
        }
    }

    private static void printCmd() {
        System.out.println("""
                Actions
                
                1. Print
                2. List Queue
                3. Move Job to Top
                4. Start Server
                5. Stop Server
                6. Restart Server
                7. See Printer Status
                8. Read Configuration
                9. Edit Configuration
                0. Exit
                
                Choose a command:
                """);
    }
}
