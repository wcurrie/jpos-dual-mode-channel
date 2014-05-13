package x;

import x.metrics.AllocatedChannels;
import x.metrics.CountingSocket;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public aspect SocketByteCounter {

    public void DualModeXmlChannel.connect(Socket socket) throws IOException {
        CountingSocket countingSocket = new CountingSocket(socket);
        super.connect(countingSocket);
        AllocatedChannels.record(this);
    }


}
