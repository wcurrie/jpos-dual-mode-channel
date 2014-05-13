package x.metrics;

import x.DualModeXmlChannel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AllocatedChannels {

    private static final List<DualModeXmlChannel> INSTANCES = new ArrayList<>();

    public static synchronized void record(DualModeXmlChannel channel) {
        INSTANCES.add(channel);
    }

    private static Map<String, List<CountingSocket>> groupByMode() {
        Map<String, List<CountingSocket>> split = new HashMap<>();
        for (DualModeXmlChannel channel : INSTANCES) {
            List<CountingSocket> sockets = split.get(channel.getMode());
            if (sockets == null) {
                sockets = new ArrayList<>();
                split.put(channel.getMode(), sockets);
            }
            CountingSocket socket = (CountingSocket) channel.getSocket();
            // not sure how we end up with null here...
            if (socket != null) {
                sockets.add(socket);
            }
        }
        return split;
    }

    public static synchronized void dump() {
        for (Map.Entry<String, List<CountingSocket>> entry : groupByMode().entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue().size() + " sockets");
            int out = 0;
            int in = 0;
            for (CountingSocket socket : entry.getValue()) {
                out += socket.getOutputByteCount();
                in += socket.getInputByteCount();
            }
            System.out.println("in: " + in + " out: " + out + " bytes");
        }
    }
}
