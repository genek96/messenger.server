import java.net.URL;
import java.util.Properties;

public class Main {
    public static void main (String[] args) throws Throwable{

        //Loading configurations
        Properties properties = new Properties();
        URL url = ClassLoader.getSystemClassLoader().getResource("config.properties");
        properties.load(url.openStream());
        final int port = Integer.parseInt(properties.getProperty("port"));
        //Setting up objects
        ClientContainer container = new ClientContainer();
        (new Thread(new Connector(port, container))).start();

    }
}
