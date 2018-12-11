import org.apache.log4j.Logger;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;


public class Connector implements Runnable{

    private static final Logger log = Logger.getLogger(Connector.class);
    private ServerSocket servSocket;
    private ClientContainer clients;

    /**
     * Open server socket
     * @param port
     *        port of the server socket
     * @param container
     *        Client container, which will store clients and work with them
     */
    Connector(int port, ClientContainer container){
        try {
            servSocket = new ServerSocket(port);
        } catch (IOException | IllegalArgumentException ex){
            log.error(ex.getMessage()+" : "+ Arrays.toString(ex.getStackTrace()));
            System.err.println(ex.getMessage());
        }
        clients = container;
    }

    /**
     * This method is waiting of new connections and added them to client container
     */
    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()){
            try{
                Socket socket = servSocket.accept();
                //authorization
                socket.setSoTimeout(0);
                DataInputStream input = new DataInputStream(socket.getInputStream());
                String name = input.readUTF();
                clients.addNewClient(socket, name);
            } catch (IOException ex){
                log.error(ex.getMessage()+" : "+ Arrays.toString(ex.getStackTrace()));
            }
        }
    }
}
