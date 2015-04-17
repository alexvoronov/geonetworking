package net.gcdc.camdenm;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class UperEncoderTest {

    @Test public void testBin() {
        String binString = "00010101";  // Length multiple of 8.
        assertEquals(binString, UperEncoder.toBinary(UperEncoder.fromBinaryString(binString)));
    }

    @Test public void testHex() {
        String hexString = "AABB00FF";  // Length multiple of 2.
        assertEquals(hexString, UperEncoder.toHexString(UperEncoder.fromHexString((hexString))));
    }
}
