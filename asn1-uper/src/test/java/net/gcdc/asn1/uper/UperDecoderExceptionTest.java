package net.gcdc.asn1.uper;

import net.gcdc.asn1.datatypes.Asn1Integer;
import net.gcdc.asn1.datatypes.IntRange;
import net.gcdc.asn1.datatypes.Sequence;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;

public final class UperDecoderExceptionTest {
    private final static Logger logger = LoggerFactory.getLogger(UperDecoderExceptionTest.class);

    @IntRange(minValue = 0, maxValue = 300)
    public static class IntVal extends Asn1Integer {
        public IntVal() { this(0); }
        public IntVal(int a) { super(a); }
    }

    @Sequence
    public static class PduWithIntegers {

        IntVal val1;
        IntVal val2;

        public PduWithIntegers(int a, int b) {
            val1 = new IntVal(a);
            val2 = new IntVal(b);
        }
        public PduWithIntegers() {
            val1 = null;
            val2 = null;
        }


    }

    @Test (expected = IllegalArgumentException.class)
    public void testException() {
        PduWithIntegers pdu = new PduWithIntegers(10, 20);

        byte[] encoded = UperEncoder.encode(pdu);
        logger.debug("encoded bitstring data hex: {}", UperEncoder.hexStringFromBytes(encoded));

        String msgExpectedFull = "050500";
        assertEquals(msgExpectedFull, UperEncoder.hexStringFromBytes(encoded));

        // Let's test that decoder can decode full message.
        PduWithIntegers decodedOk = UperEncoder.decode(UperEncoder.bytesFromHexString(msgExpectedFull), PduWithIntegers.class);
        assertArrayEquals(encoded, UperEncoder.encode(decodedOk));

        String msgFaulty = "0505";
        PduWithIntegers decodedFail = UperEncoder.decode(UperEncoder.bytesFromHexString(msgFaulty), PduWithIntegers.class);
        fail ("there should be exception");
    }

}
