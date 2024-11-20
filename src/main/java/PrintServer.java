import model.Job;
import model.OperationResult;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class PrintServer extends UnicastRemoteObject implements IPrintServer {
    private boolean isRunning;
    private final Map<String, ArrayDeque<Job>> _printers;
    private final Map<String, String> _configParams;

    private final IAuthenticationService _authenticationService;

    public PrintServer(IAuthenticationService authenticationService) throws RemoteException {
        super();
        _printers = new HashMap<>();
        _configParams = new HashMap<>();
        _authenticationService = authenticationService;

        this.isRunning = true;

        registerUser("admin", "admin", "ADMIN");
        registerUser("user", "12345", "USER");

        initPrinters();

        System.out.println(_printers);
    }

    @Override
    public OperationResult registerUser(String username, String password, String role) {
        return _authenticationService.register(username, password, role);
    }

    @Override
    public OperationResult login(String username, String password) {
        return _authenticationService.login(username, password);
    }

    @Override
    public void logout(String username) {
        _authenticationService.logout();
    }

    @Override
    public String print(String jwt, String username, String filename, String printer)
            throws RemoteException, SecurityException {

        try{
            _authenticationService.validateToken(jwt, username);
        } catch (SecurityException e) {
            return "Authentication Failed";
        }

        System.out.println(printer);

        if (_printers.containsKey(printer)) {
            int id = Math.abs(new Random(System.currentTimeMillis()).nextInt());
            _printers.get(printer).addLast(new Job(String.valueOf(id), filename));
            return "Job added: " + filename + " on " + printer;
        } else return "Specified printer is not available";
    }

    @Override
    public String queue(String jwt, String username, String printer)
            throws RemoteException, SecurityException {

        try{
            _authenticationService.validateToken(jwt, username);
        } catch (SecurityException e) {
            return "Authentication Failed";
        }

        ArrayDeque<Job> queue = _printers.get(printer);
        return printQueue(queue);
    }

    @Override
    public String topQueue(String jwt, String username, String printer, String job)
            throws RemoteException, SecurityException {

        try{
            _authenticationService.validateToken(jwt, username);
        } catch (SecurityException e) {
            return "Authentication Failed";
        }

        if (_printers.containsKey(printer)) {
            ArrayDeque<Job> queue = _printers.get(printer);
            Job jobToMove = null;

            for (Job printJob : queue) {
                if (printJob.getJobId().equals(job)) {
                    jobToMove = printJob;
                    break;
                }
            }
            if (jobToMove != null) {
                queue.remove(jobToMove);
                queue.addFirst(new Job(jobToMove.getJobId(), jobToMove.getFileName()));
                return "Queue updated successfully";
            }
        }

        return  "Specified printer is not available";
    }

    @Override
    public String start(String jwt, String username) throws RemoteException, SecurityException {
        try{
            _authenticationService.validateToken(jwt, username);
        } catch (SecurityException e) {
            return "Authentication Failed";
        }

        isRunning = true;
        return "Print server started";
    }

    @Override
    public String stop(String jwt, String username) throws RemoteException, SecurityException {
        try{
            _authenticationService.validateToken(jwt, username);
        } catch (SecurityException e) {
            return "Authentication Failed";
        }

        isRunning = false;
        return "Print server stopped";
    }

    @Override
    public String restart(String jwt, String username) throws RemoteException, SecurityException {
        try{
            _authenticationService.validateToken(jwt, username);
        } catch (SecurityException e) {
            return "Authentication Failed";
        }

        isRunning = false;
        _printers.forEach((k, v) -> {
            v.clear();
        });
        isRunning = true;
        return "Print server restarted";
    }

    @Override
    public String status(String jwt, String username, String printer)
            throws RemoteException, SecurityException {
        try{
            _authenticationService.validateToken(jwt, username);
        } catch (SecurityException e) {
            return "Authentication Failed";
        }

        if (!_printers.containsKey(printer)) return "Specified printer is not available";

        return "Printer " + printer + " is " + (isRunning ? "running" : "stopped") + "\n" +
               "Jobs in queue: " + "\n" + printQueue(_printers.get(printer));
    }

    @Override
    public String readConfig(String jwt, String username, String parameter)
            throws RemoteException, SecurityException {
        try{
            _authenticationService.validateToken(jwt, username);
        } catch (SecurityException e) {
            return "Authentication Failed";
        }
        return _configParams.getOrDefault(parameter, "Parameter not found");
    }

    @Override
    public String setConfig(String jwt, String username, String parameter, String value)
            throws RemoteException, SecurityException {
        try{
            _authenticationService.validateToken(jwt, username);
        } catch (SecurityException e) {
            return "Authentication Failed";
        }

        _configParams.put(parameter, value);
        return "Config updated successfully";
    }

    private String printQueue(ArrayDeque<Job> queue) {
        StringBuilder sb = new StringBuilder();
        for (Job job : queue) {
            sb.append(job.getJobId()).append(" ").append(job.getFileName()).append("\n");
        }
        return sb.toString();
    }

    private void initPrinters() {
        ArrayDeque<Job> queue1 = new ArrayDeque<>();
        ArrayDeque<Job> queue2 = new ArrayDeque<>();

        queue1.push(new Job("1", "file1.txt"));
        queue1.push(new Job("2", "file2.txt"));
        queue1.push(new Job("3", "file3.txt"));

        queue2.push(new Job("11", "file11.txt"));
        queue2.push(new Job("22", "file22.txt"));
        queue2.push(new Job("33", "file33.txt"));

        _printers.put("printer1", queue1);
        _printers.put("printer2", queue2);
    }

    private void initConfigParams() {
        _configParams.put("param1", "value1");
    }
}
