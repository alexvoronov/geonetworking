package net.gcdc.uppertester;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
//import static org.junit.Assert.fail;

import org.junit.Test;

public class ParserTest {

    public static class Msg3bytes {
        byte messageType = (byte) 0x99;
        @Size(3)
        int test1;
        @Size(6)
        long test2;
    }


    @Test public void test1() throws InstantiationException, IllegalAccessException {
        byte [] bytes = new byte[] {(byte) 0x01, (byte) 0x00};
        Parser parser = new Parser();
        Object cp = parser.parse2(bytes);
        assertTrue(cp instanceof InitializeResult);
    }

    @Test public void testCustomSizedFields() throws InstantiationException, IllegalAccessException {
        Msg3bytes msg = new Msg3bytes();
        msg.test1 = 100;
        msg.test2 = 200;
        byte[] encoded = Parser.toBytes(msg);
        assertEquals(10, encoded.length);

        Parser parser = new Parser();
        parser.registerMessage(Msg3bytes.class);
        Msg3bytes decoded = (Msg3bytes) parser.parse2(encoded);
        assertEquals(msg.messageType, decoded.messageType);
        assertEquals(msg.test1, decoded.test1);
        assertEquals(msg.test2, decoded.test2);
//        fail("Not yet implemented");
    }

}
