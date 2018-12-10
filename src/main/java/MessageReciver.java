public interface MessageReciver {
    void sendMessages (String message, int clientId);
    void doCommand(String text, int clientId);
    void getAnswer (String result, int clientId);
}
