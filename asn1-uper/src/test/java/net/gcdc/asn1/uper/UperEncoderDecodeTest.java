package net.gcdc.asn1.uper;

import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.fail;
import net.gcdc.camdenm.CoopIts.ItsPduHeader;
import net.gcdc.camdenm.CoopIts.ItsPduHeader.MessageId;
import net.gcdc.camdenm.CoopIts.ItsPduHeader.ProtocolVersion;
import net.gcdc.camdenm.CoopIts.StationID;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UperEncoderDecodeTest {
    private final static Logger logger = LoggerFactory.getLogger(UperEncoderDecodeTest.class);

    @Test public void decodeTest1() throws IllegalArgumentException, IllegalAccessException, InstantiationException {
        Object pdu = new ItsPduHeader(new ProtocolVersion(13), new MessageId(15), new StationID(17));
        byte[] encoded = UperEncoder.encode(pdu);
        logger.debug("data hex: {}", UperEncoder.hexStringFromBytes(encoded));
        assertEquals("0D0F00000011",
                UperEncoder.hexStringFromBytes(encoded));

        ItsPduHeader decoded = UperEncoder.decode(encoded, ItsPduHeader.class);
        logger.debug("decoded: {}", decoded);
        byte[] reencoded = UperEncoder.encode(decoded);
        logger.debug("data hex: {}", UperEncoder.hexStringFromBytes(reencoded));
        assertEquals("0D0F00000011",
                UperEncoder.hexStringFromBytes(reencoded));

    }
    @Test public void test() {
        //fail("Not yet implemented");
    }

}
