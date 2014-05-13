/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2014 Alejandro P. Revilla
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package x;

import org.jpos.iso.*;

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Differs from GZIPChannel in that it has a fixed 3 byte prefix #01.
 * Rather arbitrary but differs from the first 3 bytes of anything produced by XMLChannel,
 * which is always &ltis.
 */
public class VersionedGzipChannel extends BaseChannel {

    private static final String VERSION = "01";

    public VersionedGzipChannel(ISOPackager p) throws IOException {
        super(p);
    }
    public VersionedGzipChannel(String host, int port, ISOPackager p) {
        super(host, port, p);
    }

    protected void sendMessageLength(int len) throws IOException {
        writeLength(len, serverOut);
    }

    protected int getMessageLength() throws IOException, ISOException {
        return readLength(serverIn);
    }

    protected void sendMessage (byte[] b, int offset, int len) throws IOException {
        writePayload(b, offset, len, serverOut);
    }

    protected void getMessage (byte[] b, int offset, int len) throws IOException, ISOException {
        readPayload(b, offset, len, serverIn);
    }

    public static void writeLength(int len, DataOutputStream out) throws IOException {
        out.write(("#" + VERSION).getBytes("UTF-8"));
        out.writeInt(len);
    }

    public static int readLength(DataInputStream in) throws IOException {
        in.readFully(new byte[3]); // consume #01 version header
        return in.readInt();
    }

    public static void writePayload(byte[] b, int offset, int len, DataOutputStream out) throws IOException {
        GZIPOutputStream gzip = new GZIPOutputStream(out);
        gzip.write(b, offset, len);
        gzip.finish();
        gzip.flush();
    }

    public static void readPayload(byte[] b, int offset, int len, DataInputStream in) throws IOException {
        int total = 0;
        GZIPInputStream gzip = new GZIPInputStream(in);
        while (total < len) {
            int nread = gzip.read (b, offset, len - total);
            if (nread == -1) {
                throw new IOException("End of compressed stream reached before all data was read");
            }
            total += nread;
            offset += nread;
        }
    }
}

