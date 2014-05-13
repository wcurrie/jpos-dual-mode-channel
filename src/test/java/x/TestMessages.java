package x;

import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;

public class TestMessages {

    public static ISOMsg ping() throws ISOException {
        ISOMsg msg = new ISOMsg("0800");
        msg.set("48.1", "ping");
        return msg;
    }

    public static ISOMsg ping(String payload) throws ISOException {
        ISOMsg msg = new ISOMsg("0800");
        msg.set("48.1", payload);
        return msg;
    }

    public static  ISOMsg pong() throws ISOException {
        ISOMsg msg = new ISOMsg("0810");
        msg.set("48.1", "pong");
        return msg;
    }
}
