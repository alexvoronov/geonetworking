package net.gcdc.uppertester;

import java.lang.reflect.Field;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Parser {
    final static Logger logger = LoggerFactory.getLogger(Parser.class);

    private static final List<Class<?>> staticMessages = Arrays.asList(
            BtpEventIndication.class,
            BtpTriggerA.class,
            BtpTriggerB.class,
            BtpTriggerResult.class,
            CamEventIndication.class,
            CamTriggerChangeCurvature.class,
            CamTriggerChangeHeading.class,
            CamTriggerChangeSpeed.class,
            CamTriggerChangeYawRate.class,
            CamTriggerResult.class,
            CamTriggerSetAccelerationControlStatus.class,
            CamTriggerSetDangerousGoods.class,
            CamTriggerSetDangerousGoodsExt.class,
            CamTriggerSetDriveDirection.class,
            CamTriggerSetEmbarkationStatus.class,
            CamTriggerSetExteriorLightsStatus.class,
            CamTriggerSetLightBarSiren.class,
            CamTriggerSetPtActivation.class,
            CamTriggerSetStationType.class,
            CamTriggerSetVehicleRole.class,
            ChangePosition.class,
            ChangePositionResult.class,
            ChangePseudonym.class,
            ChangePseudonymResult.class,
            DenmEventIndication.class,
            DenmTerminationResult.class,
            DenmTrigger.class,
            DenmTriggerResult.class,
            DenmUpdate.class,
            DenmUpdateResult.class,
            GnEventIndication.class,
            GnTriggerGeoAnycast.class,
            GnTriggerGeoBroadcast.class,
            GnTriggerGeoUnicast.class,
            GnTriggerResult.class,
            GnTriggerSHB.class,
            GnTriggerTSB.class,
            Initialize.class,
            InitializeResult.class,
            TerminateDenmEvent.class
            );

    private final List<Class<?>> allMessages = new ArrayList<>(staticMessages);

    void registerMessage(Class<?> message) {
        allMessages.add(message);
        refreshMessageMap();
    }

    private final Map<Byte, Class<?>> msgToId = new HashMap<>();

    private void refreshMessageMap() {
        msgToId.clear();
        for (Class<?> c : allMessages) {
            try {
                // Object obj = c.newInstance();
                // logger.info("Adding class " + obj.getClass());
                Field f = c.getDeclaredField("messageType");
                f.setAccessible(true);
                byte msgType = f.getByte(c.newInstance());
                if (msgToId.containsKey(msgType)) {
                    logger.error("Duplicate message id " + msgType + " for " + c.getClass()
                            + " and " + msgToId.get(msgType));
                }
                msgToId.put(msgType, c);
            } catch (InstantiationException | NoSuchFieldException | IllegalAccessException e) {
                logger.error("Error adding class {}", c.getName());
            }
        }
    }

    public Parser() {
        refreshMessageMap();
    }

    public Object parse2(byte[] b) throws InstantiationException, IllegalAccessException {
        byte msgType = b[0];
        return parse(ByteBuffer.wrap(b), msgToId.get(msgType));
    }

    public static <T> T parse(ByteBuffer buffer, Class<T> classOfT) throws InstantiationException,
            IllegalAccessException {
        T obj = classOfT.newInstance();
        logger.debug("parsing class {}", classOfT.getName());
        for (Field f : classOfT.getDeclaredFields()) {
            logger.debug("field {}", f.getName());
            if (isTestInstrumentation(f)) { logger.debug("skipping {}", f.getName()); continue; }
            logger.debug("accepting {}", f.getName());
            f.setAccessible(true);
            if (f.getType().isAssignableFrom(int.class)) {
                logger.debug("integer!");
                Size sizeAnnotation = f.getAnnotation(Size.class);
                f.set(obj, sizeAnnotation == null ? buffer.getInt() :
                        getCustomInt(buffer, sizeAnnotation.value()).intValue());
            } else if (f.getType().isAssignableFrom(long.class)) {
                logger.debug("long!");
                Size sizeAnnotation = f.getAnnotation(Size.class);
                f.set(obj, sizeAnnotation == null ? buffer.getLong() :
                        getCustomInt(buffer, sizeAnnotation.value()).longValue());
            } else if (f.getType().isAssignableFrom(byte.class)) {
                logger.debug("Byte!");
                f.set(obj, buffer.get());
            } else if (f.getType().isAssignableFrom(short.class)) {
                logger.debug("Short!");
                f.set(obj, buffer.getShort());
            } else if (f.getType().isAssignableFrom(byte[].class)) {
                logger.debug("byte array!");
                byte[] arr = new byte[buffer.limit() - buffer.position()];
                buffer.get(arr);
                f.set(obj, arr);
            } else {
                logger.warn("ELSE!" + f.getType());
                throw new UnsupportedOperationException("Type " + f.getType() +
                        " is not implemented yet.");
            }
        }
        return obj;
    }

    private static boolean isTestInstrumentation(Field f) {
        return f.getName().startsWith("$");
    }

    private static BigInteger getCustomInt(ByteBuffer buffer, int size) {
        byte[] tmpBuffer = new byte[size];
        buffer.get(tmpBuffer);
        return new BigInteger(+1, tmpBuffer);
    }

    private static void putCustomInt(long value, ByteBuffer buffer, int size) {
        byte[] src = BigInteger.valueOf(value).toByteArray();
        byte[] dest = new byte[size];
        for (int d = dest.length - 1, s = src.length - 1; d >= 0 && s >= 0; d--, s--) {
            dest[d] = src[s];
        }
        // System.arraycopy(src, srcPos, dest, destPos, size);
        buffer.put(dest);
    }

    public static <T> byte[] toBytes(T obj) throws IllegalArgumentException, IllegalAccessException {
        ByteBuffer buffer = ByteBuffer.allocate(65535);
        for (Field f : obj.getClass().getDeclaredFields()) {
            if (isTestInstrumentation(f)) { continue; }
            f.setAccessible(true);
            if (f.getType().isAssignableFrom(int.class)) {
                logger.info("integer!");
                Size sizeAnnotation = f.getAnnotation(Size.class);
                if (sizeAnnotation == null) {
                    buffer.putInt(f.getInt(obj));
                } else {
                    putCustomInt(f.getInt(obj), buffer, sizeAnnotation.value());
                }
            } else if (f.getType().isAssignableFrom(long.class)) {
                logger.info("long!");
                Size sizeAnnotation = f.getAnnotation(Size.class);
                if (sizeAnnotation == null) {
                    buffer.putLong(f.getLong(obj));
                } else {
                    putCustomInt(f.getLong(obj), buffer, sizeAnnotation.value());
                }
            } else if (f.getType().isAssignableFrom(byte.class)) {
                logger.info("Byte!");
                buffer.put(f.getByte(obj));
            } else if (f.getType().isAssignableFrom(short.class)) {
                logger.info("Short!");
                buffer.putShort(f.getShort(obj));
            } else if (f.getType().isAssignableFrom(byte[].class)) {
                logger.info("byte array!");
                buffer.put((byte[]) f.get(obj));
            } else {
                logger.info("ELSE! {}:{}", f.getName(), f.getType().getName());
                throw new UnsupportedOperationException("Type " + f.getType() +
                        " is not implemented yet.");
            }
        }
        return Arrays.copyOfRange(buffer.array(), 0, buffer.position());
    }

}
