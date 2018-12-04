import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Connector implements Runnable{
    ServerSocket servSocket;
    ClientContainer clients;

    public Connector(int port, ClientContainer container){
        try {
            servSocket = new ServerSocket(port);
        } catch (IOException ex){
            System.err.println(ex.getMessage());
        }
        clients = container;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()){
            try{
                Socket socket = servSocket.accept();
                clients.addNewClient(socket);
            } catch (IOException ex){
                System.err.println(ex.getMessage());
            }
        }
    }
}
