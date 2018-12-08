import org.junit.Assert;
import org.junit.Test;

public class SenderTest {
    @Test
    public void testConstructor(){
        new Sender(null);
        new Sender(System.out);
    }

    @Test
    public void testSender(){
        Sender sender = new Sender(System.out);
        sender.sendMessage("");
        sender.sendMessage("123");
        Assert.assertTrue(sender.isActive());
        sender.sendMessage(null);
        Assert.assertFalse(sender.isActive());
    }
}
