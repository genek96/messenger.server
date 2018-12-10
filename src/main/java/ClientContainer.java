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
    private ArrayList<Thread> performersThreads = new ArrayList<>();
    private ArrayList<FunctionsPerformer> performers = new ArrayList<>();

    void addNewClient(@NotNull Socket client, String name){ //TODO add synchronization
        try {
            Receiver receiver = new Receiver(client.getInputStream(), this, clients.size());
            FunctionsPerformer performer = new FunctionsPerformer("", this, clients.size());
            Thread receiverThread = new Thread(receiver);
            Thread performerThread = new Thread(performer);

            performersThreads.add(performerThread);
            performers.add(performer);
            clients.add(client);
            outputs.add(new Sender(client.getOutputStream()));
            inputs.add(receiverThread);
            names.add(name);

            performerThread.start();
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

    @Override
    public void doCommand(String text, int clientId){
        performers.get(clientId).addTask(text);
    }

    @Override
    public void getAnswer(String result, int clientId) {
        if (result == null){
            outputs.get(clientId).sendMessage("Response to your task: command not found");
        } else {
            outputs.get(clientId).sendMessage("Response to your task: " + result);
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
                //checking: is it simple message or command?
                if (message.startsWith("/")){
                    router.doCommand(message, id);
                } else {
                    router.sendMessages(message, id);
                }

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