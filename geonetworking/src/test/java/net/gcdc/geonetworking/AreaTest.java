package net.gcdc.geonetworking;

import static net.gcdc.geonetworking.AreaTest.Support.areaParamToBytes;
import static net.gcdc.geonetworking.AreaTest.Support.asBinaryString;
import static net.gcdc.geonetworking.AreaTest.Support.getAreaByteBuffer;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.nio.ByteBuffer;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;

public class AreaTest {

  private static final boolean LOGGING = false;

  @Test
  public void testArea() {
    // check lat position
    for (int i = 0; i < 180; i++) {
      int lat = i - 90;
      testAreaPosition(lat, 0);
    }

    // check lon position
    for (int i = 0; i < 360; i++) {
      int lon = i - 180;
      testAreaPosition(0, lon);
    }

    // check distance
    for (int distance = 0; distance <= 65535; distance++) {
      testAreaDistance(distance);
    }

    // check degrees
    for (int degrees = 0; degrees < 359; degrees++) {
      testAreaDegrees(degrees);
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAreaDegreesMaxOver() {
    int degrees = 361;
    testAreaDegrees(degrees);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAreaDegreesMinOver() {
    int degrees = -1;
    testAreaDegrees(degrees);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAreaDistanceMaxOver() {
    int distance = 65536;
    testAreaDistance(distance);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAreaDistanceMinUnder() {
    int distance = -1;
    testAreaDistance(distance);
  }

  @Test
  public void testAreaTypes() {
    // Position slottsberget   = new Position(57.702878, 11.927723);
    // Position damen          = new Position(57.703864, 11.945876);
    Position sannegordshamn = new Position(57.708769, 11.927088);
    Position semcon         = new Position(57.709526, 11.943996);
    Position teater         = new Position(57.705714, 11.934608);
    Position jarntorget     = new Position(57.699786, 11.953191);
    Position kuggen         = new Position(57.706700, 11.938955);

    Area teaterSemcon = Area.ellipse(teater, 800, 400, 52);
    assertTrue(teaterSemcon.contains(semcon));
    assertFalse(teaterSemcon.contains(jarntorget));

    Area teaterTowardsKuggen = Area.ellipse(teater, 285, 50, 66);
    assertTrue(teaterTowardsKuggen.contains(kuggen));
    assertFalse(teaterTowardsKuggen.contains(semcon));

    Area teaterTowardsKuggenRect = Area.rectangle(teater, 285, 50, 66);
    assertTrue(teaterTowardsKuggenRect.contains(kuggen));
    assertFalse(teaterTowardsKuggenRect.contains(semcon));
    assertFalse(teaterTowardsKuggenRect.contains(sannegordshamn));

    Area aroundTeaterUpToKuggen = Area.circle(teater, 285);
    // assertTrue(aroundTeaterUpToKuggen.contains(kuggen));
    assertFalse(aroundTeaterUpToKuggen.contains(semcon));
  }

  @Test
  public void testEquality() {
    EqualsVerifier.forClass(Area.class).verify();
  }

  @Test
  public void testType() {
    assertEquals(Area.Type.CIRCLE, Area.Type.fromCode(0));
    assertEquals(Area.Type.RECTANGLE, Area.Type.fromCode(1));
    assertEquals(Area.Type.ELLIPSE, Area.Type.fromCode(2));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testTypeUnknownMaxAbove() {
    Area.Type.fromCode(3);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testTypeUnknownMinBelow() {
    Area.Type.fromCode(-1);
  }

  private void testAreaDegrees(int degrees) {
    Position center = new Position(51, 0);
    int distanceA = 100;
    int distanceB = 300;
    verifyArea(center, distanceA, distanceB, degrees);
  }

  private void testAreaDistance(int distance) {
    Position center = new Position(51, 0);
    int degrees = 45;
    verifyArea(center, distance, distance, degrees);
  }

  private void testAreaPosition(int lat, int lon) {
    Position center = new Position(lat, lon);
    int distanceA = 100;
    int distanceB = 300;
    int degrees = 45;
    verifyArea(center, distanceA, distanceB, degrees);
  }

  private void verifyArea(Position center, int distanceA, int distanceB, int degrees) {
    Area rect = Area.rectangle(center, distanceA, distanceB, degrees);
    verifyAreaRectangleBytes(rect, center, distanceA, distanceB, degrees);
    verifyAreaRectangleReturnTypes(rect, center);
    verifyAreaRectangleString(rect, center, distanceA, distanceB, degrees);
  }

  private void verifyAreaRectangleReturnTypes(Area rect, Position center) {
    assertEquals(Area.Type.RECTANGLE, rect.type());
    assertEquals(center, rect.center());
  }

  private void verifyAreaRectangleBytes(Area rectangle, Position center, int distanceA,
                                        int distanceB, int degrees) {
    ByteBuffer actualByteBuffer = getAreaByteBuffer();
    rectangle.putTo(actualByteBuffer);

    byte[] actual = actualByteBuffer.array();
    byte[] expected = areaParamToBytes(center, distanceA, distanceB, degrees);
    if (LOGGING) {
      System.out.println("Expected:");
      asBinaryString(expected);
      System.out.println("Actual:");
      asBinaryString(actual);
    }

    assertArrayEquals(expected, actual);

    Area actualArea = Area.getFrom(ByteBuffer.wrap(expected), Area.Type.RECTANGLE);
    assertEquals(rectangle, actualArea);
  }

  private void verifyAreaRectangleString(Area rectangle, Position center, int distanceA,
                                         int distanceB, int degrees) {
    String expected = "Area [center=" + center + ", distanceAmeters=" + distanceA
        + ", distanceBmeters=" + distanceB + ", angleDegreesFromNorth="+ degrees
        + ", type=RECTANGLE]";
    String actual = rectangle.toString();
    assertEquals(expected, actual);
  }

  static class Support {

    private static final double TENTH_MICRODEGREE_MULTIPLIER = 1E7;

    static void asBinaryString(byte[] bytes) {
      for (byte b : bytes) {
        System.out.println(Integer.toBinaryString(b & 255 | 256).substring(1));
      }
    }

    static byte[] areaParamToBytes(Position center, int distanceA, int distanceB, int degrees) {
      int latBytes = (int) (center.lattitudeDegrees() * TENTH_MICRODEGREE_MULTIPLIER);
      int lonBytes = (int) (center.longitudeDegrees() * TENTH_MICRODEGREE_MULTIPLIER);

      ByteBuffer expectedBuffer = getAreaByteBuffer();
      expectedBuffer.putInt(latBytes);
      expectedBuffer.putInt(lonBytes);
      expectedBuffer.putShort((short) distanceA);
      expectedBuffer.putShort((short) distanceB);
      expectedBuffer.putShort((short) degrees);
      return expectedBuffer.array();
    }

    static ByteBuffer getAreaByteBuffer() {
      return ByteBuffer.allocate(14);
    }
  }

}
