import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;


public class Connector implements Runnable{

    private static final Logger log = Logger.getLogger(Connector.class);
    private ServerSocket servSocket;
    private ClientContainer clients;

    Connector(int port, ClientContainer container){
        try {
            servSocket = new ServerSocket(port);
        } catch (IOException ex){
            log.error(ex.getMessage()+" : "+ Arrays.toString(ex.getStackTrace()));
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
                log.error(ex.getMessage()+" : "+ Arrays.toString(ex.getStackTrace()));
            }
        }
    }
}
