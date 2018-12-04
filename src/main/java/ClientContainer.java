import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

class ClientContainer implements MessageReciver{

    private ArrayList<Socket> clients = new ArrayList<>();
    private ArrayList<Thread> inputs = new ArrayList<>();
    private ArrayList<Sender> outputs = new ArrayList<>();

    void addNewClient(@NotNull Socket client){
        try {
            clients.add(client);
            outputs.add(new Sender(client.getOutputStream()));
            Reciver reciver = new Reciver(client.getInputStream(), this, clients.size()-1);
            Thread receiverThread = new Thread(reciver);
            inputs.add(receiverThread);
            receiverThread.start();
        } catch (IOException ex){
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

class Reciver implements Runnable{

    private DataInputStream input;
    private MessageReciver router;
    private int id;

    public Reciver (InputStream stream, MessageReciver router, int identificator){
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
                System.err.println(ex.getMessage());
                return;
            }
        }

    }
}

class Sender  {

    private DataOutputStream output;
    private boolean active = true;

    Sender (OutputStream stream){
        output = new DataOutputStream(stream);
    }

    void sendMessage (String message){
        try {
            output.writeUTF(message);
        } catch (IOException e) {
            System.err.println(e.getMessage());
            active = false;
        }
    }

    public boolean isActive(){
        return active;
    }
}