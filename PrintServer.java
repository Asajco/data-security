import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PrintServer extends UnicastRemoteObject implements IPrintServer {
    private static final long SESSION_DURATION = 30 * 60 * 1000; // 30 minutes
    private final Map<String, String> passwordStore; // username -> hashedPassword
    private final Map<String, SessionInfo> sessions; // sessionId -> SessionInfo
    private final SecureRandom secureRandom;
    private boolean isRunning;
    private final Map<String, List<PrintJob>> printerQueues; // printer -> jobs
    private final Map<String, String> configParams;

    private static class SessionInfo {
        String username;
        long expirationTime;

        SessionInfo(String username, long expirationTime) {
            this.username = username;
            this.expirationTime = expirationTime;
        }
    }

    private static class PrintJob {
        int jobId;
        String filename;

        PrintJob(int jobId, String filename) {
            this.jobId = jobId;
            this.filename = filename;
        }
    }

    public PrintServer() throws RemoteException {
        super();
        this.passwordStore = new HashMap<>();
        this.sessions = new ConcurrentHashMap<>();
        this.secureRandom = new SecureRandom();
        this.isRunning = true;
        this.printerQueues = new HashMap<>();
        this.configParams = new HashMap<>();
        
        initializeUsers();
        startSessionCleanup();
    }

    private void initializeUsers() {
        addUser("admin", "admin123");
        addUser("user1", "pass123");
    }

    private void addUser(String username, String password) {
        passwordStore.put(username, hashPassword(password));
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

    private void startSessionCleanup() {
        Thread cleanupThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(60000); // Check every minute
                    cleanupExpiredSessions();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        cleanupThread.setDaemon(true);
        cleanupThread.start();
    }

    private void cleanupExpiredSessions() {
        long now = System.currentTimeMillis();
        sessions.entrySet().removeIf(entry -> entry.getValue().expirationTime < now);
    }

    private String generateSessionId() {
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        return Base64.getEncoder().encodeToString(randomBytes);
    }

    private void validateSession(String sessionId) throws SecurityException {
        SessionInfo session = sessions.get(sessionId);
        if (session == null || session.expirationTime < System.currentTimeMillis()) {
            sessions.remove(sessionId);
            throw new SecurityException("Invalid or expired session");
        }
    }

    @Override
    public String login(String username, String password) throws RemoteException {
        String storedHash = passwordStore.get(username);
        if (storedHash != null && storedHash.equals(hashPassword(password))) {
            String sessionId = generateSessionId();
            sessions.put(sessionId, new SessionInfo(username, 
                System.currentTimeMillis() + SESSION_DURATION));
            return sessionId;
        }
        throw new SecurityException("Invalid credentials");
    }

    @Override
    public void logout(String sessionId) throws RemoteException {
        sessions.remove(sessionId);
    }

    @Override
    public void print(String sessionId, String filename, String printer) 
            throws RemoteException, SecurityException {
        validateSession(sessionId);
        printerQueues.computeIfAbsent(printer, k -> new ArrayList<>())
            .add(new PrintJob(new Random().nextInt(1000), filename));
        System.out.println("Printing " + filename + " on " + printer);
    }

    @Override
    public String queue(String sessionId, String printer) 
            throws RemoteException, SecurityException {
        validateSession(sessionId);
        List<PrintJob> queue = printerQueues.getOrDefault(printer, new ArrayList<>());
        StringBuilder sb = new StringBuilder();
        for (PrintJob job : queue) {
            sb.append(job.jobId).append(" ").append(job.filename).append("\n");
        }
        return sb.toString();
    }

    @Override
    public void topQueue(String sessionId, String printer, int job) 
            throws RemoteException, SecurityException {
        validateSession(sessionId);
        List<PrintJob> queue = printerQueues.get(printer);
        if (queue != null) {
            PrintJob jobToMove = null;
            for (PrintJob printJob : queue) {
                if (printJob.jobId == job) {
                    jobToMove = printJob;
                    break;
                }
            }
            if (jobToMove != null) {
                queue.remove(jobToMove);
                queue.add(0, jobToMove);
            }
        }
    }

    @Override
    public void start(String sessionId) throws RemoteException, SecurityException {
        validateSession(sessionId);
        isRunning = true;
        System.out.println("Print server started");
    }

    @Override
    public void stop(String sessionId) throws RemoteException, SecurityException {
        validateSession(sessionId);
        isRunning = false;
        System.out.println("Print server stopped");
    }

    @Override
    public void restart(String sessionId) throws RemoteException, SecurityException {
        validateSession(sessionId);
        isRunning = false;
        printerQueues.clear();
        isRunning = true;
        System.out.println("Print server restarted");
    }

    @Override
    public String status(String sessionId, String printer) 
            throws RemoteException, SecurityException {
        validateSession(sessionId);
        return "Printer " + printer + " is " + (isRunning ? "running" : "stopped") + 
               " with " + printerQueues.getOrDefault(printer, new ArrayList<>()).size() + 
               " jobs in queue";
    }

    @Override
    public String readConfig(String sessionId, String parameter) 
            throws RemoteException, SecurityException {
        validateSession(sessionId);
        return configParams.getOrDefault(parameter, "Parameter not found");
    }

    @Override
    public void setConfig(String sessionId, String parameter, String value) 
            throws RemoteException, SecurityException {
        validateSession(sessionId);
        configParams.put(parameter, value);
    }
}
