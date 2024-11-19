import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IPrintServer extends Remote {
    // Authentication methods
    String login(String username, String password) throws RemoteException;
    void logout(String sessionId) throws RemoteException;
    
    // Print server operations
    void print(String sessionId, String filename, String printer) throws RemoteException, SecurityException;
    String queue(String sessionId, String printer) throws RemoteException, SecurityException;
    void topQueue(String sessionId, String printer, int job) throws RemoteException, SecurityException;
    void start(String sessionId) throws RemoteException, SecurityException;
    void stop(String sessionId) throws RemoteException, SecurityException;
    void restart(String sessionId) throws RemoteException, SecurityException;
    String status(String sessionId, String printer) throws RemoteException, SecurityException;
    String readConfig(String sessionId, String parameter) throws RemoteException, SecurityException;
    void setConfig(String sessionId, String parameter, String value) throws RemoteException, SecurityException;
}
