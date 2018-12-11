public class Main {
    public static void main (String[] args) throws Throwable{

        final int port = 12345;
        ClientContainer container = new ClientContainer();
        (new Thread(new Connector(port, container))).start();

    }
}
