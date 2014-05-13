package x;

import org.jpos.iso.BaseChannel;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOPackager;
import org.jpos.iso.packager.XMLPackager;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Intended to be used on in an ISOServer where some clients are using XMLChannel and
 * the desire is to migrate clients to VersionedGzipChannel to save bytes on the wire.
 *
 * Relies on BaseChannel wrapping the InputStream from the socket with something supporting
 * mark and reset so we can sniff the message type.
 *
 * Works out which format to use for the session (conversion with a client) on the first
 * call to receive(). Typically from ISOServer.Session.
 */
public class DualModeXmlChannel extends BaseChannel {
    // <isomsg
    // #01xxxx
    private static final int PREFIX_LENGTH = 3; // <is or #01

    private Mode mode;

    public DualModeXmlChannel (String host, int port, ISOPackager p) {
        super(host, port, p);
    }

    public DualModeXmlChannel (ISOPackager p) throws IOException {
        super(p);
    }

    @Override
    protected byte[] streamReceive() throws IOException {
        if (mode == null) {
            String prefix = readModePrefix();
            if (prefix.equals("<is")) {
                mode = new RawMode();
            } else {
                mode = new ZippedMode();
            }
        }
        return mode.receive();
    }

    @Override
    protected void sendMessage(byte[] b, int offset, int len) throws IOException {
        if (mode == null) {
            throw new IllegalStateException("Dual mode channel must receive a message before it knows what format to use");
        }
        mode.send(b, offset, len);
    }

    private String readModePrefix() throws IOException {
        serverIn.mark(PREFIX_LENGTH);
        byte[] bytes = new byte[PREFIX_LENGTH];
        serverIn.readFully(bytes, 0, PREFIX_LENGTH);
        String prefix = new String(bytes, "UTF-8");
        serverIn.reset();
        return prefix;
    }

    protected int getHeaderLength() {
        // XML Channel does not support header
        return 0;
    }

    protected void sendMessageHeader(ISOMsg m, int len) {
        // XML Channel does not support header
    }

    public void disconnect () throws IOException {
        super.disconnect();
        if (mode != null) {
            mode.close();
        }
    }

    public String getMode() {
        if (mode instanceof RawMode) {
            return "Raw";
        }
        if (mode instanceof ZippedMode) {
            return "Zip";
        }
        return null;
    }

    private static interface Mode {
        byte[] receive() throws IOException;
        void send(byte[] b, int offset, int len) throws IOException;
        void close();
    }

    private class RawMode implements Mode {

        private BufferedReader reader = null;

        private RawMode() {
            reader = new BufferedReader(new InputStreamReader(serverIn));
        }

        @Override
        public byte[] receive() throws IOException {
            int sp = 0;
            StringBuilder sb = new StringBuilder();
            while (reader != null) {
                String s = reader.readLine();
                if (s == null)
                    throw new EOFException();
                sb.append (s);
                if (s.contains("<" + XMLPackager.ISOMSG_TAG))
                    sp++;
                if (s.contains("</" + XMLPackager.ISOMSG_TAG + ">"))
                {
                    if (--sp <= 0)
                        break;
                }
            }
            return sb.toString().getBytes();
        }

        @Override
        public void send(byte[] b, int offset, int len) throws IOException {
            serverOut.write(b, offset, len);
        }

        @Override
        public void close() {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ignored) {
                }
            }
            reader = null;
        }
    }

    private class ZippedMode implements Mode {

        @Override
        public void send(byte[] b, int offset, int len) throws IOException {
            VersionedGzipChannel.writeLength(len, serverOut);
            VersionedGzipChannel.writePayload(b, offset, len, serverOut);
        }

        @Override
        public byte[] receive() throws IOException {
            int length = VersionedGzipChannel.readLength(serverIn);
            byte[] received = new byte[length];
            VersionedGzipChannel.readPayload(received, 0, length, serverIn);
            return received;
        }

        @Override
        public void close() {
        }
    }
}
