package com.example.nos3;

import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.yamcs.TmPacket;
import org.yamcs.YConfiguration;
import org.yamcs.tctm.AbstractPacketPreprocessor;
import org.yamcs.tctm.CcsdsPacketPreprocessor;
import org.yamcs.utils.ByteArrayUtils;
import org.yamcs.utils.TimeEncoding;

/**
 * Preprocessor for the CFS TM packets:
 * <ul>
 * <li>CCSDS primary header 6 bytes</li>
 * <li>Time seconds 4 bytes</li>
 * <li>subseconds(1/2^16 fraction of seconds) 2 bytes</li>
 * </ul>
 * 
 * Options:
 * 
 * <pre>
 *   dataLinks:
 *   ...
 *      packetPreprocessor: org.yamcs.tctm.cfs.CfsPacketPreprocessor
 *      packetPreprocessorArgs:
 *          byteOrder: LITTLE_ENDIAN
 *          timeEncoding:
 *              epoch: CUSTOM
 *              epochUTC: 1970-01-01T00:00:00Z
 *              timeIncludesLeapSeconds: false
 * 
 * </pre>
 * 
 * The {@code byteOrder} option (default is {@code BIG_ENDIAN}) is used only for decoding the timestamp in the secondary
 * header: the 4 bytes second and 2 bytes
 * subseconds are decoded in little endian.
 * <p>
 * The primary CCSDS header is always decoded as BIG_ENDIAN.
 * <p>
 * For explanation on the {@code timeEncoding} property, please see {@link AbstractPacketPreprocessor}. The default
 * timeEncoding used if none is specified, is GPS, equivalent with this configuration:
 * 
 * <pre>
 * timeEncoding:
 *     epoch: GPS
 * </pre>
 * 
 * which is also equivalent with this more detailed configuration:
 * 
 * <pre>
 * timeEncoding:
 *     epoch: CUSTOM
 *     epochUTC: "1980-01-06T00:00:00Z"
 *     timeIncludesLeapSeconds: true
 * </pre>
 */
public class MyPacketPreprocessor extends CcsdsPacketPreprocessor {
    static final int MINIMUM_LENGTH = 12;

    public MyPacketPreprocessor(String yamcsInstance) {
        this(yamcsInstance, YConfiguration.emptyConfig());
    }

    public MyPacketPreprocessor(String yamcsInstance, YConfiguration config) {
        super(yamcsInstance, config);
        if (!config.containsKey(CONFIG_KEY_TIME_ENCODING)) {
            this.timeEpoch = TimeEpochs.GPS;
        }
    }

    // public String byteToHex(byte num) {
    //     char[] hexDigits = new char[2];
    //     hexDigits[0] = Character.forDigit((num >> 4) & 0xF, 16);
    //     hexDigits[1] = Character.forDigit((num & 0xF), 16);
    //     return new String(hexDigits);
    // }

    // public String encodeHexString(byte[] byteArray) {
    //     StringBuffer hexStringBuffer = new StringBuffer();
    //     for (int i = 0; i < byteArray.length; i++) {
    //         hexStringBuffer.append(byteToHex(byteArray[i]));
    //     }
    //     return hexStringBuffer.toString();
    //}

    public static String bytesTohex(byte[] in){
        final StringBuilder builder = new StringBuilder();
        for(byte b: in){
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
    }

    @Override
    public TmPacket process(TmPacket packet) {

        byte[] bytes = packet.getPacket();
        String hex_bytes = bytesTohex(bytes);

        eventProducer.sendWarning("GOT_PACKET", "Got Packet: " + hex_bytes);

      
        // if (bytes.length < 6) { // Expect at least the length of CCSDS primary header
        //     eventProducer.sendInfo("SHORT_PACKET",
        //             "Short packet received, length: " + bytes.length + "; minimum required length is 6 bytes.");

        //     // If we return null, the packet is dropped.
        //     return null;
        // }

        // // Verify continuity for a given APID based on the CCSDS sequence counter
        // int apidseqcount = ByteBuffer.wrap(bytes).getInt(0);
        // int apid = (apidseqcount >> 16) & 0x07FF;
        // int seq = (apidseqcount) & 0x3FFF;
        // AtomicInteger ai = seqCounts.computeIfAbsent(apid, k -> new AtomicInteger());
        // int oldseq = ai.getAndSet(seq);

        // if (((seq - oldseq) & 0x3FFF) != 1) {
        //     eventProducer.sendWarning("SEQ_COUNT_JUMP",
        //             "Sequence count jump for APID: " + apid + " old seq: " + oldseq + " newseq: " + seq);
        // }

        // // Our custom packets don't include a secundary header with time information.
        // // Use Yamcs-local time instead.
        // packet.setGenerationTime(TimeEncoding.getWallclockTime());

        // // Use the full 32-bits, so that both APID and the count are included.
        // // Yamcs uses this attribute to uniquely identify the packet (together with the gentime)
        // packet.setSequenceCount(apidseqcount);

        return packet;
    }
}