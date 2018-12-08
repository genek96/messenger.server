import org.junit.Test;

import java.io.InputStream;

import static org.mockito.Mockito.mock;

public class ReciverTest {
    @Test
    public void testConstructor(){
        new Receiver(null, null, 1);
        new Receiver(System.in, null, 1);
    }

    @Test (timeout = 1000)
    public void testReceiver() throws InterruptedException {
        InputStream stream = System.in;
        MessageReciver router = mock(MessageReciver.class);
        Receiver reciver = new Receiver(stream, router, 1);
        Thread testThread = new Thread(reciver);
        testThread.start();
        Thread.sleep(500);
        testThread.interrupt();
    }
}
