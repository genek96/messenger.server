import org.junit.Test;

import java.io.IOException;
import java.net.Socket;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ClientContainerTest {
    @Test
    public void testClientContainer() throws IOException {
        ClientContainer container = new ClientContainer();
        Socket socket = mock(Socket.class);
        when(socket.getOutputStream()).thenReturn(System.out);
        when(socket.getInputStream()).thenReturn(System.in);
        Socket socket2 = mock(Socket.class);
        when(socket2.getOutputStream()).thenReturn(System.out);
        when(socket2.getInputStream()).thenReturn(System.in);
        container.addNewClient(socket, "");
        container.addNewClient(socket2, "");
        container.addNewClient(socket, "");
        container.sendMessages("", 0);
        container.sendMessages(null, 0);
        container.sendMessages("", -1);
        container.sendMessages("", 1000);
    }
}
