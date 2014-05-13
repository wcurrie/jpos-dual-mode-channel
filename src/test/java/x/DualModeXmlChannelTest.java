package x;

import org.jpos.iso.BaseChannel;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.channel.XMLChannel;
import org.jpos.iso.packager.XMLPackager;
import org.jpos.util.Logger;
import org.jpos.util.SimpleLogListener;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static x.HamcrestIsoMsgMatcher.isIsoMsg;
import static x.TestMessages.ping;
import static x.TestMessages.pong;

public class DualModeXmlChannelTest {

    private DualModeXmlChannel channel;
    private ServerSocket serverSocket;
    private EchoServer server;
    private BaseChannel client;
    private Logger logger;

    @Before
    public void setUp() throws Exception {
        logger = new Logger();
        logger.setName("logger");
//        logger.addListener(new SimpleLogListener());

        serverSocket = new ServerSocket(0);
        channel = new DualModeXmlChannel(new XMLPackager());
        channel.setLogger(logger, "server");
        server = new EchoServer();
    }

    @After
    public void tearDown() throws Exception {
        server.stop();
        if (client != null) {
            client.disconnect();
        }
    }

    @Test
    public void backwardsCompatible_afterReceivingRawXmlWillSendRawXml() throws Exception {
        client = new XMLChannel("localhost", serverSocket.getLocalPort(), new XMLPackager());
        canPlayPingPong();
    }

    @Test
    public void afterReceivingZippedXmlWillSendZippedXml() throws Exception {
        client = new VersionedGzipChannel("localhost", serverSocket.getLocalPort(), new XMLPackager());
        canPlayPingPong();
    }

    private void canPlayPingPong() throws IOException, ISOException {
        client.setTimeout(5000);
        channel.setLogger(logger, "client");
        client.connect();

        client.send(ping());
        assertThat(client.receive(), isIsoMsg(pong()));
    }

    private class EchoServer implements Runnable {

        private final ExecutorService executorService;
        private volatile boolean run = true;

        private EchoServer() {
            executorService = Executors.newSingleThreadExecutor();
            executorService.submit(this);
        }

        @Override
        public void run() {
            while (run) {
                try {
                    channel.accept(serverSocket);
                    ISOMsg request = channel.receive();
                    ISOMsg response = responseFor(request);
                    channel.send(response);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }

        private ISOMsg responseFor(ISOMsg request) throws ISOException {
            ISOMsg response = (ISOMsg) request.clone();
            response.setResponseMTI();
            response.set("48.1", "pong");
            return response;
        }

        public void stop() throws InterruptedException {
            run = false;
            try {
                serverSocket.close();
            } catch (IOException ignored) {
            }
            executorService.shutdown();
            executorService.awaitTermination(1, TimeUnit.SECONDS);
        }
    }
}