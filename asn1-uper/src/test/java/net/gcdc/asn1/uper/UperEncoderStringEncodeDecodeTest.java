package net.gcdc.asn1.uper;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UperEncoderStringEncodeDecodeTest {

    private final static Logger logger = LoggerFactory.getLogger(UperEncoderStringEncodeDecodeTest.class);

    @Test public void Utf8StringTest4() throws IllegalArgumentException, IllegalAccessException {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 15; i++) {
            sb.append("1234567890");
        }
        String string150 = sb.toString();
        Object pdu = new Utf8TestClass(
                new Utf8TestClass.CompanyName(string150)
              );
        byte[] encoded = UperEncoder.encode(pdu);
        logger.debug("data hex: {}", UperEncoder.hexStringFromBytes(encoded));
        assertEquals("8096313233343536373839303132333435363738393031323334353637383930313233343536373839303132333435363738393031323334353637383930313233343536373839303132333435363738393031323334353637383930313233343536373839303132333435363738393031323334353637383930313233343536373839303132333435363738393031323334353637383930",
                UperEncoder.hexStringFromBytes(encoded));

        Object decoded = UperEncoder.decode(encoded, Utf8TestClass.class);
        byte[] reencoded = UperEncoder.encode(decoded);
        assertArrayEquals("encoded and reencoded", encoded, reencoded);
    }

    @Test public void Utf8StringTest5() throws IllegalArgumentException, IllegalAccessException {
        Object pdu = new Utf8TestClass(
                new Utf8TestClass.CompanyName("mölndal")
              );
        byte[] encoded = UperEncoder.encode(pdu);
        logger.debug("data hex: {}", UperEncoder.hexStringFromBytes(encoded));
        assertEquals("086DC3B66C6E64616C",
                UperEncoder.hexStringFromBytes(encoded));

        Object decoded = UperEncoder.decode(encoded, Utf8TestClass.class);
        byte[] reencoded = UperEncoder.encode(decoded);
        assertArrayEquals("encoded and reencoded", encoded, reencoded);
    }

    @Test public void Utf8StringTest6() throws IllegalArgumentException, IllegalAccessException {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 15; i++) {
            sb.append("1234567890");
        }
        String string150 = sb.toString();
        Object pdu = new Utf8TestClass(
                new Utf8TestClass.CompanyName(string150)
              );
        byte[] encoded = UperEncoder.encode(pdu);
        logger.debug("data hex: {}", UperEncoder.hexStringFromBytes(encoded));
        assertEquals("8096313233343536373839303132333435363738393031323334353637383930313233343536373839303132333435363738393031323334353637383930313233343536373839303132333435363738393031323334353637383930313233343536373839303132333435363738393031323334353637383930313233343536373839303132333435363738393031323334353637383930",
                UperEncoder.hexStringFromBytes(encoded));
    }


}
