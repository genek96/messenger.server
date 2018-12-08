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
    private ArrayList<String> names = new ArrayList<>();

    void addNewClient(@NotNull Socket client, String name){
        try {
            Receiver receiver = new Receiver(client.getInputStream(), this, clients.size());
            Thread receiverThread = new Thread(receiver);

            clients.add(client);
            outputs.add(new Sender(client.getOutputStream()));
            inputs.add(receiverThread);
            names.add(name);

            receiverThread.start();
            sendMessages(name + " have joined to chat!", clients.size()-1);
        } catch (IOException ex){
            log.error(ex.getMessage()+" : "+ Arrays.toString(ex.getStackTrace()));
            System.err.println(ex.getMessage());
        }
    }

    @Override
    public void sendMessages(String message, int clientId) {
        if (clientId < 0 || clientId >= names.size()){
            log.error("Index out of boundaries exception : "+ Arrays.toString((new Exception()).getStackTrace()));
            return;
        }
        String sender = names.get(clientId); //name of message sender
        for (int i = 0; i < outputs.size(); i++) {
            if (i == clientId || !outputs.get(i).isActive()){
                continue;
            }
            outputs.get(i).sendMessage(sender + ": " + message);
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
                router.sendMessages(message, id);
            } catch (IOException ex){
                log.error(ex.getMessage()+" : "+ Arrays.toString(ex.getStackTrace()));
                System.err.println(ex.getMessage());
                router.sendMessages("have leaved the chat", id);
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
        } catch (IOException | NullPointerException e) {
            log.error(e.getMessage()+" : "+ Arrays.toString(e.getStackTrace()));
            System.err.println(e.getMessage());
            active = false;
        }
    }

    boolean isActive(){
        return active;
    }
}