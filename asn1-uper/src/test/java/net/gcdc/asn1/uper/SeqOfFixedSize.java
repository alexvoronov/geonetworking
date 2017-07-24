package net.gcdc.asn1.uper;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;

import net.gcdc.asn1.datatypes.Asn1SequenceOf;
import net.gcdc.asn1.datatypes.FixedSize;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SeqOfFixedSize {
        private final static Logger logger = LoggerFactory.getLogger(SeqOfFixedSize.class);

        @FixedSize(3)
        public static class Bar extends Asn1SequenceOf<Byte> {
            public Bar(Byte... coll)          { this(Arrays.asList(coll)); }
            public Bar(Collection<Byte> coll) { super(coll); }
        }


        @Test public void Test1() throws IllegalArgumentException, IllegalAccessException {
           Object pdu =
                        new Bar( (byte) 0xff, (byte) 0xff, (byte) 0xff);
            byte[] encoded = UperEncoder.encode(pdu);
            logger.debug("data hex: {}", UperEncoder.hexStringFromBytes(encoded));
            assertEquals("FFFFFF",
                    UperEncoder.hexStringFromBytes(encoded));
            Bar decoded = UperEncoder.decode(encoded, Bar.class);
            byte[] encoded2 = UperEncoder.encode(decoded);
            logger.debug("data-2 hex: {}", UperEncoder.hexStringFromBytes(encoded2));
            assertEquals("FFFFFF",
                    UperEncoder.hexStringFromBytes(encoded2));

        }


}
