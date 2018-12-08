import org.junit.Test;

import java.awt.*;

import static org.mockito.Mockito.mock;

public class ConnectorTest {
    @Test
    public void testConnector(){
        ClientContainer container = mock(ClientContainer.class);
        new Connector(12345, container);
        new Connector(1, container);
        new Connector(-1, null);
    }
}
