import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable {
    protected int          serverPort    = 8080;
    protected String       outputMessage = "";
    protected ServerSocket serverSocket  = null;
    protected boolean      isStopped     = false;
    protected Thread       runningThread = null;
    protected ExecutorService threadPool = null;

    public Server(int port, int numOfClients, String outputMessage){
        this.serverPort = port;
        this.threadPool = Executors.newFixedThreadPool(numOfClients);
        this.outputMessage = outputMessage + "\n\n";
    }

    public void run(){
        synchronized(this){
            this.runningThread = Thread.currentThread();
        }
        openServerSocket();
        while(! isStopped()){
            Socket clientSocket;
            try {
                clientSocket = this.serverSocket.accept();
            } catch (IOException e) {
                if(isStopped()) {
                    System.out.println("Server stopping") ;
                    break;
                }
                throw new RuntimeException(
                        "Error accepting client connection", e);
            }
            this.threadPool.execute(
                    new Connection(clientSocket,outputMessage));
        }
        this.threadPool.shutdown();
        System.out.println("Server is kill");
        System.exit(0);
    }


    private synchronized boolean isStopped() {
        return this.isStopped;
    }

    public synchronized void stop(){
        this.isStopped = true;
        try {
            this.serverSocket.close();
        } catch (IOException e) {
            throw new RuntimeException("Error closing server", e);
        }
    }

    private void openServerSocket() {
        try {
            this.serverSocket = new ServerSocket(this.serverPort);
        } catch (IOException e) {
            throw new RuntimeException("Cannot open port: " + this.serverPort, e);
        }
    }
}
