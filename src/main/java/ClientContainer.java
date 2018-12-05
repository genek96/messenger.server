import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

class ClientContainer implements MessageReciver{

    private final static Logger log = Logger.getLogger(ClientContainer.class);
    
    private ArrayList<Socket> clients = new ArrayList<>();
    private ArrayList<Thread> inputs = new ArrayList<>();
    private ArrayList<Sender> outputs = new ArrayList<>();

    void addNewClient(@NotNull Socket client){
        try {
            clients.add(client);
            outputs.add(new Sender(client.getOutputStream()));
            Receiver receiver = new Receiver(client.getInputStream(), this, clients.size()-1);
            Thread receiverThread = new Thread(receiver);
            inputs.add(receiverThread);
            receiverThread.start();
        } catch (IOException ex){
            log.error(ex.getMessage()+" : "+ Arrays.toString(ex.getStackTrace()));
            System.err.println(ex.getMessage());
        }
    }

    @Override
    public void getMessage(String message, int clientId) {
        for (int i = 0; i < outputs.size(); i++) {
            if (i == clientId || !outputs.get(i).isActive()){
                continue;
            }
            outputs.get(i).sendMessage(message);
        }
    }

    public void stopAll(){
        for (int i = 0; i < clients.size(); i ++){
            inputs.get(i).interrupt();
            try {
                clients.get(i).close();
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        }
    }
}

class Receiver implements Runnable{
    
    private static final Logger log = Logger.getLogger(Receiver.class);
    
    private DataInputStream input;
    private MessageReciver router;
    private int id;

    Receiver(InputStream stream, MessageReciver router, int identificator){
        input = new DataInputStream(stream);
        this.router = router;
        id = identificator;
    }


    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()){
            try {
                String message = input.readUTF();
                router.getMessage(message, id);
            } catch (IOException ex){
                log.error(ex.getMessage()+" : "+ Arrays.toString(ex.getStackTrace()));
                System.err.println(ex.getMessage());
                return;
            }
        }

    }
}

class Sender  {
    
    private static final Logger log = Logger.getLogger(Sender.class);
    
    private DataOutputStream output;
    private boolean active = true;

    Sender (OutputStream stream){
        output = new DataOutputStream(stream);
    }

    void sendMessage (String message){
        try {
            output.writeUTF(message);
        } catch (IOException e) {
            log.error(e.getMessage()+" : "+ Arrays.toString(e.getStackTrace()));
            System.err.println(e.getMessage());
            active = false;
        }
    }

    boolean isActive(){
        return active;
    }
}