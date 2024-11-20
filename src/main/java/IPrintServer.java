import model.OperationResult;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;


public interface IPrintServer extends Remote {
    // Authentication methods
    OperationResult login(String username, String password) throws RemoteException;
    OperationResult registerUser(String username, String password, String role) throws RemoteException;
    void logout(String sessionId) throws RemoteException;
    
    // Print server operations
    String print(String jwt, String username, String filename, String printer) throws RemoteException, SecurityException;
    String queue(String jwt, String username, String printer) throws RemoteException, SecurityException;
    String topQueue(String jwt, String username, String printer, String job) throws RemoteException, SecurityException;
    String start(String jwt, String username) throws RemoteException, SecurityException;
    String stop(String jwt, String username) throws RemoteException, SecurityException;
    String restart(String jwt, String username) throws RemoteException, SecurityException;
    String status(String jwt, String username, String printer) throws RemoteException, SecurityException;
    String readConfig(String jwt, String username, String parameter) throws RemoteException, SecurityException;
    String setConfig(String jwt, String username, String parameter, String value) throws RemoteException, SecurityException;
}
