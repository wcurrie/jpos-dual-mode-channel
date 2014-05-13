package x;

import org.jpos.iso.packager.XMLPackager;
import org.junit.Test;

import java.net.ServerSocket;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;
import static x.HamcrestIsoMsgMatcher.isIsoMsg;
import static x.TestMessages.ping;
import static x.TestMessages.pong;

public class VersionedGzipChannelTest {

    @Test
    public void pingPong() throws Exception {
        ServerSocket serverSocket = new ServerSocket(0);

        VersionedGzipChannel client = new VersionedGzipChannel("localhost", serverSocket.getLocalPort(), new XMLPackager());
        client.setTimeout(5000);
        client.connect();

        client.send(ping());

        VersionedGzipChannel server = new VersionedGzipChannel(new XMLPackager());
        server.accept(serverSocket);

        assertThat(server.receive(), isIsoMsg(ping()));

        server.send(pong());

        assertThat(client.receive(), isIsoMsg(pong()));
    }
}