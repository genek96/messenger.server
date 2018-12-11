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

    private Object locker = new Object(); //locker for collections
    /**
     * This method add new client to container
     * and open input and output streams for it.
     * @param client
     *        socket of a new client
     * @param name
     *        this name will shown to all other members of the chat
     */
    void addNewClient(@NotNull Socket client, String name){
        try {
            Receiver receiver = new Receiver(client.getInputStream(), this, clients.size());
            FunctionsPerformer performer = new FunctionsPerformer("", this, clients.size());
            Thread receiverThread = new Thread(receiver);
            Thread performerThread = new Thread(performer);

            synchronized (locker){
                performersThreads.add(performerThread);
                performers.add(performer);
                clients.add(client);
                outputs.add(new Sender(client.getOutputStream()));
                inputs.add(receiverThread);
                names.add(name);
            }

            performerThread.start();
            receiverThread.start();
            sendMessages(name + " have joined to chat!", clients.size()-1);
        } catch (IOException ex){
            log.error(ex.getMessage()+" : "+ Arrays.toString(ex.getStackTrace()));
            System.err.println(ex.getMessage());
        }
    }

    /**
     * You have to call it when somebody send a message.
     * This method send this message for everybody, who joined the chat
     * @param message
     *        Text of the message
     * @param clientId
     *        id of client, who sent this message
     */
    @Override
    public void sendMessages(String message, int clientId) {
        synchronized (locker){
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

    /**
     * You have call it when somebody sent a command, which starts from '/'
     * This method starts performing of this command
     * @param text
     *        full text of command, including '/' and parameters, separated by ' '
     * @param clientId
     *         if od client, who sent the command
     */
    @Override
    public void doCommand(String text, int clientId){
        synchronized (locker){
            performers.get(clientId).addTask(text);
        }
    }

    /**
     * This method must called only by FunctionsPerformer.
     * It send response on the command, which client sent early
     * @param result
     *        result of performing the command
     * @param clientId
     *        client, who sent the command. He will receive the answer.
     */
    @Override
    public void getAnswer(String result, int clientId) {
        synchronized (locker){
            if (result == null){
                outputs.get(clientId).sendMessage("Response to your task: command not found");
            } else {
                outputs.get(clientId).sendMessage("Response to your task: " + result);
            }
        }
    }

}

class Receiver implements Runnable{
    
    private static final Logger log = Logger.getLogger(Receiver.class);
    
    private DataInputStream input;
    private MessageReciver router;
    private int id;

    /**
     *
     * @param stream
     *        Input stream for receiving messages
     * @param router
     *        Link to the ClientContainer, who will use this object.
     *        If you create this object from some ClientContainer, you must use "this"
     * @param identifier
     *          unique identifier of the client.
     *          If it called from ClientContainer it should be index of client in an array.
     */
    Receiver(InputStream stream, MessageReciver router, int identifier){
        input = new DataInputStream(stream);
        this.router = router;
        id = identifier;
    }

    /**
     * It is waiting for a new message and when it receive one,
     * it called methods doCommand() or sendMessage() of the router,
     * which must be defined in the constructor
     */
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

    /**
     *
     * @param stream
     *        to this Output Stream Sender will send messages
     */
    Sender (OutputStream stream){
        output = new DataOutputStream(stream);
    }

    /**
     *
     * @param message
     *        Send message to client
     */
    void sendMessage (String message){
        try {
            output.writeUTF(message);
        } catch (IOException | NullPointerException e) {
            log.error(e.getMessage()+" : "+ Arrays.toString(e.getStackTrace()));
            System.err.println(e.getMessage());
            active = false;
        }
    }

    /**
     *
     * @return
     *          {true} - if Sender is available to sending
     *          {false} - if it is not
     */
    boolean isActive(){
        return active;
    }
}